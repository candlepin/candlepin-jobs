import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/candlepin-check-bugzilla-reference") {
    description('Check that a PR references the proper BZ')
    parameters {
        stringParam('pr_number', null, 'PR number to check')
    }
    wrappers {
        credentialsBinding {
            usernamePassword('GITHUB_USER', 'GITHUB_TOKEN', 'github-api-token-as-username-password')
        }
    }
    scm {
        github('candlepin/candlepin-jobs', 'master')
    }
    label('rhsm')
    logRotator{
        daysToKeep(90)
    }
    steps {
        shell 'python resources/check_pr_branch.py ${pr_number}'
    }
}
