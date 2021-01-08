def getLatestRevisionFromGit() {
    def defaultRevision = '1.0.0'
    def latestRevision = sh returnStdout: true, script: "git describe --tags '\$(git rev-list --tags=*.*.* --max-count=1 2> /dev/null)' 2> /dev/null || echo ${defaultRevision}"
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

def revision;
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

                            commitId = sh returnStdout: true, script: 'git rev-parse --short HEAD'
                            def commitRevision
                            try {
                               commitRevision = sh returnStdout: true, script: "git describe --exact-match --tags ${commitId} 2> /dev/null || exit 0 "
                            } catch(Exception e) {
                                // ignore
                            }

                            if (commitRevision?.trim()) {
                                revision = commitRevision; // reuse the revision number of this commit to avoid patch increment
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
                echo 'Deploying....'
                script {

                    withCredentials([usernamePassword(credentialsId: 'docker-hub-secret', usernameVariable: 'DOCKER_REGISTRY_USERNAME', passwordVariable: 'DOCKER_REGISTRY_PASSWORD'),
                                     usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'ARTIFACTORY_STAGING_USERNAME', passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD'),
                                     usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'HELM_STABLE_USERNAME', passwordVariable: 'HELM_STABLE_PASSWORD')]) {

                        container('build-container') {
                            sh "mvn deploy -s \${MAVEN_HOME}/conf/settings.xml -PdeployToArtifactory,staging -Drevision=${revision} -Dsha1=${commitId}"
                        }

                    } // withCredentials

                    sh("git tag -a ${revision} -m 'Jenkins'")

                    sshagent(credentials: ['github-secret']) {
                        sh("git push origin --tags")
                    }
                }
            }
        }
    }
}