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
            when {
                branch 'master'
            }
            steps {
                container('maven') {
                  echo "deploy"
                }
            }
        }
    }
}

