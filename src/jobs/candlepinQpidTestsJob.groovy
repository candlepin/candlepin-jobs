import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'

def rhsmJob = job("$baseFolder/candlepin-pullrequest-spectests-qpid") {
    previousNames('candlepin-pullrequest-spectests-qpid')
    environmentVariables(CANDLEPIN_DATABASE: 'postgresql', CP_TEST_ARGS: '-q -r qpid_spec')
    description("Compiles and deploys Candlepin and runs rspec tests against PostgreSQL with Qpid. Does not produce any installable products.")
    label('docker')
    concurrentBuild()
    wrappers {
        timeout {
            noActivity(7200)
        }
        preBuildCleanup()
        colorizeOutput()
        timestamps()
    }
    logRotator{
        daysToKeep(90)
        artifactNumToKeep(-1)
        artifactDaysToKeep(2)
    }
    steps {
        shell readFileFromWorkspace('src/resources/candlepin-rspec-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('artifacts/*')
            allowEmpty()
        }
    }
}

rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.candlepinRepo, 'jenkins-rspec-qpid', false)
