//library 'pipeline-library'
library 'LEAD'

pipeline {
  agent any
  stages {
    stage('Gradle Package and Deploy') {
      agent {
        label "lead-toolchain-maven"
      }
      steps {
        notifyPipelineStart([Jenkinsfile: 'Jenkinsfile'])
        notifyStageStart()
        container('gradle') {
          sh 'gradle clean assemble test'
        }
      }
      post {
        success {
          notifyStageEnd()
        }
        failure {
          notifyStageEnd([result: "fail"])
        }
      }
    }
  }
  post {
    success {
      echo "Pipeline Success"
      notifyPipelineEnd()
    }
    failure {
      echo "Pipeline Fail"
      notifyPipelineEnd([result: "fail"])
    }
  }
}
