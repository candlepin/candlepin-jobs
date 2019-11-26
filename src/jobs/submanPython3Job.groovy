import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder

def job = job("$baseFolder/subscription-manager-python3-tests-pr-builder"){
    previousNames("$baseFolder/python-rhsm-python3-tests-pr-builder")
    description("checkout subscription-manager PR, make sure we don't regress on Python 3 compatibility")
    label('subman')
    wrappers {
        preBuildCleanup()
        timeout {
            absolute(5)
        }
    }
    logRotator{
        numToKeep(20)
    }
    steps {
        shell readFileFromWorkspace('src/resources/subscription-manager-python3-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('nosetests.xml')
        }
        publishHtml {
            report('htmlcov/') {
                reportName('Coverage module html report')
                allowMissing()
            }
        }
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(job, githubOrg, rhsmLib.submanRepo, 'subscription-manager-python3', false, false)
rhsmLib.addCandlepinNotifier(job)
