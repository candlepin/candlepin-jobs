job("Candlepin Performance"){
    description('This job runs candlepin performance tests')
    parameters {
        stringParam('candlepin_branch', 'master', 'name of the candlepin branch to test')
        stringParam('caracalla_branch', 'master', 'name of the caracalla branch to use for the test')
        stringParam('test_duration', '600')
        stringParam('sample_rate', '1800')
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
            additionalParameters(' --extraVars = \"candlepin_branch: \'${candlepin_branch}\'')
        }
    }
}
