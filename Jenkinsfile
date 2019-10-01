//library 'pipeline-library'
library 'LEAD'

pipeline {
  agent any
  stages {
    stage('Gradle build and deploy') {
      agent {
        label "lead-toolchain-maven"
      }
      steps {
        notifyPipelineStart([Jenkinsfile: 'Jenkinsfile'])
        notifyStageStart()
        container('maven') {
          sh 'mvn package'
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
