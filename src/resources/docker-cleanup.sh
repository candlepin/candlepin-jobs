#!/bin/bash -x

#docker images -q | xargs -r docker rmi -f
#docker ps -aq | xargs -r docker rm -f
#docker network rm $(docker network ls | awk '$3 == "bridge" && $2 != "bridge" { print $1 }')
#docker volume ls -q | xargs -r docker volume rm

# this does all of the above now
if [ "$CLEAR_BUILD_CACHE" = 'true' ]; then
  docker system prune -a -f
  # add --volumes to the above whenever on Docker 17.06.1
  docker volume prune -f
else
  # everything but the build cache
  docker image prune -a -f
  docker volume prune -f
  docker container prune -f
  docker network prune -f
fi

if [ "$RESTART_DOCKER" = 'true' ]; then
  sudo systemctl restart docker
fi



