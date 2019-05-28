def call(Map params = [:]) {
    params = [
        vagrantCloudCredentialsId: 'vagrant-cloud',
        baseBox: 'jumperfly/centos-7',
        baseBoxVersion: '7.6.1',
        ansibleRequirementsFile: 'requirements.yml',
        agentLabel: 'packer && ansible && virtualbox && vagrant'
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
            BASE_BOX = "${params.baseBox}"
            BASE_BOX_PATH = params.baseBox.replace('/', '-VAGRANTSLASH-')
            BASE_BOX_VERSION = "${params.baseBoxVersion}"
        }

        stages {
            stage('Clean') {
                when { 
                    expression { return fileExists("output-virtualbox-ovf") }
                }
                steps {
                    sh 'rm -rf output-virtualbox-ovf'
                }
            }
            stage ('Download Roles') {
                when { 
                    expression { return fileExists("${params.ansibleRequirementsFile}") }
                }
                steps {
                    sh "ansible-galaxy install -r ${params.ansibleRequirementsFile} -p roles"
                }
            }
            stage ('Download Base Box') {
                when { 
                    expression { return !fileExists("$HOME/.vagrant.d/boxes/$BASE_BOX_PATH/$BASE_BOX_VERSION/virtualbox/box.ovf") }
                }
                steps {
                    sh "vagrant box add $BASE_BOX --box-version $BASE_BOX_VERSION"
                }
            }
            stage('Build') {
                steps {
                    sh 'packer build packer.json'
                }
            }
        }
    }
}