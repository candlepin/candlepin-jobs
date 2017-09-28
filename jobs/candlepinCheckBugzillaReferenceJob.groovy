import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/candlepin-check-bugzilla-reference") {
    description('Check that a PR references the proper BZ')
    parameters {
        stringParam('sha1', null, 'sha1 of the commit to check')
        stringParam('target', null, 'which branch the commit targets')
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
        shell 'python check_pr_branch.py --sha1 $sha1 --target $target'
    }
}
