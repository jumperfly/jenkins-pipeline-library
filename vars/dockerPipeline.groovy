def call(Map params = [:]) {
    params = [
        dockerHubCredentialsId: 'docker-hub',
        agentLabel: 'master',
        imageName: ''
    ] << params

    pipeline {
        agent {
            label "${params.agentLabel}"
        }

        options {
            ansiColor('xterm')
        }

        environment {
            GIT_COMMIT_SHORT = sh(script: "printf \$(git rev-parse --short ${GIT_COMMIT})", returnStdout: true)
            DOCKER_HUB = credentials("${params.dockerHubCredentialsId}")
        }

        stages {
            stage('Build') {
                steps {
                    sh "docker build . -t ${params.imageName}:${GIT_COMMIT_SHORT}"
                }
            }
            stage('Push') {
                steps {
                    sh "docker login -u ${DOCKER_HUB_USR} -p ${DOCKER_HUB_PSW}"
                    sh "docker push ${params.imageName}:${GIT_COMMIT_SHORT}"
                }
            }
        }
    }
}
