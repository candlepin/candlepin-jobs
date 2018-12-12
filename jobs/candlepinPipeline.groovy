@Library('github.com/candlepin/candlepin-jobs') _

pipeline {
    agent none
    parameters {
        string(name: 'commit', description:'What to test. Use origin/pr/<pr_number>/merge to test a PR')
        booleanParam(name: 'TEST_UNIT_TESTS', defaultValue: true, description: 'Run unit tests')
        booleanParam(name: 'TEST_BZ_REFERENCE', defaultValue: true, description: 'Run BZ reference check')
        booleanParam(name: 'TEST_CHECKSTYLE', defaultValue: true, description: 'Run checkstyle tests')
        booleanParam(name: 'TEST_MYSQL', defaultValue: true, description: 'Run mysql spec tests')
        booleanParam(name: 'TEST_POSTGRES', defaultValue: true, description: 'Run postgres spec tests')
        booleanParam(name: 'TEST_HOSTED_MYSQL', defaultValue: true, description: 'Run hosted mysql spec tests')
        booleanParam(name: 'TEST_HOSTED_POSTGRES', defaultValue: true, description: 'Run hosted postgres spec tests')
        booleanParam(name: 'TEST_QPID', defaultValue: true, description: 'Run qpid spec tests')
    }
    stages {
        stage('Get PR number') {
            script {
                env.pr_number = null
                if ("${commit}" ~== 'origin/pr/.*/merge') {
                    env.pr_number = ("${commit}" ~= 'origin/pr/(.*)/merge')[0][1]
                }
            }
        }
        stage('Get PR Info') {
            steps {
                getPrInfo(repo: 'candlepin', pr_number: env.pr_number)
            }
        }
        stage('Set to pending on GitHub') {
            steps {
                githubStatus(repo: 'candlepin', sha: env.PR_SHA, status: 'PENDING', context: 'jenkins-pipeline', targetUrl: BUILD_URL)
            }
        }
        stage('Run Tests') {
            parallel {
                stage('Unit Tests') {
                    when {
                        expression {
                            return params.TEST_UNIT_TESTS
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-candlepin-unittests', job: "candlepin-pullrequest-unittests", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'sha1',
                            value: "${commit}"
                        ]])
                    }
                }
                stage('Checkstyle') {
                    when {
                        expression {
                            return params.TEST_CHECKSTYLE
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-checkstyle', job: "candlepin-pullrequest-lint", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'sha1',
                            value: "${commit}"
                        ]])
                    }
                }
                stage('Rspec Postgresql') {
                    when {
                        expression {
                            return params.TEST_POSTGRES
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-rspec-postgresql', job: "candlepin-pullrequest-spectests-postgresql", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'sha1',
                            value: "${commit}"
                        ]])
                    }
                }
                stage('Rspec MySQL') {
                    when {
                        expression {
                            return params.TEST_MYSQL
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-rspec-mysql', job: "candlepin-pullrequest-spectests-mysql", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'sha1',
                            value: "${commit}"
                        ]])
                    }
                }
                stage('Rspec Hosted Postgresql') {
                    when {
                        expression {
                            return params.TEST_POSTGRES_HOSTED
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-rspec-hosted-postgres', job: "candlepin-pullrequest-spectests-hosted-postgresql", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'sha1',
                            value: "${commit}"
                        ]])
                    }
                }
                stage('Rspec Hosted Mysql') {
                    when {
                        expression {
                            return params.TEST_MYSQL_HOSTED
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-rspec-hosted-mysql', job: "candlepin-pullrequest-spectests-hosted-mysql", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'sha1',
                            value: "${commit}"
                        ]])
                    }
                }
                stage('Rspec QPID') {
                    when {
                        expression {
                            return params.TEST_QPID
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-rspec-qpid', job: "candlepin-pullrequest-spectests-qpid", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'sha1',
                            value: "${commit}"
                        ]])
                    }
                }
                stage('Check Bugzilla reference') {
                    when {
                        expression {
                            return params.TEST_BZ_REFERENCE
                        }
                        expression {
                            return pr_number != null
                        }
                    }
                    steps {
                        buildWithNotifications(repo: 'candlepin', sha: env.PR_SHA, context: 'jenkins-bugzilla-reference', job: "candlepin-check-bugzilla-reference", parameters: [[
                            $class: 'StringParameterValue',
                            name: 'pr_number',
                            value: "${pr_number}"
                        ]])
                    }
                }
            }
        }
    }
    post {
        success {
            githubStatus(repo: 'candlepin', sha: env.PR_SHA, status: 'SUCCESS', context: 'jenkins-pipeline', targetUrl: BUILD_URL)
        }
        unsuccessful {
            githubStatus(repo: 'candlepin', sha: env.PR_SHA, status: 'FAILURE', context: 'jenkins-pipeline', targetUrl: BUILD_URL)
        }
    }
}