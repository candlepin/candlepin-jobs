import hudson.plugins.ircbot.v2.IRCConnectionProvider

String REPO = 'https://github.com/candlepin/subscription-manager'

def setCommitStatus = { repo, sha1, state, message ->
    try {
        step([
            $class: 'GitHubCommitStatusSetter',
            commitShaSource: [
                $class: 'ManuallyEnteredShaSource',
                sha   : sha1,
            ],
            contextSource: [
                $class: 'ManuallyEnteredCommitContextSource',
                context: 'jenkins-pipeline',
            ],
            reposSource: [
                $class: 'ManuallyEnteredRepositorySource',
                url   : repo,
            ],
            statusResultSource: [
                $class : 'ConditionalStatusResultSource',
                results: [[
                    $class : 'AnyBuildResult',
                    message: message,
                    state  : state,
                ]]
            ],
        ])
    } catch(e) {
        echo "Unable to set status for ${sha1}: ${e}"
    }
}

def sendIrcNotification = { channel, text ->
    try {
        IRCConnectionProvider.getInstance().currentConnection().send(channel, text)
    } catch(e) {
        echo "Unable to send IRC notification to ${channel}: ${e}"
    }
}

stage('test') {
    node('master') {
        setCommitStatus(REPO, "${sha1}", 'Pending', 'Tests are pending.')
    }
    try {
        parallel(
            'nose': {
                build(job: 'subscription-manager-nose-tests-pr', parameters: [[
                    $class: 'StringParameterValue',
                    name: 'sha1',
                    value: "${sha1}"
                ]])
            },
            'stylish': {
                build(job: 'subscription-manager-stylish-tests-pr', parameters: [[
                    $class: 'StringParameterValue',
                    name: 'sha1',
                    value: "${sha1}"
                ]])
            },
            'tito': {
                build(job: 'subscription-manager-tito-tests-pr', parameters: [[
                    $class: 'StringParameterValue',
                    name: 'sha1',
                    value: "${sha1}"
                ]])
            },
        )
        node('master') {
            currentBuild.result = 'SUCCESS'
            setCommitStatus(REPO, "${sha1}", 'SUCCESS', 'All tests passed.')
        }
    } catch(e) {
        node('master') {
            currentBuild.result = 'FAILURE'
            setCommitStatus(REPO, "${sha1}", 'FAILURE', 'One or more tests failed.')

            String subject = "subscription-manager - build # ${env.BUILD_NUMBER} - Failure"
            emailext(
                    subject: subject,
                    body: "${subject}:\n\nCheck console output at ${env.BUILD_URL} to view the results",
                    to: 'chainsaw@redhat.com',
            )
            sendIrcNotification('#candlepin', "subscription-manager build # ${env.BUILD_NUMBER} failed. See ${env.BUILD_URL} for details.")
        }
    }
}