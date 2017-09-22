import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/CandlepinPerformance") {
    description('This job runs candlepin performance tests')
    label('rhsm')
    wrappers {
        preBuildCleanup()
        configFiles {
            file('candlepinPerformanceInventory') {
                variable('PERF_INVENTORY')
            }
        }
        colorizeOutput()
    }
    properties {
        githubProjectUrl('https://github.com/candlepin/candlepin/')
    }
    parameters {
        stringParam('ghprbActualCommit', 'master', 'name of the candlepin branch to test')
        stringParam('caracalla_branch', 'master', 'name of the caracalla branch to use for the test')
        stringParam('candlepin_throughput_properties', '[\'DURATION_SECONDS=3600\',\'SAMPLES_PER_MINUTE=6900\']', 'override test duration, sample size(small=180, medium = 900, large=6900), and other properties.')
        choiceParam('jmeter_tests', ['candlepin-throughput','loop-over-apis','ImportExport'], 'what test to run')
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
            inventoryPath('${PERF_INVENTORY}')
            credentialsId('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
            additionalParameters('--extra-vars=\"candlepin_branch=${ghprbActualCommit} caracalla_branch=${caracalla_branch} candlepin_throughput_properties=${candlepin_throughput_properties} jmeter_tests=${jmeter_tests}\"')
            colorizedOutput(true)
        }
    }
    publishers {
        archiveArtifacts {
            pattern('ansible/artifacts/*.*')
        }
    }
}
