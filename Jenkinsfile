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
        sh '/opt/gradle/gradle-7.4.2/bin/gradle clean build'
      }
    }
    stage('Remove Old Jar'){
       steps{
         sh 'ps -ef | grep CrazyJavaScientist-0.0.1-SNAPSHOT.jar | awk \'{print $2}\' | xargs sudo kill -9 || true'
        }
    }
    stage('archive') {
       steps {
        archiveArtifacts(artifacts: '**/*.jar', followSymlinks: false)
      }
    }
    stage('Deploy New Jar'){
       steps{
          sh 'JENKINS_NODE_COOKIE=dontKillMe nohup java -jar -Dspring.profiles.active=server /var/lib/jenkins/jobs/\'Discord Bot Deployment\'/builds/${BUILD_NUMBER}/archive/build/libs/CrazyJavaScientist-0.0.1-SNAPSHOT.jar &'
      }
    }
  }
}

