//library 'pipeline-library'
library 'LEAD'

pipeline {
  agent any
  stages {
    stage('Gradle build and deploy') {
      steps {
        notifyPipelineStart([Jenkinsfile: 'Jenkinsfile'])
        notifyStageStart()
        withCredentials([usernamePassword(credentialsId: 'Artifactory', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh 'gradle artifactoryPublish '
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
