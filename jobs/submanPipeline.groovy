import hudson.plugins.ircbot.v2.IRCConnectionProvider
import jenkins.model.Jenkins

REPO = 'https://github.com/candlepin/subscription-manager'
GITHUB_CREDENTIALS_ID = 'github-api-token-as-username-password'
GITHUB_ACCOUNT = 'candlepin'
GITHUB_REPO = 'subscription-manager'
GITHUB_COMMIT = ghprbActualCommit ?: sha1
PENDING_MESSAGE = 'Build has been scheduled.'
SUCCESS_MESSAGE = 'Test(s) passed.'
FAILURE_MESSAGE = 'Test(s) failed.'

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
        'nose': {
            results.add(buildWithNotifications(context: 'nose', job: "subscription-manager-nose-tests-pr", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'stylish': {
            results.add(buildWithNotifications(context: 'stylish', job: "subscription-manager-stylish-tests-pr", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'tito': {
            results.add(buildWithNotifications(context: 'tito', job: "subscription-manager-tito-tests-pr", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'python-rhsm-python3': {
            results.add(buildWithNotifications(context: 'python-rhsm-python3', job: "python-rhsm-python3-tests-pr-builder", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'opensuse42-build': {
            results.add(buildWithNotifications(context: 'opensuse42-build', job: "subscription-manager-suse-build-opensuse42", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'sles11-build': {
            results.add(buildWithNotifications(context: 'sles11-build', job: "subscription-manager-suse-build-sles11", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'sles12-build': {
            results.add(buildWithNotifications(context: 'sles12-build', job: "subscription-manager-suse-build-sles12", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'opensuse42-test': {
            results.add(buildWithNotifications(context: 'opensuse42-test', job: "subscription-manager-suse-opensuse42-tests", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
        'sles12-test': {
            results.add(buildWithNotifications(context: 'sles12-test', job: "subscription-manager-suse-sles12-tests", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]]))
        },
    )
    node('master') {
        def buildPassed = !results.contains('FAILURE') && !results.contains('ABORTED') && !results.contains('UNSTABLE')
        if (!buildPassed) {
            currentBuild.result = 'FAILURE'
            githubStatus(status: 'FAILURE', context: 'jenkins-pipeline', targetUrl: BUILD_URL)

            sendIrcNotification('#candlepin', "subscription-manager build # ${env.BUILD_NUMBER} failed. See ${env.BUILD_URL} for details.")
        }
        else {
            githubStatus(status: 'SUCCESS', context: 'jenkins-pipeline', targetUrl: BUILD_URL)
        }
    }
}
