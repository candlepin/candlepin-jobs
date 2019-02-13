job('Development Seed Job') {
    customWorkspace('/var/jenkins_home/dsl-dev/')
    steps {
        gradle 'clean test'
        dsl {
        external 'src/jobs/**/*Job.groovy'
        additionalClasspath 'src/main/groovy'
        }
        dsl {
        external 'src/jobs/views.groovy'
        additionalClasspath 'src/main/groovy'
        }
    }
}