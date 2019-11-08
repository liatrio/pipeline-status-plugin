pipeline {
    agent {
        label "lead-toolchain-maven" 
    }
    stages {
        stage('Test & Package Artifact') {
            when {
                branch 'master'
            }
            steps {
                container('maven-test') {
                    sh "make build"
                }
            }
        }
        stage('Deploy') {
            when {
                branch 'master'
            }
            steps {
                container('maven-test') {
                    sh "make deploy" 
                }
            }
        }
    }
}

