def call(Map params = [:]) {
    params = [
        vagrantCloudCredentialsId: 'vagrant-cloud',
        agentLabel: 'vbox',
        template: 'packer.json'
    ] << params

    pipeline {
        agent {
            label "${params.agentLabel}"
        }

        options {
            ansiColor('xterm')
        }

        environment {
            VAGRANT_CLOUD_TOKEN = credentials("${params.vagrantCloudCredentialsId}")
        }

        stages {
            stage('Clean') {
                steps {
                    sh 'rm -rf output-*'
                }
            }
            stage('Build') {
                steps {
                    sh "packer build ${params.template}"
                }
            }
        }
    }
}
