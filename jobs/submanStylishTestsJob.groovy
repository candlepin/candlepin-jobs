String submanRepo = 'candlepin/subscription-manager'
String desc = "Run 'make stylish' on github pull requests for subscription-manager.\n\n" +
              "This runs against pull request branches."

job("subscription-manager-stylish-tests-pr"){
    description(desc)
    label('rhsm')
    wrappers {
        preBuildCleanup()
        colorizeOutput('css')
    }
    logRotator{
        numToKeep(20)
    }
    parameters {
        stringParam('sha1', 'master', 'GIT commit hash of what you want to test.')
    }
    scm {
        git {
            remote{
                github(submanRepo)
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }
    triggers {
        githubPullRequest {
            onlyTriggerPhrase(false)
            useGitHubHooks(false)
            permitAll(false)
            allowMembersOfWhitelistedOrgsAsAdmin(true)
            cron('H/5 * * * *')
            orgWhitelist('candlepin')
            extensions {
                commitStatus {
                    context('jenkins-stylish')
                }
            }
        }
    }
    steps {
        shell readFileFromWorkspace('resources/subscription-manager-stylish-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('stylish_results.txt')
        }
        extendedEmail {
            recipientList('chainsaw@redhat.com')
        }
        irc{
            channel('#candlepin')
            strategy('FAILURE_AND_FIXED')
            notificationMessage('Default')
        }
    }
}