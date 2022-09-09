pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    durabilityHint('PERFORMANCE_OPTIMIZED')
    disableConcurrentBuilds()
  }
  stages{
    stage('build') {
      steps {
        sh '/opt/gradle/gradle-7.4.2/bin/gradle clean jar'
      }
    }
    stage('archive') {
      steps {
        archiveArtifacts(artifacts: '**/*.jar', followSymlinks: false)
      }
    }
  }
}