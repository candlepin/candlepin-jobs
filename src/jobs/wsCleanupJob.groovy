job("WsCleanup"){
    description('This job deletes leftover ws-cleanup files across all the worker nodes.')
    parameters {
        labelParam('NODE_LABEL') {
            defaultValue('candlepin')
            description('Select nodes')
            allNodes('allCases', 'IgnoreOfflineNodeEligibility')
        }
    }
    logRotator{
        numToKeep(10)
    }
    triggers {
        cron('H 20 * * *')
    }
    steps {
        shell readFileFromWorkspace('src/resources/ws-cleanup.sh')
    }
}
