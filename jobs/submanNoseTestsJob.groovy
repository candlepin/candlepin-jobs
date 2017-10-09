import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder

def rhsmJob = job("$baseFolder/subscription-manager-nose-tests-pr"){
    previousNames("subscription-manager-nose-tests-pr")
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
        shell readFileFromWorkspace('resources/python-rhsm-nose-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('nosetests.xml')
            pattern('coverage.xml')
            pattern('python-rhsm/nosetests.xml')
        }
        publishHtml {
            report('cover/') {
                reportName('Coverage module html report')
            }
            report('python-rhsm/htmlcov/') {
                reportName('python-rhsm coverage module html report')
                allowMissing()
            }
            report('htmlcov/') {
                reportName('python-rhsm coverage module html report')
                allowMissing()
            }
        }
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, 'jenkins-nosetests', false)
