import jobLib.rhsmLib

def rhsmJob = job("subscription-manager-nose-tests-pr"){
    description('Welcome to the subscription-manager nose tests!')
    label('rhsm')
    wrappers {
        preBuildCleanup()
        colorizeOutput('css')
    }
    logRotator{
        numToKeep(20)
    }
    steps {
        shell readFileFromWorkspace('resources/subscription-manager-nose-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('nosetests.xml')
            pattern('coverage.xml')
        }
        publishHtml {
            report('cover/') {
                reportName('Coverage module html report')
            }
        }
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, 'jenkins-nosetests', false)
