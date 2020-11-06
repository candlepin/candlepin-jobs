package jobLib

import javaposse.jobdsl.dsl.Job

class rhsmLib {
    static String candlepinRepo = "candlepin"
    static String submanRepo = "subscription-manager"
    static String candlepinJobFolder = "candlepin"
    static String submanJobFolder = "subscription-manager"

    static addPullRequester = { Job job, String githubOrg, String repo, String name, boolean trigger = true, boolean notify = true ->
        job.with {
            parameters {
                stringParam('sha1', 'master', 'GIT commit hash of what you want to test.')
            }
            scm {
                git {
                    remote {
                        github("${githubOrg}/${repo}")
                        refspec('+refs/pull/*:refs/remotes/origin/pr/*')
                    }
                    branch('${sha1}')
                }
            }
            if (trigger) {
                triggers {
                    githubPullRequest {
                        onlyTriggerPhrase(false)
                        useGitHubHooks(false)
                        permitAll(false)
                        allowMembersOfWhitelistedOrgsAsAdmin(true)
                        cron('H/5 * * * *')
                        orgWhitelist(githubOrg)
                        extensions {
                            commitStatus {
                                context(name)
                                if (!notify) {
                                    triggeredStatus('--none--')
                                    startedStatus('--none--')
                                    completedStatus('SUCCESS', '--none--')
                                    completedStatus('FAILURE', '--none--')
                                    completedStatus('ERROR', '--none--')
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static addCandlepinNotifier = { Job job  ->
        job.with {
            publishers {
                irc{
                    channel('#candlepin')
                    strategy('FAILURE_AND_FIXED')
                    notificationMessage('Default')
                }
            }
        }
    }
}



