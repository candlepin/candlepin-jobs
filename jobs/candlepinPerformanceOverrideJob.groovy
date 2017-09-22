import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/CandlepinPerformanceOverride") {
    description('Job that watches for performance OK by candlepin org members and marks a PR as PASS for performance. Use responsibly')
    parameters {
        stringParam('sha1', null, 'sha1 of commit to PASS performance for')
    }
    label('rhsm')
    scm {
        git {
            remote {
                github('candlepin/candlepin')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }
    triggers {
        githubPullRequest {
            triggerPhrase('performance ok')
            onlyTriggerPhrase(true)
            useGitHubHooks(false)
            permitAll(false)
            allowMembersOfWhitelistedOrgsAsAdmin(true)
            cron('H/5 * * * *')
            orgWhitelist('candlepin')
            extensions {
                commitStatus {
                    context('jenkins-candlepin-performance')
                    triggeredStatus('--none--')
                    startedStatus('--none--')
                    completedStatus('SUCCESS', 'Marked as PASS via GitHub comment.')
                    completedStatus('FAILURE', '--none--')
                    completedStatus('ERROR', '--none--')
                }
            }
        }
    }
}
