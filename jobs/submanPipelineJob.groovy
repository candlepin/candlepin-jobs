import jobLib.rhsmLib

pipeline_helper = job('subscription-manager pipeline helper') {
    logRotator {
        numToKeep(0)
    }
    steps {
        downstreamParameterized {
            trigger('subscription-manager') {
                parameters {
                    predefinedProp('sha1', '${GIT_COMMIT}')
                }
            }
        }
    }
}

pipeline = pipelineJob('subscription-manager') {
    description('Delivery Pipeline for subscription-manager')
    parameters {
        stringParam('sha1', 'master', 'GIT commit hash of what you want to test.')
    }
    logRotator {
        numToKeep(20)
    }
    steps {
        definition {
            cps {
                script(readFileFromWorkspace('jobs/submanPipeline.groovy'))
            }
        }
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(pipeline_helper, githubOrg, rhsmLib.submanRepo, 'jenkins-pipeline', true, false)
