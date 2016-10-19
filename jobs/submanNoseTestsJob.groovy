devs = ['awood',
        'mstead',
        'wottop',
        'bkearney',
        'Ceiu',
        'vritant',
        'nguyenfilip',
        'cnsnyder',
        'barnabycourt',
        'kahowell']

String submanRepo = 'candlepin/subscription-manager'
String basePath = 'Scratch'

folder(basePath) {
    description 'This is a scratch job.'
}

job("$basePath/subscription-manager-nose-tests-pr"){
    logRotator{
        numToKeep(365)
    }
    scm {
        git {
            remote{
                github(submanRepo)
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }
    triggers {
        githubPullRequest {
            admins(devs)
            onlyTriggerPhrase(false)
            cron('H/5 * * * *')
            orgWhitelist('candlepin')
            userWhitelist(devs)
            credentialsId('830050e0-ece9-4878-8b3d-2779cfe76abe')
        }
    }
    steps {
        shell readFileFromWorkspace('resources/subscription-manager-nose-tests.sh')
    }
    publishers {
        archiveArtifacts {
            pattern('nosetests.xml')
            pattern('coverage.xml')
        }
        publishHtml {
            report('cover/') {
                reportName('Coverage module html report')
            }
        }
//        archiveJunit('nosetests.xml') {
//            healthScaleFactor(1.5)
//        }
        extendedEmail {
            recipientList('chainsaw@redhat.com')
        }
        irc{
            channel('#candlepin')
            strategy('FAILURE_AND_FIXED')
            notificationMessage('Default')
        }
    }
}