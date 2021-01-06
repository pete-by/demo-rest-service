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
        /*
        stage('Run maven') {
          steps {
            sh 'set'
            sh "echo OUTSIDE_CONTAINER_ENV_VAR = ${CONTAINER_ENV_VAR}"
            container('maven') {
              sh 'echo MAVEN_CONTAINER_ENV_VAR = ${CONTAINER_ENV_VAR}'
              sh 'mvn -version'
            }
          }
        }
        */
        stage('Run build container') {
          steps {
            sh 'set'
            sh "echo OUTSIDE_CONTAINER_ENV_VAR = ${CONTAINER_ENV_VAR}"
            sh 'whoami'
            container('build-container') {
              sh 'echo MAVEN_CONTAINER_ENV_VAR = ${CONTAINER_ENV_VAR}'
              sh 'whoami'
              sh 'ls /home/jenkins/.m2'
              sh 'mvn clean install'
              sh 'ls /home/jenkins/.m2'
            }
          }
        }
        /*
        stage('setup') {
            steps {
                sh "env"
                sh "/usr/local/bin/docker -v"
            }
        }
        stage('Build') {
            agent {
                docker {
                    image 'peteby/build-container'
                    registryUrl 'https://docker.io'
                    registryCredentialsId 'docker-hub-secret'
                    args '-v ${PWD}:/app -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                sh 'mvn clean install'
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
        }*/
    }
}