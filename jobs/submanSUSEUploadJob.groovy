import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder

def rhsmJob = job("$baseFolder/subscription-manager-suse-upload"){
    description('Prepare and upload subman sources to the OpenSUSE Build Service')
    label('opensuse')
    parameters {
        // FIXME move out of home:kahowell?
        stringParam('obs_project_name', 'home:kahowell', 'Build Service project to upload to.')
    }
    wrappers {
        preBuildCleanup()
        colorizeOutput('css')
        credentialsBinding {
            file('OSC_CONFIG', 'OSC_CONFIG')
        }
    }
    logRotator{
        numToKeep(20)
    }
    steps {
        shell """
. ~/.bashrc
scripts/suse_upload.sh "\${obs_project_name}" -m "Automated upload of \${GIT_COMMIT}"
"""
    }
}

String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'
rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, 'jenkins-suseupload', false, false)
