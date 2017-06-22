import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder
String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'

pipeline_helper = job("$baseFolder/candlepin-pipeline-helper") {
    environmentVariables {
        groovy('''
            return [emailDestination: binding.variables.get('ghprbPullAuthorEmail') ?: binding.variables.get('GIT_AUTHOR_EMAIL')]
        ''')
    }
    logRotator {
        numToKeep(1)
    }
    steps {
        downstreamParameterized {
            trigger('candlepin') {
                parameters {
                    predefinedProp('sha1', '${sha1}')
                    predefinedProp('ghprbActualCommit', '${ghprbActualCommit}')
                    predefinedProp('emailDestination', '${emailDestination}')
                    predefinedProp('ghprbTargetBranch', '${ghprbTargetBranch}')
                }
            }
        }
    }
}

pipeline = pipelineJob("$baseFolder/candlepin") {
    description('Delivery Pipeline for candlepin')
    parameters {
        stringParam('sha1', 'master', 'GIT commit hash of what you want to test.')
        stringParam('ghprbActualCommit', null, 'commit used to report status against a GitHub PR')
        stringParam('ghprbTargetBranch', null, 'PR target branch')
        stringParam('emailDestination', null, 'email address to report failures to')
    }
    logRotator {
        numToKeep(20)
    }
    steps {
        definition {
            cps {
                script(readFileFromWorkspace('jobs/candlepinPipeline.groovy'))
            }
        }
    }
}

rhsmLib.addPullRequester(pipeline_helper, githubOrg, rhsmLib.candlepinRepo, 'jenkins-pipeline', true, false)
