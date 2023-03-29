pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    durabilityHint('PERFORMANCE_OPTIMIZED')
    disableConcurrentBuilds()
  }
  node {
      stage 'Clone the project'
      git ' https://github.com/codersage-in/my_website_springboot'

      dir('.') {
          stage("Compilation and Analysis") {
              parallel 'Compilation': {
                  sh "gradle clean build -x test"
              }
          }

          stage("Testing Stage") {
              parallel 'Unit tests': {
                  stage("Running unit tests") {
                      sh "gradle test"
                  }
              }

              stage("Staging") {
                  sh "sudo gradle bootRun -Pargs=spring.profiles.active=server,jasypt.encryptor.password=server-env-key"
              }
          }
      }
  }
}

