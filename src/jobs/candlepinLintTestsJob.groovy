import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'

def rhsmJob = job("$baseFolder/candlepin-pullrequest-lint") {
    previousNames('candlepin-pullrequest-lint')
    description('Runs a linter against Candlepin code, and other miscellaneous checks. Does not produce any installable products.')
    label('docker')
    concurrentBuild()
    wrappers {
        timeout {
            noActivity(600)
        }
        preBuildCleanup()
        colorizeOutput()
    }
    logRotator{
        daysToKeep(90)
        artifactNumToKeep(-1)
        artifactDaysToKeep(2)
    }
    steps {
        shell readFileFromWorkspace('src/resources/candlepin-lint.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('artifacts/*')
            allowEmpty()
        }
    }
}

rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.candlepinRepo, 'jenkins-checkstyle', false)
