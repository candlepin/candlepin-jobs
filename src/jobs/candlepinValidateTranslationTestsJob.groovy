import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'

def rhsmJob = job("$baseFolder/candlepin-pullrequest-validate-translation") {
    previousNames('candlepin-pullrequest-validate-translation')
    description('Runs a validator on translated files against Candlepin code')
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
        shell readFileFromWorkspace('src/resources/candlepin-validate-text.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('artifacts/*')
            allowEmpty()
        }
    }
}

rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.candlepinRepo, 'jenkins-validate-translation', false)
