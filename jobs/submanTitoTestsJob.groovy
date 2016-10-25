String submanRepo = 'candlepin/subscription-manager'
String desc = "Run 'tito build --test --rpm' on github pull requests for subscription-manager\n\n" +
              "This runs against github master branch."

job("subscription-manager-tito-tests-pr"){
    description(desc)
    label('rhsm')
    wrappers {
        preBuildCleanup()
        colorizeOutput('css')
    }
    logRotator{
        numToKeep(20)
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
    parameters {
        stringParam('sha1', 'master', 'GIT commit hash of what you want to test.')
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
        shell readFileFromWorkspace('resources/subscription-manager-tito-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('tito_results.txt')
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