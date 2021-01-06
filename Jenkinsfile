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
                  def commitId = sh(returnStdout: true, script: 'git rev-parse --short HEAD')
                  sh 'mvn clean install -Drevision=1.0.0 -Dsha1=$commitId'
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