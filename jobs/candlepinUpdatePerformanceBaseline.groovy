import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/CandlepinUpdatePerformanceBaseline") {
    description('This job updates the performance test baseline data.')
    label('rhsm')
    wrappers {
        preBuildCleanup()
        colorizeOutput()
    }
    parameters {
        stringParam('caracalla_branch', 'master', 'name of the caracalla branch to use for the test')
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
    steps {
        ansiblePlaybook('ansible/baseline_maintenance.yml') {
            inventoryPath('/etc/performanceInventory')
            credentialsId('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
            additionalParameters('--extra-vars=\"caracalla_branch=${caracalla_branch}\"')
            colorizedOutput(true)
        }
    }
}
