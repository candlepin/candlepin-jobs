import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/CandlepinPerformanceOverride") {
    description('By running this job, the user marks a PR as PASS for performance. Use responsibly')
    label('rhsm')
    parameters {
        stringParam('sha1', 'sha1 of commit to PASS performance for')
    }
    wrappers {
        environmentVariables {
            groovy('[BUILD_USER: currentBuild.getCause(hudson.model.Cause.UserIdCause.class).userId]')
        }
    }
    configure { project ->
        project / publishers << 'org.jenkinsci.plugins.github.status.GitHubCommitStatusSetter' {
            commitShaSource(class:'org.jenkinsci.plugins.github.status.sources.ManuallyEnteredShaSource') {
                sha('${sha1}')
            }
            reposSource(class:'org.jenkinsci.plugins.github.status.sources.ManuallyEnteredRepositorySource') {
                url('https://github.com/candlepin/candlepin')
            }
            contextSource(class:'org.jenkinsci.plugins.github.status.sources.ManuallyEnteredCommitContextSource') {
                context('jenkins-candlepin-performance')
            }
            statusResultSource(class:'org.jenkinsci.plugins.github.status.sources.ConditionalStatusResultSource') {
                results {
                    'org.jenkinsci.plugins.github.status.sources.misc.AnyBuildResult' {
                        state('SUCCESS')
                        message('Marked as PASS by ${BUILD_USER}')
                    }
                }
            }
            statusBackrefSource(class: 'org.jenkinsci.plugins.github.status.sources.BuildRefBackrefSource')
            errorHandlers()
        }
    }
}
