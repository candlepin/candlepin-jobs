import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder

def pyrhsmJob = job("$baseFolder/python-rhsm-python3-tests-pr-builder"){
    previousNames("python-rhsm-python3-tests-pr-builder")
    description("checkout python-rhsm PR, make sure we don't regress on Python 3 compatibility")
    label('rhsm')
    wrappers {
        preBuildCleanup()
    }
    logRotator{
        numToKeep(20)
    }
    steps {
        shell readFileFromWorkspace('resources/python-rhsm-python3-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('python-rhsm/nosetests.xml')
        }
        publishHtml {
            report('python-rhsm/htmlcov/') {
                reportName('Coverage module html report')
            }
        }
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(pyrhsmJob, githubOrg, rhsmLib.submanRepo, 'python-rhsm-python3', false, false)
rhsmLib.addCandlepinNotifier(pyrhsmJob)
