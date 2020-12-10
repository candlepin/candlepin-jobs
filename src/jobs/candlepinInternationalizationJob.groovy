import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/candlepin-internationalization") {
    description('Periodically executes the gettext and msgattrib tasks on master/hotfix branches')
    label('candlepinbuild')
    scm {
        git {
            remote {
                url('https://github.com/candlepin/candlepin.git')
                credentials('bot_github_token')
            }
            branch("master")
        }
    }
    wrappers {
        credentialsBinding{
            string("GITHUB_TOKEN", "bot_github_token")
        }
    }
    triggers {
        cron('0 7 * * 0,4')
    }
    steps {
        shell readFileFromWorkspace('src/resources/candlepin-internationalization.sh')
    }
}
