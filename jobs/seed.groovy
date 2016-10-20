job('seed') {
    scm {
        github 'Lorquas/candlepin-jobs'
    }
    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        gradle 'clean test'
        dsl {
            external 'jobs/**/*Job.groovy'
            additionalClasspath 'src/main/groovy'
        }
    }
}