#!/bin/bash -x

function jssh(){
  ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no jenkins@${1} ${2}
}

for HOST in $(echo $DOCKER_HOSTS | sed "s/,/ /g"); do
	jssh ${HOST} "docker images | grep '<none>' | awk '{print \$3}' | xargs -r docker rmi"
    jssh ${HOST} "docker ps -aq | xargs -r docker rm -f"
done
