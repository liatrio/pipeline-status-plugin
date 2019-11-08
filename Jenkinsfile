pipeline {
    agent {
        label "lead-toolchain-maven" 
    }
    stages {
        stage('Test & Package Artifact') {
            steps {
                container('maven-test') {
                    sh "pwd"
                    sh "make build"
                    sh "sleep 360"
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

