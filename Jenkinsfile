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
                  sh "gradle clean build -x test"
                }
              }


          stage("Running Testing") {
                  steps {
                      sh "gradle test"
                  }
          }


              stage("Staging") {
              steps{
                  sh "sudo gradle bootRun -Pargs=spring.profiles.active=server,jasypt.encryptor.password=server-env-key"
                  }
              }
          }
}