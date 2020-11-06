import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

multibranchPipelineJob("${baseFolder}/candlepin") {
    branchSources {
        // upstream; PRs only
        branchSource {
            source {
                github {
                    id('9dff2e67-8a82-415b-afc7-f79fe76795cd') // IMPORTANT: use a constant and unique identifier
                    credentialsId('github-api-token-as-username-password')
                    repoOwner('candlepin')
                    repository('candlepin')
                    repositoryUrl('https://github.com/candlepin/candlepin.git')
                    configuredByUrl(false)
                    traits {
                        gitHubPullRequestDiscovery {
                            strategyId(1)
                        }
                        cleanAfterCheckoutTrait {
                            extension {
                                deleteUntrackedNestedRepositories(true)
                            }
                        }
                    }
                }
            }
        }
    }
    factory {
        workflowBranchProjectFactory {
            scriptPath('jenkins/Jenkinsfile')
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            numToKeep(5)
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('5m')
        }
    }
    // TODO: remove this when issue is resolved > https://issues.jenkins-ci.org/browse/JENKINS-60874
    configure {
        def traits = it / 'sources' / 'data' / 'jenkins.branch.BranchSource' / 'source' / 'traits'
        traits << 'org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait' {
            strategyId(1)
            trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission')
        }
    }
}