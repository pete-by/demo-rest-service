def nextVersionFromGit(scope) {
    def latestVersion = sh returnStdout: true, script: 'git describe --tags "$(git rev-list --tags=*.*.* --max-count=1 2> /dev/null)" 2> /dev/null || echo 1.0.0'
    def (major, minor, patch) = latestVersion.tokenize('.').collect { it.toInteger() }
    def nextVersion
    switch (scope) {
        case 'major':
            nextVersion = "${major + 1}.0.0"
            break
        case 'minor':
            nextVersion = "${major}.${minor + 1}.0"
            break
        case 'patch':
            nextVersion = "${major}.${minor}.${patch + 1}"
            break
    }
    nextVersion
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
            // sh 'set'
            withCredentials([usernamePassword(credentialsId: 'docker-hub-secret', usernameVariable: 'DOCKER_HUB_USERNAME', passwordVariable: 'DOCKER_HUB_PASSWORD'),
                             usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'ARTIFACTORY_STAGING_USERNAME', passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD')]) {
                container('build-container') {
                    script {
                      def commitId = sh(returnStdout: true, script: 'git rev-parse --short HEAD')
                      sh "mvn clean install -Drevision=1.0.0 -Dsha1=${commitId}"
                      def nextVersion = nextVersionFromGit("patch")
                      sh("git tag ${nextVersion}")
                      sh("git push")
                    }
                }
            }
          }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}