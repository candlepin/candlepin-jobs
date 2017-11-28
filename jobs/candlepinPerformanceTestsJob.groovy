import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/CandlepinPerformance") {
    disabled()
    description('This job runs candlepin performance tests')
    label('rhsm')
    wrappers {
        preBuildCleanup()
        colorizeOutput()
        timestamps()
    }
    properties {
        githubProjectUrl('https://github.com/candlepin/candlepin/')
    }
    parameters {
        stringParam('ghprbActualCommit', 'master', 'name of the candlepin branch to test')
        stringParam('caracalla_branch', 'master', 'name of the caracalla branch to use for the test')
        stringParam('candlepin_throughput_properties', '[\'DURATION_SECONDS=3600\',\'SAMPLES_PER_MINUTE=6900\']', 'override test duration, sample size(small=180, medium = 900, large=6900), and other properties.')
        stringParam('loop_over_apis_properties', '[\'SCALE=1000\']', 'override test scale or other specific loop counts.')
        choiceParam('jmeter_tests', ['candlepin-throughput','loop-over-apis','ImportExport'], 'what test to run')
        choiceParam('target_branch', ['master', 'candlepin-2.0-HOTFIX','candlepin-2.1-HOTFIX'], 'what is target branch of PR (influences snapshot of database VM use for testing)')
        booleanParam('keep_logs', false, 'Check this to keep candlepin.log and access.log')
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
        cron('H 3 * * 6')
    }
    steps {
        ansiblePlaybook('ansible/candlepin.yml') {
            inventoryPath('/etc/performanceInventory')
            credentialsId('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
            additionalParameters('--extra-vars=\"candlepin_branch=${ghprbActualCommit} caracalla_branch=${caracalla_branch} candlepin_throughput_properties=${candlepin_throughput_properties} loop_over_apis_properties=${loop_over_apis_properties} jmeter_tests=${jmeter_tests} target_branch=${target_branch} keep_logs=${keep_logs}\"')
            colorizedOutput(true)
        }
    }
    publishers {
        archiveArtifacts {
            pattern('ansible/artifacts/*.*')
        }
    }
}
