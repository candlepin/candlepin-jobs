import jobLib.rhsmLib

String baseFolder = rhsmLib.candlepinJobFolder

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'

String[] databases = ['mysql', 'postgresql']
String[] modes = ['hosted', 'standalone']
def dbDescriptions = [mysql: 'MySQL/MariaDB', postgresql: 'PostgreSQL']
def modeDescriptions = [standalone: '', hosted: ' with Candlepin configured like hosted']
def cpTestArgs = [standalone: '-r', hosted: '-H']

databases.each { db ->
    modes.each { mode ->
        String typeSuffix = mode == 'hosted' ? 'hosted-' : ''
        String jobName = "candlepin-pullrequest-spectests-${typeSuffix}${db}"
        def rhsmJob = job("$baseFolder/$jobName") {
            previousNames(jobName)
            environmentVariables(CANDLEPIN_DATABASE: db, CP_TEST_ARGS: cpTestArgs[mode])
            description("Compiles and deploys Candlepin and runs rspec tests${modeDescriptions[mode]}, using a ${dbDescriptions[db]} backend. Does not produce any installable products.")
            label('docker')
            concurrentBuild()
            wrappers {
                timeout {
                    noActivity(7200)
                }
                preBuildCleanup()
                colorizeOutput()
            }
            logRotator{
                daysToKeep(90)
                artifactNumToKeep(5)
            }
            steps {
                shell readFileFromWorkspace('resources/candlepin-rspec-tests.sh')
            }
            publishers {
                archiveArtifacts {
                    pattern('artifacts/*')
                    allowEmpty()
                }
            }
        }

        rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.candlepinRepo, "jenkins-rspec-${typeSuffix}${db}", false)
    }
}
