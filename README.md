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

## Testing

`./gradlew test` runs the specs.

[JobScriptsSpec](src/test/groovy/com/dslexample/JobScriptsSpec.groovy)
will loop through all DSL files and make sure they don't throw any exceptions when processed.  The seed job will also run the tests before deploying automatically so a broken DSL isn't deployed, which can result in a half broken state between jobs.

## Debug XML

Jenkins itself stores all of its configurations in XML internally. This DSL effectively directs Jenkins to generate this XML.  Sometimes for debugging purposes (or converting from manual jobs to DSL) it is useful to compare or look at the XML directly.

To see the existing xml on any job through the jenkins UI, go to the job and add `/config.xml` to the URL.  For example: `https://<your-jenkins>.rhev-ci-vms.eng.rdu2.redhat.com/job/<your job>/config.xml`

When you run `./gradlew test` the XML output files will be copied to `build/debug-xml/`. This can be useful if you want to inspect the generated XML before check-in.

## Seed Job

You can create the example seed job via the Rest API Runner (see below) using the pattern `jobs/seed.groovy`.

Or manually create a job with the same structure:

* Invoke Gradle script → Use Gradle Wrapper: `true`
* Invoke Gradle script → Tasks: `clean test`
* Process Job DSLs → DSL Scripts: `jobs/**/*Job.groovy`
* Process Job DSLs → Additional classpath: `src/main/groovy`
* Publish JUnit test result report → Test report XMLs: `build/test-results/**/*.xml`

# Running this project

Originally this project was designed for running on your own hosted version of Jenkins (such as the Jenkins CSB) and populating the jobs on there via the seed jobs.  Included here is a vagrant/ansible setup (in the `ansible` folder) that that will start a local Jenkins server that will give you a full access environment to test your jobs and poke around however you like.  Also included is a container environment setup in the `docker` folder if you prefer working that way or deploy your jenkins on openshift.  For the purposes of the examples given, both enviornments are set up the same using  [Jenkins Configuration as Code](https://github.com/jenkinsci/configuration-as-code-plugin/blob/master/README.md) to set up the system.

## Vagrant

Before creating a vagrant instance with jenkins on it, it's a good idea to install the vagrant-hostmanager plugin first.  You can do this like so:

```
sudo vagrant plugin install vagrant-hostmanager
```

This plugin will allow the new instance to be accessible via it's hostname which by default is `jenkins.example.com`


`vagrant up` with vagrant installed will spawn a VM that runs an instance of
Jenkins, with necessary plugins and a modified seed job that will generate the
jobs based on the vagrant shared folder (the top-level directory of this repo).
Using this, you can make changes, run `vagrant rsync`, run the seed job, and
see the results.

If you would like to test the jobs against your own repos, you can set `CANDLEPIN_JENKINS_GITHUB_ORG` to your username
(or another org where appropriate repos exist).

## Vagrant Jenkins

Once you have started your vagrant instance, you can go to it with your browser.  If you installed the hostmanager plugin, you can browse to it from your local machine:

```
http://jenkins.example.com:8080
```

The default user and password are admin/admin

The `Development Seed Job` on this server is equivalent to the `Main Seed` job in the project.

Check out the `jenkins_plugins` var in `ansible/vagrant.yml` for a list of installed plugins.  Included are a big list of plugins for many use cases, you should edit this list to your use case.

## Docker

Included is a `docker/Dockerfile` for building a docker container to run in openstack or the CP environment.  You will have to edit `script_approval.groovy` and `docker/jenkins.yaml` to  your liking (tons of examples [here](https://github.com/jenkinsci/configuration-as-code-plugin/tree/master/demos)).  With this Dockerfile it creates a directory `$JENKINS_HOME/dsl-dev`.  If you run your docker container and mount this project into it you will get the eqivalent to the dev enviorment that we have in vagrant.  An example of how to run this locally is:

```
cd docker
docker build -t jenkins-example .
docker run --rm --name jenkins-example -p 8080:8080 -v "$HOME/Projects/jenkins-dsl-template":/var/jenkins_home/dsl-dev  jenkins-example
```
* replace `$HOME/Projects/jenkins-dsl-template` with whever you have this project

## Jenkins Plugins

When you run the jobs you may get many warnings or possibly errors referring to missing plugins.  You can add these plugins as dependencies in the `build.gradle` using the `testPlugins` function, and they can be installed on the vagrant/development Jenkins instance in the `ansible/vagrant.yml` file under the `jenkins_plugins` var.

Its also worth noting here that this project tries to keep up with the latest Jenkins CSB release.  Jenkins and DSL plugin versions are set project side in `gradle.properties`.

# Creating your own job

Here are some tips and guidelines on how to create your own Job with the Groovy DSL.  Groovy is not terribly hard, and most of the code is hopefully relatively easy to understand.

It all starts from the [seed job](src/jobs/seed.groovy), which will process all files in the `src/jobs/` folder that end in `Jobs.groovy`.  It also runs [views.groovy](src/jobs/views.groovy) which manages the tabs on the jenkins UI.

## The job()

The core of a Job is the job() function.  Just like in the GUI, each job must have a unique name, so the job function takes one argument, which is the name if your job:


```
job("my-job-name") {
    // ...
}
```

