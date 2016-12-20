import hudson.plugins.ircbot.v2.IRCConnectionProvider
import groovy.json.StringEscapeUtils

static String REPO = 'https://github.com/candlepin/subscription-manager'

void setCommitStatus(String repo, String sha1, String state, String message) {
    String GITHUB_API_TOKEN_CREDENTIALS_ID = '830050e0-ece9-4878-8b3d-2779cfe76abe'
    try {
        String pattern = "https://github.com/([^/]*)/([^/]*)/?"
        String GITHUB_ORG = (repo =~ pattern)[0][1]
        String GITHUB_REPO = (repo =~ pattern)[0][2]
        withCredentials([string(credentialsId: GITHUB_API_TOKEN_CREDENTIALS_ID, variable: 'GITHUB_API_TOKEN')]) {
            sh """curl "https://api.github.com/repos/$GITHUB_ORG/$GITHUB_REPO/statuses/$sha1?access_token=$GITHUB_API_TOKEN" \\
                    -H "Content-Type: application/json" \\
                    -d "{\\"state\\": \\"${state.toLowerCase()}\\", \\"description\\": \\"${StringEscapeUtils.escapeJavaScript(message)}\\", \\"target_url\\": \\"$BUILD_URL\\", \\"context\\": \\"jenkins-pipeline\\"}" """
        }
    } catch(e) {
        echo "Unable to set status for ${sha1}: ${e}"
        echo "Check that the API token is set as a \"secret text\" credential with ID ${GITHUB_API_TOKEN_CREDENTIALS_ID}"
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
                build(job: "subscription-manager-nose-tests-pr", parameters: [[
                    $class: 'StringParameterValue',
                    name: 'sha1',
                    value: "${sha1}"
                ]])
            },
            'stylish': {
                build(job: "subscription-manager-stylish-tests-pr", parameters: [[
                    $class: 'StringParameterValue',
                    name: 'sha1',
                    value: "${sha1}"
                ]])
            },
            'tito': {
                build(job: "subscription-manager-tito-tests-pr", parameters: [[
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
