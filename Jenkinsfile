def getLatestRevisionFromGit() {
    def defaultRevision = '1.0.0'
    def latestRevision = sh returnStdout: true, script: 'git describe --tags "$(git rev-list --tags=*.*.* --max-count=1 2> /dev/null)" 2> /dev/null || echo ${defaultRevision}'
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

                withCredentials([usernamePassword(credentialsId: 'docker-hub-secret', usernameVariable: 'DOCKER_HUB_USERNAME', passwordVariable: 'DOCKER_HUB_PASSWORD'),
                                 usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'ARTIFACTORY_STAGING_USERNAME', passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD')]) {
                    container('build-container') {
                        script {
                          def commitId = sh(returnStdout: true, script: 'git rev-parse --short HEAD')
                          def latestRevision = getLatestRevisionFromGit()
                          sh "mvn clean compile -Drevision=${latestRevision} -Dsha1=${commitId}"
                        }
                    }
                }

            }
        } // Build
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {

            script {

              def nextVersion = nextVersionFromGit("patch")
              sh("git tag ${nextVersion}")
              sshagent(credentials: ['github-secret']) {
                sh("git push origin --force -v --tags")
              }
            }

            steps {
                echo 'Deploying....'
            }
        }
    }
}