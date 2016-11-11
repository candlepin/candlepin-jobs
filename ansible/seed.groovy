job('Development Seed Job') {
    customWorkspace('/vagrant/')
    steps {
        gradle 'clean test'
        dsl {
            external 'jobs/**/*Job.groovy'
            additionalClasspath 'src/main/groovy'
        }
    }
}
