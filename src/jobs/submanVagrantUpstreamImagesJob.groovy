import jobLib.rhsmLib
String baseFolder = rhsmLib.submanJobFolder

job("$baseFolder/vagrant-upstream-images") {
    disabled()
    description('builds centos, fedora, etc. vagrant images for subman development')
    label('rhsm-packer')
    scm {
        github('candlepin/packer', 'master')
    }
    wrappers {
        colorizeOutput()
        credentialsBinding {
            string('VAGRANT_CLOUD_TOKEN', 'VAGRANT_CLOUD_TOKEN')
        }
    }
    steps {
        shell(readFileFromWorkspace('src/resources/subman-vagrant-images.sh'))
    }
}
