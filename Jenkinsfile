/**
 Get latest release number (revision) by finding latest release tag
*/
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
  def releaseInfo = info
  writeYaml file: 'release-info.yaml', data: releaseInfo, overwrite: true
}

def GITHUB_SSH_SECRET = 'github-ssh-secret'
def RELEASE_BRANCH_NAME = 'master'
def releaseRevision
def revision
def sameRevision = false
def commitId
def sha1
def appName = "demo-rest-service"
def appGitRepo = "git@github.com:pete-by/demo-rest-service.git"
def helmRepo = "https://axamit.jfrog.io/artifactory/helm-stable"
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

                // Determine target version
                script {
                    // get HEAD revision hash
                    commitId = sh returnStdout: true, script: 'git rev-parse HEAD'
                    sha1 = commitId.substring(0, 7)
                    try {
                       // get a release number (revision) if it is associated with the current commit
                       releaseRevision = sh returnStdout: true, script: "git describe --exact-match --tags $commitId 2> /dev/null || echo ''"
                       releaseRevision = releaseRevision.trim() // to trim new lines
                    } catch(err) {
                        println(err.toString())
                    }

                    if (releaseRevision?.trim()) {
                        sameRevision = true  // we should not increment release version and try to put a tag again (will produce an error)
                        revision = releaseRevision; // reuse the revision number from existing release tag
                    } else {
                        if(env.BRANCH_NAME == RELEASE_BRANCH_NAME) { // bump a new release revision if we are on releasable branch
                            revision = nextRevisionFromGit("patch") // TODO: determine element to increment from tag or commit message, by default patch
                            // only tag releasable branch, not private or feature branches
                            echo "Tagging the source with $revision tag"
                            sshagent(credentials: [GITHUB_SSH_SECRET]) {
                               sh """
                                  git tag -a $revision -m 'Jenkins Build Agent'
                                  git push origin --tags
                               """
                            }
                        } else {
                            revision = getLatestRevisionFromGit() // reuse last released revision
                        }

                    }
                    appVersion = revision + "-" + sha1
                }

                // Run compile
                withCredentials([usernamePassword(credentialsId: 'artifactory-secret',
                                                usernameVariable: 'ARTIFACTORY_STAGING_USERNAME',
                                                passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD')]) {
                    container('build-container') {
                        script {
                            sh "mvn clean compile -Drevision=$revision -Dsha1=$sha1"
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

                // Publish artifacts
                withCredentials([usernamePassword(credentialsId: 'docker-hub-secret', usernameVariable: 'DOCKER_REGISTRY_USERNAME', passwordVariable: 'DOCKER_REGISTRY_PASSWORD'),
                                 usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'ARTIFACTORY_STAGING_USERNAME', passwordVariable: 'ARTIFACTORY_STAGING_PASSWORD'),
                                 usernamePassword(credentialsId: 'artifactory-secret', usernameVariable: 'HELM_STABLE_USERNAME', passwordVariable: 'HELM_STABLE_PASSWORD')]) {

                    container('build-container') {
                        sh "mvn deploy -PdeployToArtifactory,deployToHelmRepo,dcr -Drevision=$revision -Dsha1=$sha1"
                    }

                } // withCredentials

                script {

                    if(!sameRevision) { // only tag release and push if it there were changes, to avoid re-deployments

                        sh "mkdir gke-deployment-pipeline" // create a target folder for checkout
                        dir('gke-deployment-pipeline') {

                            checkout([$class: 'GitSCM', branches: [[name: '*/master']],
                                       userRemoteConfigs: [[credentialsId: GITHUB_SSH_SECRET,
                                       url: 'git@github.com:pete-by/gke-deployment-pipeline.git']]])
                            /*
                             TODO: should we create a branch named after release (e.g. 2.1.0) while non-releasable
                             branches after version (e.g. 2.1.0-j5o1we).
                             Having branch names the same as tags considered a bad practice though
                             */
                            sh """
                               git checkout -b $appVersion
                            """

                            echo 'Preparing release info'
                            // TODO: consider using maven task to create the release-info.yaml
                            def helmChartFilename = appName + '-' + appVersion + '.tgz'
                            def releaseInfo = [ version: appVersion,
                                                vcs: [revision: commitId,
                                                      release: (env.BRANCH_NAME == RELEASE_BRANCH_NAME) ? revision : null,
                                                      url: appGitRepo],
                                                modules: [[
                                                    name: appName,
                                                    artifacts: [
                                                                [name: appName,
                                                                 filename: helmChartFilename,
                                                                 url: helmRepo + '/' + helmChartFilename,
                                                                 type: "helm", sha1: "TODO", md5: "TODO"]
                                                    ]
                                                ]]
                                              ]

                            echo 'Writing release info'
                            writeReleaseInfo(releaseInfo);

                            echo 'Pushing release info'
                            sshagent(credentials: [GITHUB_SSH_SECRET]) {
                                // commit release-info.yaml
                                sh """
                                   git add release-info.yaml
                                   git commit -m "Jenkins Build Agent: created a release-info.yaml for $appVersion"
                                   git push --atomic -u origin $appVersion
                                """
                            }
                        }

                    }
                }
            }

        }
    }
}