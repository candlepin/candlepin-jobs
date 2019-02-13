multibranchPipelineJob('Subscription-manager PR Pipeline') {
    branchSources {
        github {
            scanCredentialsId('github-api-token-as-username-password')
            repoOwner('candlepin')
            repository('subscription-manager')
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            numToKeep(20)
        }
    }
}
