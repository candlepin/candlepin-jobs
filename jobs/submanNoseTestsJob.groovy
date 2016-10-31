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

rhsmLib.addPullRequester(rhsmJob, rhsmLib.submanRepo, 'jenkins-nosetests')
rhsmLib.addCandlepinNotifier(rhsmJob)