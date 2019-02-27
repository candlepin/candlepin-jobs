job("WsCleanup"){
    description('This job deletes leftover ws-cleanup files across all the slave nodes.')
    parameters {
        labelParam('NODE_LABEL') {
            defaultValue('rhsm')
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
        shell readFileFromWorkspace('resources/ws-cleanup.sh')
    }
}
