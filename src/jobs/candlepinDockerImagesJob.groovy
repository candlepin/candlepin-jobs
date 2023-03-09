import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/candlepin-docker-images") {
    previousNames('candlepin-docker-images')
    description('Periodically builds and pushes docker images from main')
    label('docker-build')
    scm {
        github('candlepin/candlepin', 'main')
    }
    wrappers {
        preBuildCleanup()
        colorizeOutput()
        credentialsBinding {
            usernamePassword('CANDLEPIN_QUAY_BOT_USER', 'CANDLEPIN_QUAY_BOT_TOKEN', 'candlepin-quay-bot')
        }
    }
    logRotator{
        daysToKeep(90)
    }
    triggers {
        cron('H 6 * * 1')
    }
    steps {
        shell readFileFromWorkspace('src/resources/candlepin-docker-build.sh')
    }
    publishers {
        flexiblePublish {
            conditionalAction {
                condition {
                    alwaysRun()
                }
                steps {
                    shell 'docker logout quay.io'
                }
            }
        }
        irc {
            channel('#candlepin')
        }
        extendedEmail {
            recipientList('chainsaw@redhat.com')
            defaultSubject('Candlepin docker images failed to build')
            defaultContent('See $BUILD_URL')
            contentType('text/html')
            triggers {
                failure {
                    sendTo {
                        recipientList('chainsaw@redhat.com')
                    }
                    attachBuildLog()
                }
            }
        }
    }
}
