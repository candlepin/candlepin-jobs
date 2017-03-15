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
        mailer('chainsaw@redhat.com')
        irc {
            channel('#candlepin')
        }
    }
}
