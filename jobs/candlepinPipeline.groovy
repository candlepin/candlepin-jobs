import hudson.plugins.ircbot.v2.IRCConnectionProvider
import jenkins.model.Jenkins

REPO = 'https://github.com/candlepin/candlepin'
GITHUB_CREDENTIALS_ID = 'github-api-token-as-username-password'
GITHUB_ACCOUNT = 'candlepin'
GITHUB_REPO = 'candlepin'
GITHUB_COMMIT = ghprbActualCommit ?: sha1
PENDING_MESSAGE = 'Build has been scheduled.'
SUCCESS_MESSAGE = 'Test(s) passed.'
FAILURE_MESSAGE = 'Test(s) failed.'
PERFORMANCE_BRANCH_BLACKLIST = [
    '0.9.23-hotfix',
    'candlepin-0.9.49-HOTFIX',
    'candlepin-0.9.51-HOTFIX',
    'candlepin-0.9.54-HOTFIX',
]
QPID_BRANCH_BLACKLIST = PERFORMANCE_BRANCH_BLACKLIST // they're the same at the moment
STATUS_MESSAGE_MAP = [
    'PENDING': PENDING_MESSAGE,
    'SUCCESS': SUCCESS_MESSAGE,
    'FAILURE': FAILURE_MESSAGE,
]

def sendIrcNotification(channel, text) {
    try {
        IRCConnectionProvider.getInstance().currentConnection().send(channel, text)
    } catch(e) {
        echo "Unable to send IRC notification to ${channel}: ${e}"
    }
}

def githubStatus(Map args) {
    description = args.get('description') ?: STATUS_MESSAGE_MAP[args.status]
    try {
        githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                status: args.status, description: description, context: args.context, targetUrl: args.targetUrl)
    } catch (e) {
        echo "Unable to update GitHub status (commit: ${GITHUB_COMMIT}, status: ${args.status}, context: ${args.context})"
    }
}

def buildWithNotifications(Map args) {
    boolean enabled = false
    try {
        enabled = Jenkins.instance.getItem(args.job, currentBuild.rawBuild.parent, Item.class).buildable
    } catch (e) {
        echo "Unable to get job status for ${args.job}; skipping build. Does the job exist?"
        return 'SUCCESS'
    }
    if (!enabled) {
        echo "Skipping ${args.job} as it is disabled."
        return 'SUCCESS'
    }
    githubStatus(status: 'PENDING', context: args.context, targetUrl: BUILD_URL)
    def buildInstance = build(job: args.job, parameters: args.parameters, propagate: false)
    node('master') {
        if (buildInstance.result == 'SUCCESS') {
            githubStatus(status: 'SUCCESS', context: args.context, targetUrl: buildInstance.absoluteUrl)
        }
        else {
            githubStatus(status: 'FAILURE', context: args.context, targetUrl: buildInstance.absoluteUrl)
        }
    }
    return buildInstance.result
}

stage('test') {
    node('master') {
        githubStatus(status: 'PENDING', context: 'jenkins-pipeline', targetUrl: BUILD_URL)
    }
    def results = []
    parallel(
        'unit-tests': {
            results.add(buildWithNotifications(context: 'jenkins-candlepin-unittests', job: "candlepin-pullrequest-unittests", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'lint': {
            results.add(buildWithNotifications(context: 'jenkins-checkstyle', job: "candlepin-pullrequest-lint", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'performance': {
            if (!PERFORMANCE_BRANCH_BLACKLIST.contains(ghprbTargetBranch)) {
                results.add(buildWithNotifications(context: 'jenkins-candlepin-performance', job: "CandlepinPerformance", parameters: [[
                     $class: 'StringParameterValue',
                     name  : 'ghprbActualCommit',
                     value : "${sha1}"
                ]]))
            }
        },
        'rspec-postgres': {
            results.add(buildWithNotifications(context: 'jenkins-rspec-postgresql', job: "candlepin-pullrequest-spectests-postgresql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'rspec-mysql': {
            results.add(buildWithNotifications(context: 'jenkins-rspec-mysql', job: "candlepin-pullrequest-spectests-mysql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'rspec-hosted-postgres': {
            results.add(buildWithNotifications(context: 'jenkins-rspec-hosted-postgres', job: "candlepin-pullrequest-spectests-hosted-postgresql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'jenkins-rspec-hosted-mysql': {
            results.add(buildWithNotifications(context: 'jenkins-rspec-hosted-mysql', job: "candlepin-pullrequest-spectests-hosted-mysql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'jenkins-rspec-qpid': {
            if (!QPID_BRANCH_BLACKLIST.contains(ghprbTargetBranch)) {
                results.add(buildWithNotifications(context: 'jenkins-rspec-qpid', job: "candlepin-pullrequest-spectests-qpid", parameters: [[
                    $class: 'StringParameterValue',
                    name: 'sha1',
                    value: "${sha1}"
                ]]))
            }
        },
    )
    node('master') {
        def buildPassed = !results.contains('FAILURE') && !results.contains('ABORTED') && !results.contains('UNSTABLE')
        if (!buildPassed) {
            currentBuild.result = 'FAILURE'
            githubStatus(status: 'FAILURE', context: 'jenkins-pipeline', targetUrl: BUILD_URL)

            String subject = "candlepin - build # ${env.BUILD_NUMBER} - Failure"
            emailext(
                subject: subject,
                body: "${subject}:\n\nCheck console output at ${env.BUILD_URL} to view the results",
                to: emailDestination,
            )
            try {
              branch = "${env.GIT_BRANCH}"
              pr = branch.split('/')[2]
              sendIrcNotification('#candlepin', "tests failed for pull request: ${pr}. See https://github.com/candlepin/candlepin/pull/${pr} for details.")
            } catch(e) {
              sendIrcNotification('#candlepin', "candlepin build # ${env.BUILD_NUMBER} failed. See ${env.BUILD_URL} for details.")
            }
        }
        else {
            githubStatus(status: 'SUCCESS', context: 'jenkins-pipeline', targetUrl: BUILD_URL)
        }
    }
}
