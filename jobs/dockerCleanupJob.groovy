jenkinsSlavesScript = '["rhsm-jenkins-slave1.usersys.redhat.com:selected",' +
                       '"rhsm-jenkins-slave2.usersys.redhat.com:selected",' +
                       '"rhsm-jenkins-slave3.usersys.redhat.com:selected",' +
                       '"rhsm-jenkins-slave4.usersys.redhat.com:selected",' +
                       '"rhsm-jenkins-slave5.usersys.redhat.com:selected"]'

job("Docker Cleanup"){
    description('This job deletes unused docker images across all the slave nodes.')
    label('rhsm')
    parameters {
        //choiceParam('DOCKER_HOSTS', jenkinsSlaves, 'Host-names of servers to run on.')
        activeChoiceParam('DOCKER_HOSTS') {
            description('Hostnames of servers to run on.')
            choiceType('MULTI_SELECT')
            groovyScript {
                script(jenkinsSlavesScript)
            }
        }
    }
    wrappers {
        sshAgent('fe2c79db-3166-4e61-8996-a8e7de7fbb5c')
    }
    logRotator{
        numToKeep(10)
    }
    triggers {
        cron('H 0 * * 6')
    }
    steps {
        shell readFileFromWorkspace('resources/docker-cleanup.sh')
    }
}