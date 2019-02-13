import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder
String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
String[] distros = ['opensuse42', 'sles12'] // FIXME sles11 can't be tested due missing python deps (incl. nose)

distros.each { distro ->
    def rhsmJob = job("$baseFolder/subscription-manager-suse-${distro}-tests"){
        description("Functional tests for ${distro} not scoped in QE tests.")
        label(distro)
        environmentVariables {
            propertiesFile('/etc/rhsm_zypper_tests.properties')
        }
        wrappers {
            preBuildCleanup()
            colorizeOutput('css')
        }
        logRotator{
            numToKeep(20)
        }
        steps {
            shell readFileFromWorkspace('src/resources/subscription-manager-suse-tests.sh')
        }
        publishers {
            archiveArtifacts {
                pattern('nosetests.xml')
            }
        }
    }
    rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, "jenkins-${distro}-tests", false, false)
}
