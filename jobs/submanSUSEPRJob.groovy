import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder
String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
String[] distros = ['opensuse42', 'sles11', 'sles12']

distros.each { distro ->
    def matcher = distro =~ /([A-Za-z]*)([0-9]*)/
    String distroName = matcher[0][1]
    String distroVersion = matcher[0][2]

    // build job
    String buildJobName = "subscription-manager-suse-build-${distro}"

    // functional tests
    String testsJobName = "subscription-manager-suse-${distro}-tests"

    // QE tier0
    String tier0JobName = "rhsm-${distroName}-${distroVersion}-x86_64-Tier0Tests"

    def rhsmJob = multiJob("$baseFolder/subscription-manager-${distro}-pr"){
        description("Job that encompasses all PR-invoked testing for ${distro}")
        steps {
            phase('Build') {
                phaseJob(buildJobName) {
                    parameters {
                        currentBuild()
                    }
                }
            }
            phase('Test') {
                phaseJob(tier0JobName) // TODO make sure tier0 installs latest build
                phaseJob(testsJobName)
            }
        }
    }
    rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, "jenkins-${distro}", false, false)
}
