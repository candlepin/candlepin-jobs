import jobLib.rhsmLib
String baseFolder = rhsmLib.submanJobFolder

job("$baseFolder/vagrant-upstream-images") {
    description('builds centos, fedora, etc. vagrant images for subman development')
    label('rhsm-packer')
    wrappers {
        colorizeOutput()
        credentialsBinding {
            string('VAGRANT_CLOUD_TOKEN', 'VAGRANT_CLOUD_TOKEN')
        }
    }
    steps {
        shell(readFileFromWorkspace('resources/subman-vagrant-images.sh'))
    }
}
