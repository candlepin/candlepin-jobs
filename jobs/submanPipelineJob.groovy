import jobLib.rhsmLib
String baseFolder = rhsmLib.submanJobFolder

pipeline_helper = job("$baseFolder/subscription-manager-pipeline-helper") {
    logRotator {
        numToKeep(1)
    }
    steps {
        downstreamParameterized {
            trigger('subscription-manager') {
                parameters {
                    predefinedProp('sha1', '${sha1}')
                    predefinedProp('ghprbActualCommit', '${ghprbActualCommit}')
                    predefinedProp('ghprbPullId', '${ghprbPullId}')
                }
            }
        }
    }
}

pipeline = pipelineJob("$baseFolder/subscription-manager") {
    description('Delivery Pipeline for subscription-manager')
    parameters {
        stringParam('sha1', 'master', 'GIT commit hash of what you want to test.')
        stringParam('ghprbActualCommit', null, 'commit used to report status against a GitHub PR')
        stringParam('ghprbPullId', null, 'PR number (used in certain checks)')
    }
    logRotator {
        numToKeep(20)
    }
    definition {
        cps {
            script(readFileFromWorkspace('jobs/submanPipeline.groovy'))
        }
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(pipeline_helper, githubOrg, rhsmLib.submanRepo, 'jenkins-pipeline', true, false)
