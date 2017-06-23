import jobLib.rhsmLib

String baseFolder = rhsmLib.submanJobFolder
String githubOrg = binding.variables['CANDLEPIN_JENKINS_GITHUB_ORG'] ?: 'candlepin'

def DISTROS = [ // values can be found via `osc repos`
    'opensuse42': 'openSUSE_Leap_42.2',
    'sles11': 'SUSE_SLE-11_SP3_standard',
    'sles12': 'SLE_12_SP1',
]

DISTROS.each { name, repo_name ->
    def rhsmJob = job("$baseFolder/subscription-manager-suse-build-${name}"){
        description("Build SUSE RPMs locally for ${name}")
        label(name)
        parameters {
            // FIXME move out of home:kahowell?
            stringParam('obs_project_name', 'home:kahowell', 'Build Service project to reference for config.')
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
            shell "scripts/suse_build.sh \"\${obs_project_name}\" \"${repo_name}\" -k \$WORKSPACE"
            shell """
cd python-rhsm
../scripts/suse_build.sh \"\${obs_project_name}\" \"${repo_name}\" -k \$WORKSPACE
cd ..
"""
            shell '''
# adapted from QE-RPMs jobs (thanks jsefler)
BUILD_ARCHS=x86_64
for ARCH in $BUILD_ARCHS; do
    TARGETDIR=$WORKSPACE/rpms/$ARCH
    mkdir -p $TARGETDIR

    for RPM in *.${ARCH}.rpm; do
        mv $RPM $TARGETDIR/$RPM
        # copying the rpm to a versionless rpm filename allows automated hudson jobs to reference predictable artifacts like...
        # lastSuccessfulBuild/artifact/rpms/x86_64/subscription-manager.rpm
        VERSIONLESS_RPM=`echo $RPM | sed "s/\\(\\($PKG\\(-[a-zA-Z]\\+\\)*\\)\\)-.*/\\1.rpm/"`
        cp $TARGETDIR/$RPM $TARGETDIR/$VERSIONLESS_RPM
    done

    createrepo $TARGETDIR
done
'''
        }
        publishers {
            archiveArtifacts('rpms/**')
        }
    }

    rhsmLib.addPullRequester(rhsmJob, githubOrg, rhsmLib.submanRepo, "jenkins-susebuild-${name}", false, false)
}
