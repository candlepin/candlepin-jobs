import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

job("$baseFolder/candlepin-docker-images") {
    previousNames('candlepin-docker-images')
    description('Periodically builds and pushes docker images from master')
    label('docker')
    scm {
        github('candlepin/candlepin', 'master')
    }
    wrappers {
        preBuildCleanup()
        colorizeOutput()
    }
    logRotator{
        daysToKeep(90)
    }
    triggers {
        cron('H 6 * * 1')
    }
    steps {
        shell readFileFromWorkspace('resources/candlepin-docker-build.sh')
    }
    publishers {
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
