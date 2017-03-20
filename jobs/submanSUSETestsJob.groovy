import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder
String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
String[] distros = ['opensuse42', 'sles11', 'sles12']

distros.each { distro ->
    def rhsmJob = job("$baseFolder/subscription-manager-suse-${distro}-tests"){
        description("Functional tests for ${distro} not scoped in QE tests.")
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
            shell 'sudo nosetests -c playpen/noserc.zypper test/zypper_test'
            shell 'sudo chown -R $USER:$USER $WORKSPACE' // since we just ran w/ sudo
        }
        publishers {
            archiveArtifacts {
                pattern('nosetests.xml')
            }
        }
    }
    rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, "jenkins-${distro}-tests", false, false)
}
