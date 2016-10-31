import jobLib.rhsmLib

String desc = "Run 'tito build --test --rpm' on github pull requests for subscription-manager\n\n" +
              "This runs against github master branch."

def titoJob = job("subscription-manager-tito-tests-pr"){
    description(desc)
    label('rhsm')
    wrappers {
        preBuildCleanup()
        colorizeOutput('css')
    }
    logRotator{
        numToKeep(20)
    }
    steps {
        shell readFileFromWorkspace('resources/subscription-manager-tito-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('tito_results.txt')
        }
    }
}

rhsmLib.addPullRequester(titoJob, rhsmLib.submanRepo, 'jenkins-tito')
rhsmLib.addCandlepinNotifier(titoJob)