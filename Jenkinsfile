#!groovy

pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    durabilityHint('PERFORMANCE_OPTIMIZED')
    disableConcurrentBuilds()
  }

  stages {

          stage("Compilation and Analysis") {
          steps{
                  sh "./gradlew clean build -x test"
                }
              }


          stage("Running Testing") {
                  steps {
                      sh "./gradlew test"
                  }
          }


              stage("Staging") {
              steps{
                  sh "./gradlew bootRun -Pargs=spring.profiles.active=server,jasypt.encryptor.password=server-env-key"
                  }
              }
          }
}