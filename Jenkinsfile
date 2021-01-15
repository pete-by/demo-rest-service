
def getLatestRevisionFromGit() {
    def defaultRevision = '1.0.0'
    def latestRevision = sh returnStdout: true, script: "git describe --match=*.*.* --abbrev=0 2> /dev/null || echo ${defaultRevision}"
    latestRevision
}

def nextRevisionFromGit(scope) {
    def (major, minor, patch) = getLatestRevisionFromGit().tokenize('.').collect { it.toInteger() }
    def nextRevision
    switch (scope) {
        case 'major':
            nextRevision = "${major + 1}.0.0"
            break
        case 'minor':
            nextRevision = "${major}.${minor + 1}.0"
            break
        case 'patch':
            nextRevision = "${major}.${minor}.${patch + 1}"
            break
    }
    nextRevision
}

def revision
def sameRevision = false
def commitId

pipeline {
    environment {
        DOCKER_REGISTRY_SECRET = 'docker-hub-secret'
    }
    agent {
        kubernetes {
            yamlFile 'KubernetesPod.yaml'
        }
    }
    stages {
        stage('Build') {
            steps {

                withCredentials([usernamePassword(credentialsId: 'artifactory-secret',
                                                usernameVariable: 'ARTIFACTORY_STAGING_USERNAME',
                                                passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD')]) {
                    container('build-container') {
                        script {
                            // get HEAD revision hash
                            commitId = sh returnStdout: true, script: 'git rev-parse --short HEAD'
                            def commitRevision
                            try {
                               // get a release version (revision) if it is associated with the commit
                               commitRevision = sh returnStdout: true, script: "git describe --exact-match --tags ${commitId} 2> /dev/null || echo ''"
                               commitRevision = commitRevision.trim() // to trim new lines
                            } catch(err) {
                                println(err.toString())
                            }

                            if (commitRevision?.trim()) {
                                sameRevision = true  // we should not increment release version and try to put a tag again
                                revision = commitRevision; // reuse the revision number
                            } else {
                                revision = nextRevisionFromGit("patch") // TODO: determine from tag or commit message, by default patch
                            }
                            sh "mvn clean compile -Drevision=${revision} -Dsha1=${commitId}"
                        }
                    }
                }

            }
        } // Build
        stage('Test') {
            steps {
                echo 'Testing...'
                container('build-container') {
                    sh 'mvn test'
                }
            }
        }
        stage('Deploy') {

            steps {
                echo 'Deploying artefacts to repositories....'

                withCredentials([usernamePassword(credentialsId: 'docker-hub-secret', usernameVariable: 'DOCKER_REGISTRY_USERNAME', passwordVariable: 'DOCKER_REGISTRY_PASSWORD'),
                                 usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'ARTIFACTORY_STAGING_USERNAME', passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD'),
                                 usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'HELM_STABLE_USERNAME', passwordVariable: 'HELM_STABLE_PASSWORD')]) {

                    container('build-container') {
                        sh "mvn deploy -PdeployToArtifactory,deployToHelmRepo,dcr -Drevision=${revision} -Dsha1=${commitId}"
                    }

                } // withCredentials

                if(!sameRevision) { // only tag release and push if it there were changes
                    sshagent(credentials: ['github-secret']) {
                       sh """
                          git tag -a ${revision} -m 'Jenkins Build Agent'
                          git push origin --tags
                        """
                    }
                }

                if(env.BRANCH_NAME == "master") {
                    build job: "gke-deployment-pipeline", parameters: [string(name: 'REVISION', value: commitId)], wait: false
                }
            }

        }
    }
}