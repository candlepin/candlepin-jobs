job('Candlepin Seed Job') {
    label('rhsm')
    wrappers {
        preBuildCleanup()
    }
    scm {
        github('candlepin/candlepin-jobs', 'master')
    }
    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        gradle 'clean test'
        dsl {
            external 'src/jobs/folders.groovy'
            additionalClasspath 'src/main/groovy'
        }
        dsl {
            external 'src/jobs/**/*Job.groovy'
            additionalClasspath 'src/main/groovy'
        }
        dsl {
            external 'src/jobs/views.groovy'
            additionalClasspath 'src/main/groovy'
        }
    }
    publishers {
        publishHtml {
            report('build/reports/tests/') {
                reportName('Grade Test Results')
            }
        }
    }
}
