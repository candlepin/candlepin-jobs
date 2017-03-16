import hudson.plugins.ircbot.v2.IRCConnectionProvider

String REPO = 'https://github.com/candlepin/candlepin'
String GITHUB_CREDENTIALS_ID = 'github-api-token-as-username-password'
String GITHUB_ACCOUNT = 'candlepin'
String GITHUB_REPO = 'candlepin'
String GITHUB_COMMIT = ghprbActualCommit ?: sha1
String PENDING_MESSAGE = 'Build has been scheduled.'
String SUCCESS_MESSAGE = 'Test(s) passed.'
String FAILURE_MESSAGE = 'Test(s) failed.'
String[] PERFORMANCE_BRANCH_BLACKLIST = [
    '0.9.23-hotfix',
    'candlepin-0.9.49-HOTFIX',
    'candlepin-0.9.51-HOTFIX',
    'candlepin-0.9.54-HOTFIX',
]

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
        'unit-tests': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                        status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-candlepin-unittests', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "candlepin-pullrequest-unittests", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-candlepin-unittests', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-candlepin-unittests', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'lint': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                        status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-checkstyle', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "candlepin-pullrequest-lint", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-checkstyle', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-checkstyle', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'performance': {
            if (!PERFORMANCE_BRANCH_BLACKLIST.contains(ghprbTargetBranch)) {
                node('master') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-candlepin-performance', targetUrl: BUILD_URL)
                }
                def buildInstance = build(job: "Candlepin Performance", parameters: [[
                     $class: 'StringParameterValue',
                     name  : 'ghprbActualCommit',
                     value : "${sha1}"
                ]], propagate: false)
                node('master') {
                    if (buildInstance.result == 'SUCCESS') {
                        githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                                status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-candlepin-performance', targetUrl: buildInstance.absoluteUrl)
                    } else {
                        githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                                status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-candlepin-performance', targetUrl: buildInstance.absoluteUrl)
                    }
                }
                results.add(buildInstance.result)
            }
        },
        'rspec-postgres': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                      status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-rspec-postgresql', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "candlepin-pullrequest-spectests-postgresql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-rspec-postgresql', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-rspec-postgresql', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'rspec-mysql': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                      status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-rspec-mysql', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "candlepin-pullrequest-spectests-mysql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-rspec-mysql', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-rspec-mysql', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'rspec-hosted-postgres': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                      status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-rspec-hosted-postgres', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "candlepin-pullrequest-spectests-hosted-postgresql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-rspec-hosted-postgres', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-rspec-hosted-postgres', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'jenkins-rspec-hosted-mysql': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                      status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-rspec-hosted-mysql', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "candlepin-pullrequest-spectests-hosted-mysql", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-rspec-hosted-mysql', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-rspec-hosted-mysql', targetUrl: buildInstance.absoluteUrl)
                }
            }
            results.add(buildInstance.result)
        },
        'jenkins-rspec-qpid': {
            node('master') {
                githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                      status: 'PENDING', description: PENDING_MESSAGE, context: 'jenkins-rspec-qpid', targetUrl: BUILD_URL)
            }
            def buildInstance = build(job: "candlepin-pullrequest-spectests-qpid", parameters: [[
                $class: 'StringParameterValue',
                name: 'sha1',
                value: "${sha1}"
            ]], propagate: false)
            node('master') {
                if (buildInstance.result == 'SUCCESS') {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-rspec-qpid', targetUrl: buildInstance.absoluteUrl)
                }
                else {
                    githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                            status: 'FAILURE', description: FAILURE_MESSAGE, context: 'jenkins-rspec-qpid', targetUrl: buildInstance.absoluteUrl)
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

            String subject = "candlepin - build # ${env.BUILD_NUMBER} - Failure"
            emailext(
                subject: subject,
                body: "${subject}:\n\nCheck console output at ${env.BUILD_URL} to view the results",
                to: emailDestination,
            )
            sendIrcNotification('#candlepin', "candlepin build # ${env.BUILD_NUMBER} failed. See ${env.BUILD_URL} for details.")
        }
        else {
            githubNotify(account: GITHUB_ACCOUNT, repo: GITHUB_REPO, credentialsId: GITHUB_CREDENTIALS_ID, sha: GITHUB_COMMIT,
                    status: 'SUCCESS', description: SUCCESS_MESSAGE, context: 'jenkins-pipeline', targetUrl: BUILD_URL)
        }
    }
}
