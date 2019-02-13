job("DockerCleanup"){
    description('This job deletes unused docker images across all the slave nodes.')
    label('rhsm')
    parameters {
        labelParam('NODE_LABEL') {
          defaultValue('rhsm')
          description('Select nodes')
          allNodes('allCases', 'IgnoreOfflineNodeEligibility')
        }
        booleanParam('RESTART_DOCKER', false, 'Restart docker afterwards if checked.')
        booleanParam('CLEAR_BUILD_CACHE', false, 'Clear the docker build cache if checked.')
    }
    logRotator{
        numToKeep(10)
    }
    triggers {
        // cron('H 0 * * 6')
        cron('H 20 * * *')
    }
    steps {
        shell readFileFromWorkspace('src/resources/docker-cleanup.sh')
    }
}
