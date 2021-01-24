
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

def writeReleaseInfo(info) {
  def releaseInfo = info.toMapString()
  writeYaml file: 'release-info.yaml', data: releaseInfo
}

def revision
def sameRevision = false
def commitId
def sha1
def appName = "demo-rest-service"
def appGitRepo = "git@github.com:pete-by/demo-rest-service.git"
def appVersion

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

                echo "Jenkinsfile in master is used"
                echo "Running build on ${env.GIT_BRANCH}"

                withCredentials([usernamePassword(credentialsId: 'artifactory-secret',
                                                usernameVariable: 'ARTIFACTORY_STAGING_USERNAME',
                                                passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD')]) {
                    container('build-container') {
                        script {
                            // get HEAD revision hash
                            commitId = sh returnStdout: true, script: 'git rev-parse HEAD'
                            sha1 = commitId.substring(0, 7)
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
                            appVersion = revision + "-" + sha1
                            sh "mvn clean compile -Drevision=${revision} -Dsha1=${sha1}"
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
                        sh "mvn deploy -PdeployToArtifactory,deployToHelmRepo,dcr -Drevision=${revision} -Dsha1=${sha1}"
                    }

                } // withCredentials

                script {
                    if(!sameRevision) { // only tag release and push if it there were changes
                        sshagent(credentials: ['github-ssh-secret']) {
                           sh """
                              git tag -a ${revision} -m 'Jenkins Build Agent'
                              git push origin --tags
                           """
                        }

                        sh "mkdir gke-deployment-pipeline" // create a target folder for checkout
                        dir('gke-deployment-pipeline') {

                            checkout([$class: 'GitSCM', branches: [[name: '*/master']],
                                       userRemoteConfigs: [[credentialsId: 'github-ssh-secret',
                                       url: 'git@github.com:pete-by/gke-deployment-pipeline.git']]])

                            echo 'Preparing release info'
                            def releaseInfo = [ version: appVersion, stage: 'dev',
                                                vcs: [revision: commitId, url: appGitRepo],
                                                modules: [[
                                                    name: appName,
                                                    artifacts: [name: appName + "-chart", type: "helm", sha1: "TODO", md5: "TODO"]
                                                ]]
                                              ]

                            echo 'Writing release info'
                            writeReleaseInfo(releaseInfo);

                            echo 'Pushing release info'
                            sh """
                               git add release-info.yaml
                               git commit -m "Created a release info for ${appVersion}"
                               git push -u origin ${version}
                            """

                        }

                    }
                }
            }

        }
    }
}