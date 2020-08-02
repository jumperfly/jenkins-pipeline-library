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
                    sh 'rm -rf output-* roles'
                }
            }
            stage ('Download Roles') {
                when {
                    expression { return fileExists("requirements.yml") }
                }
                steps {
                    sh "ansible-galaxy install -r requirements.yml -p roles"
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
