pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    durabilityHint('PERFORMANCE_OPTIMIZED')
    disableConcurrentBuilds()
    copyArtifactPermission('Discord Bot Deployment')
  }
  node {
    def remote = [:]
    remote.name = '192.168.3.177'
    remote.host = '192.168.3.177'
    remote.user = 'CYBER\\Antho'
    remote.password = '2205'
    remote.port = '22'
    remote.allowAnyHosts = true
    stage('Remote SSH') {
      sshCommand remote: remote, command: "ls -lrt"
      sshCommand remote: remote, command: "for i in {1..5}; do echo -n \"Loop \$i \"; date ; sleep 1; done"
    }
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
  }
}

