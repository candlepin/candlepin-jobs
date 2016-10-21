String submanRepo = 'candlepin/subscription-manager'

// not sure if we want to stick this in a folder yet
//String basePath = 'Scratch'

//folder(basePath) {
//    description 'This is a scratch job.'
//}

//job("$basePath/subscription-manager-nose-tests-pr"){
job("subscription-manager-nose-tests-pr"){
    description('Welcome to the subscription-manager nose tests!')
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
                    context('jenkins-nosetests')
                }
            }
        }
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
//        archiveJunit('nosetests.xml') {
//            healthScaleFactor(1.5)
//        }
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