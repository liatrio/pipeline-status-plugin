pipelineJob('job-embedded-in-casc') {
  definition {
    cps {
      script("""\
        pipeline {
          agent any
          stages {
            stage('Stage1') {
              steps {
                sh "sleep 5"
                echo "Hello from 1"
              }
            }
            stage('Stage2') {
              steps {
                sh "sleep 5"
                echo "Hello from 2!"
              }
            }
            stage('Stage3') {
              steps {
                sh "sleep 5"
                error "Error from 3!"
              }
            }
            stage('Stage4') {
              steps {
                sh "sleep 5"
                echo "Hello from 3!"
              }
            }
          }
        }""".stripIndent())
    }
  }
}
