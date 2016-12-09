# Candlepin Jenkins Job DSL

This repo is for [Candlepin](https://github.com/candlepin/candlepin) Jenkins Job DSL files.

## File structure

    .
    ├── jobs                    # DSL script files
    ├── resources               # resources for DSL scripts
    ├── src
    │   ├── main
    │   │   ├── groovy          # support classes
    │   │   └── resources
    │   │       └── idea.gdsl   # IDE support for IDEA
    │   └── test
    │       └── groovy          # specs
    └── build.gradle            # build file

# Script Examples

This repo is based off the [Job DSL example project](https://github.com/jenkinsci/job-dsl-plugin). Examples have been moved to the jobs/examples folder for reference. Check out [this presentation](https://www.youtube.com/watch?v=SSK_JaBacE0) for a walkthrough of this example (starts around 14:00).


## Testing

`./gradlew test` runs the specs.

[JobScriptsSpec](src/test/groovy/com/dslexample/JobScriptsSpec.groovy)
will loop through all DSL files and make sure they don't throw any exceptions when processed.

## Debug XML

`./gradlew debugXml -Dpattern=jobs/**/*Job.groovy` runs the DSL and writes the XML output to files to `build/debug-xml`.

This can be useful if you want to inspect the generated XML before check-in.

## Seed Job

You can create the example seed job via the Rest API Runner (see below) using the pattern `jobs/seed.groovy`.

Or manually create a job with the same structure:

* Invoke Gradle script → Use Gradle Wrapper: `true`
* Invoke Gradle script → Tasks: `clean test`
* Process Job DSLs → DSL Scripts: `jobs/**/*Job.groovy`
* Process Job DSLs → Additional classpath: `src/main/groovy`
* Publish JUnit test result report → Test report XMLs: `build/test-results/**/*.xml`

## REST API Runner

A gradle task is configured that can be used to create/update jobs via the Jenkins REST API, if desired. Normally
a seed job is used to keep jobs in sync with the DSL, but this runner might be useful if you'd rather process the
DSL outside of the Jenkins environment or if you want to create the seed job from a DSL script.

```./gradlew rest -Dpattern=<pattern> -DbaseUrl=<baseUrl> [-Dusername=<username>] [-Dpassword=<password>]```

* `pattern` - ant-style path pattern of files to include
* `baseUrl` - base URL of Jenkins server
* `username` - Jenkins username, if secured
* `password` - Jenkins password or token, if secured

## Vagrant

`vagrant up` with vagrant installed will spawn a VM that runs an instance of
Jenkins, with necessary plugins and a modified seed job that will generate the
jobs based on the vagrant shared folder (the top-level directory of this repo).
Using this, you can make changes, run `vagrant rsync`, run the seed job, and
see the results.
