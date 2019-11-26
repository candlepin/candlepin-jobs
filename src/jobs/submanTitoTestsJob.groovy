import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder

String desc = "Run 'tito build --test --rpm' on github pull requests for subscription-manager\n\n" +
              "This runs against github master branch."

def titoJob = job("$baseFolder/subscription-manager-tito-tests-pr"){
    previousNames("subscription-manager-tito-tests-pr")
    description(desc)
    label('rpmbuild')
    wrappers {
        preBuildCleanup()
        colorizeOutput('css')
    }
    logRotator{
        numToKeep(20)
    }
    steps {
        shell readFileFromWorkspace('src/resources/subscription-manager-tito-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('tito_results.txt')
        }
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(titoJob, githubOrg, rhsmLib.submanRepo, 'jenkins-tito', false)
