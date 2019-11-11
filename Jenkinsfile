pipeline {
    agent {
        label "lead-toolchain-maven" 
    }
    stages {
        stage('Verify') {
            when {
                not {
                    branch 'master'
                }
            }
            steps {
                container('maven') {
                    sh "mvn -B clean verify"
                }
            }
        }
        stage('Deploy') {
            when {
                branch 'master'
            }
            steps {
                container('maven') {
                    sh "mvn -B -e clean deploy" 
                }
            }
        }
    }
}

