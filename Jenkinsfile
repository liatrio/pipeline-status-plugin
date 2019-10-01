library 'pipeline-library'
library 'LEAD'

pipeline {
  agent any
  stages {
    stage('Gradle build and deploy') {
      steps {
        notifyPipelineStart([Jenkinsfile: 'Jenkinsfile'])
        notifyStageStart()
        gradleBuildAndDeploy()
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
