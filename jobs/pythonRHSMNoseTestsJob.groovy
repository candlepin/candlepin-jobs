import jobLib.rhsmLib

def pyrhsmJob = job("python-rhsm-nose-tests-pr-builder"){
    description('checkout python-rhsm pull requests and run the unit tests')
    label('rhsm')
    wrappers {
        preBuildCleanup()
    }
    logRotator{
        numToKeep(20)
    }
    steps {
        shell readFileFromWorkspace('resources/python-rhsm-nose-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('nosetests.xml')
        }
        publishHtml {
            report('htmlcov/') {
                reportName('Coverage module html report')
            }
        }
    }
}

rhsmLib.addPullRequester(pyrhsmJob, rhsmLib.pythonRHSMRepo, 'jenkins-nose-tests')
rhsmLib.addCandlepinNotifier(pyrhsmJob)