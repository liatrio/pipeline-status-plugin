pipeline {
    agent {
        label "lead-toolchain-maven" 
    }
    stages {
        stage('Test & Package Artifact') {
            steps {
                container('maven-test') {
                    sh "make build"
                }
            }
        }
        stage('Deploy') {
            steps {
                container('maven-test') {
                    sh "make deploy" 
                }
            }
        }
    }
}

