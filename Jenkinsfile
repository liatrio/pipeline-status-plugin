library 'pipeline-library'
//library 'LEAD'

pipeline {
  agent any
  stages {
    stage('Gradle build and deploy') {
      steps {
        gradleBuildAndDeploy()
      }
    }
  }
}
