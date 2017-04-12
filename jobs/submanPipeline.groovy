import hudson.plugins.ircbot.v2.IRCConnectionProvider
import groovy.json.StringEscapeUtils

String REPO = 'https://github.com/candlepin/subscription-manager'
String GITHUB_CREDENTIALS_ID = 'github-api-token-as-username-password'
String GITHUB_ACCOUNT = 'candlepin'
String GITHUB_REPO = 'subscription-manager'
String GITHUB_COMMIT = ghprbActualCommit ?: sha1
String PENDING_MESSAGE = 'Build has been scheduled.'
String SUCCESS_MESSAGE = 'Test(s) passed.'
String FAILURE_MESSAGE = 'Test(s) failed.'

def sendIrcNotification = { channel, text ->
    try {
        IRCConnectionProvider.getInstance().currentConnection().send(channel, text)
    } catch(e) {
        echo "Unable to send IRC notification to ${channel}: ${e}"
    }
}

stage('test') {
    node('master') {
        githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-pipeline', targetUrl: BUILD_URL)
    }
    def results = []
    parallel(
        'nose': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                        status: 'PENDING', description: PENDING_MESSAGE, context: 'nose', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "subscription-manager-nose-tests-pr", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'nose', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'nose', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'stylish': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                        status: 'PENDING', description: PENDING_MESSAGE, context: 'stylish', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "subscription-manager-stylish-tests-pr", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'stylish', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'stylish', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'tito': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                      status: 'PENDING', description: PENDING_MESSAGE, context: 'tito', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "subscription-manager-tito-tests-pr", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'tito', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'tito', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'python-rhsm-python3': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                      status: 'PENDING', description: PENDING_MESSAGE, context: 'python-rhsm-python3', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "python-rhsm-python3-tests-pr-builder", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'python-rhsm-python3', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'python-rhsm-python3', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
    )
    node('master') {
        def buildPassed = !results.contains('FAILURE') && !results.contains('ABORTED') && !results.contains('UNSTABLE')
        if (!buildPassed) {
            currentBuild.result = 'FAILURE'
            githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                    status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-pipeline', targetUrl: BUILD_URL)

            sendIrcNotification('#candlepin', "subscription-manager build # ${env.BUILD_NUMBER} failed. See ${env.BUILD_URL} for details.")
        }
        else {
            githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                    status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-pipeline', targetUrl: BUILD_URL)
        }
    }
}
