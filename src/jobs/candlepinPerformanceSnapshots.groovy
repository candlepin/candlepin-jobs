import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/CandlepinPerformanceSnapshotsMaintenance") {
    disabled()
    description('This job runs candlepin maintenance of VM snapshots used for performance tests')
    label('rhsm')
    wrappers {
        preBuildCleanup()
        colorizeOutput()
        timestamps()
    }
    scm {
        git {
            remote {
                url('https://github.com/candlepin/caracalla.git')
            }
            branch '${caracalla_branch}'
        }
    }
    logRotator{
        numToKeep(10)
    }
    triggers {
        cron('H 1 * * 6')
    }
    steps {
        ansiblePlaybook('ansible/maintenance.yml') {
            inventoryPath('/etc/performanceInventory')
            credentialsId('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
            additionalParameters('--extra-vars=\"candlepin_branch=candlepin-2.0-HOTFIX caracalla_branch=${caracalla_branch}\"')
            colorizedOutput(true)
        }
        ansiblePlaybook('ansible/maintenance.yml') {
            inventoryPath('/etc/performanceInventory')
            credentialsId('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
            additionalParameters('--extra-vars=\"candlepin_branch=candlepin-2.1-HOTFIX caracalla_branch=${caracalla_branch}\"')
            colorizedOutput(true)
        }
        ansiblePlaybook('ansible/maintenance.yml') {
            inventoryPath('/etc/performanceInventory')
            credentialsId('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
            additionalParameters('--extra-vars=\"candlepin_branch=candlepin-2.2-HOTFIX caracalla_branch=${caracalla_branch}\"')
            colorizedOutput(true)
        }
    }
}
