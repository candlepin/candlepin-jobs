job('Candlepin Seed Job') {
    label('rhsm')
    wrappers {
        preBuildCleanup()
    }
    scm {
        github 'candlepin/candlepin-jobs'
    }
    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        gradle 'clean test'
        dsl {
            external 'jobs/folders.groovy'
            additionalClasspath 'src/main/groovy'
        }
        dsl {
            external 'jobs/**/*Job.groovy'
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