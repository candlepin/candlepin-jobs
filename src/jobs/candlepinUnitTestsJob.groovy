import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'

def rhsmJob = job("$baseFolder/candlepin-pullrequest-unittests") {
    previousNames('candlepin-pullrequest-unittests')
    description('Compiles candlepin and runs unit tests. Does not produce any installable products.')
    label('docker')
    concurrentBuild()
    wrappers {
        timeout {
            noActivity(7200)
        }
        preBuildCleanup()
        colorizeOutput()
    }
    logRotator{
        daysToKeep(90)
        artifactNumToKeep(5)
    }
    steps {
        shell readFileFromWorkspace('src/resources/candlepin-unit-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('artifacts/*')
            allowEmpty()
        }
    }
}

rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.candlepinRepo, 'jenkins-candlepin-unittests', false)
