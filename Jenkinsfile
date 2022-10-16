pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    durabilityHint('PERFORMANCE_OPTIMIZED')
    disableConcurrentBuilds()
    copyArtifactPermission('Discord Bot Deployment')
  }
  stages{
    stage('build') {
      steps {
        sh '/opt/gradle/gradle-7.4.2/bin/gradle clean build'
      }
    }
    stage('Remove Old Jar'){
       steps{
         sh 'ps -ef | grep cjs-1.jar | awk \'{print $2}\' | xargs kill -9 || true'
        }
    }
    stage('archive') {
       steps {
        archiveArtifacts(artifacts: '**/*.jar', followSymlinks: false)
      }
    }
    stage('Copy Artifacts'){
    steps{
    copyArtifacts(projectName: 'Discord Bot Deployment',selector: specific("${BUILD_NUMBER}"), target:"/discordbot/crazyjavascientist/cjs/")
    }
    }
    stage('Run Jar'){
    steps{
        dir('/discordbot/crazyjavascientist/cjs/build/libs/') {
                sh 'JENKINS_NODE_COOKIE=dontKillMe nohup java -jar -Dspring.profiles.active=server cjs-1.jar &'
            }

    }
    }
  }
}

