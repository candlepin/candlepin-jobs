package jobLib

import javaposse.jobdsl.dsl.Job

class rhsmLib {
    static String candlepinRepo = "candlepin/candlepin"
    static String pythonRHSMRepo = "candlepin/python-rhsm"
    static String submanRepo = "candlepin/subscription-manager"

    static addPullRequester = { Job job, String repo, String name ->
        job.with {
            parameters {
                stringParam('sha1', 'master', 'GIT commit hash of what you want to test.')
            }
            scm {
                git {
                    remote {
                        github(repo)
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
                            context(name)
                        }
                    }
                }
            }
        }
    }

    static addCandlepinNotifier = { Job job  ->
        job.with {
            publishers {
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
    }
}



