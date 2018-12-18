def call(Map args) {
    ['job', 'repo', 'sha', 'context', 'parameters'].each { arg ->
        if (!args.containsKey(arg)) {
            throw new IllegalArgumentException("Missing ${arg} in call to buildWithNotifications")
        }
    }

    try {
        enabled = jenkins.model.Jenkins.instance.getItem(args.job, currentBuild.rawBuild.parent, Item.class).buildable
    } catch (e) {
        echo "Unable to get job status for ${args.job}; skipping build. Does the job exist?"
        return 'SUCCESS'
    }
    if (!enabled) {
        echo "Skipping ${args.job} as it is disabled."
        return 'SUCCESS'
    }
    githubStatus(repo: args.repo, status: 'PENDING', context: args.context, targetUrl: BUILD_URL, sha: args.sha)
    def buildInstance = build(job: args.job, parameters: args.parameters, propagate: false)
    node('master') {
        if (buildInstance.result == 'SUCCESS') {
            githubStatus(repo: args.repo, status: 'SUCCESS', context: args.context, targetUrl: buildInstance.absoluteUrl)
        }
        else {
            githubStatus(repo: args.repo, status: 'FAILURE', context: args.context, targetUrl: buildInstance.absoluteUrl)
        }
    }
    return buildInstance.result
}
