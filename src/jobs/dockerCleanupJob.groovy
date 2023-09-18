job("DockerCleanup"){
    disabled(true)
    description('This job deletes unused docker and podman images across all the worker nodes.')
    label('candlepin')
    parameters {
        labelParam('NODE_LABEL') {
          defaultValue('candlepin')
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
        shell readFileFromWorkspace('src/resources/podman-cleanup.sh')
    }
}
