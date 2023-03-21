import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/candlepin-podman-images") {
    previousNames('candlepin-podman-images')
    description('Periodically builds and pushes podman images from master')
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
        shell readFileFromWorkspace('src/resources/candlepin-podman-build.sh')
    }
    publishers {
        flexiblePublish {
            conditionalAction {
                condition {
                    alwaysRun()
                }
                steps {
                    shell 'podman logout quay.io'
                }
            }
        }
        irc {
            channel('#candlepin')
        }
        extendedEmail {
            recipientList('chainsaw@redhat.com')
            defaultSubject('Candlepin podman images failed to build')
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
