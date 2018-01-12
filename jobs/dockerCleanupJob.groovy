String listOfSlaves(int start, int end) {
  String slaveList = ""
  for (i = start; i <= end; i++) {
    slaveList = slaveList + "rhsm-jenkins-slave${i}.usersys.redhat.com"
    if ( i < end ) {
      slaveList = slaveList + ","
    }
  }
  return slaveList
}

job("DockerCleanup"){
    description('This job deletes unused docker images across all the slave nodes.')
    label('rhsm')
    parameters {
        stringParam('DOCKER_HOSTS', listOfSlaves(0,7), 'Hostnames of servers to run on.')
        booleanParam('RESTART_DOCKER', false, 'Restart docker afterwards if checked.')
    }
    wrappers {
        sshAgent('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
    }
    logRotator{
        numToKeep(10)
    }
    triggers {
        // cron('H 0 * * 6')
        cron('H 20 * * *')
    }
    steps {
        shell readFileFromWorkspace('resources/docker-cleanup.sh')
    }
}
