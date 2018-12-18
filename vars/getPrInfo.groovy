def call(Map args) {
    ['repo', 'pr_number'].each { arg ->
        if (!args.containsKey(arg)) {
            throw new IllegalArgumentException("Missing ${arg} in call to getPrInfo")
        }
    }
    if (args.pr_number == "" || args.pr_number == null) {
        return
    }
    node('master') {
        withCredentials([usernamePassword(
            credentialsId: 'github-api-token-as-username-password',
            passwordVariable: 'GITHUB_TOKEN',
            usernameVariable: 'GITHUB_USER')
        ]) {
            def headers = [[maskValue: true, name: 'Authorization', value: "token ${GITHUB_TOKEN}"]]
            def response = httpRequest customHeaders: headers, url: "https://api.github.com/repos/candlepin/${args.repo}/pulls/${args.pr_number}"
            def prInfo = readJSON text: "${response.content}"
            env.PR_SHA = prInfo['sha']
            env.PR_MERGEABLE = prInfo['mergeable']
            env.PR_MERGE_COMMIT_SHA = env.PR_MERGEABLE == true ? prInfo['merge_commit_sha'] : null
            env.PR_TEST_COMMIT = env.PR_MERGEABLE ? env.PR_MERGE_COMMIT_SHA : env.PR_SHA
        }
    }
}