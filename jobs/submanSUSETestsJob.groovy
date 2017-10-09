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
            shell '''
if [ -d python-rhsm ]; then
  pushd python-rhsm
fi
python setup.py build_ext --inplace
cd $WORKSPACE
sudo -i bash -c "cd $WORKSPACE; PYTHONPATH=$WORKSPACE/src:$WORKSPACE/python-rhsm/src nosetests -c playpen/noserc.zypper test/zypper_test"
'''
            shell 'sudo chown -R $USER $WORKSPACE' // since we just ran w/ sudo
        }
        publishers {
            archiveArtifacts {
                pattern('nosetests.xml')
            }
        }
    }
    rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, "jenkins-${distro}-tests", false, false)
}
