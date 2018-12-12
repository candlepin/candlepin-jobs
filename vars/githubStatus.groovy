def call(Map args) {
    ['repo', 'status', 'sha', 'context', 'targetUrl', 'pr_number'].each { arg ->
        if (!args.containsKey(arg)) {
            throw new IllegalArgumentException("Missing ${arg} in call to githubStatus")
        }
    }

    return; // don't want to actually set at the moment...

    if (pr_number == null) {
        return  // if this wasn't a PR, no need to report status
    }

    GITHUB_CREDENTIALS_ID = 'github-api-token-as-username-password'
    GITHUB_ACCOUNT = 'candlepin'
    PENDING_MESSAGE = 'Build has been scheduled.'
    SUCCESS_MESSAGE = 'Test(s) passed.'
    FAILURE_MESSAGE = 'Test(s) failed.'
    STATUS_MESSAGE_MAP = [
        'PENDING': PENDING_MESSAGE,
        'SUCCESS': SUCCESS_MESSAGE,
        'FAILURE': FAILURE_MESSAGE,
    ]

    description = args.get('description') ?: STATUS_MESSAGE_MAP[args.status]
    try {
        githubNotify(
            account: GITHUB_ACCOUNT,
            repo: args.repo,
            credentialsId: GITHUB_CREDENTIALS_ID,
            sha: args.sha,
            status: args.status,
            description: description,
            context: args.context,
            targetUrl: args.targetUrl
        )
    } catch (e) {
        echo "Unable to update GitHub status (repo: ${args.repo}, sha: ${args.sha}, status: ${args.status}, context: ${args.context})"
    }
}
