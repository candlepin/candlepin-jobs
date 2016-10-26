String pythonRHSMRepo = 'candlepin/python-rhsm'

job("python-rhsm-nose-tests-pr-builder"){
    description('checkout python-rhsm pull requests and run the unit tests')
    label('rhsm')
    wrappers {
        preBuildCleanup()
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
                github(pythonRHSMRepo)
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
                    context('jenkins-nose-tests')
                }
            }
        }
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