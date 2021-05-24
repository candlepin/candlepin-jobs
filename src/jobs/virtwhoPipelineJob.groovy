import jobLib.rhsmLib

String baseFolder = rhsmLib.virtwhoJobFolder

multibranchPipelineJob("${baseFolder}/virt-who") {
  branchSources {
    // upstream; PRs only
    branchSource {
      source {
        github {
          id('35f9f740-fc4b-11ea-a644-482ae3184260') // IMPORTANT: use a constant and unique identifier
          credentialsId('github-api-token-as-username-password')
          repoOwner('candlepin')
          repository('virt-who')
          repositoryUrl('https://github.com/candlepin/virt-who.git')
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
