#!/bin/bash -xe

env | sort
echo

docker pull docker-registry.usersys.redhat.com/candlepin/candlepin-postgresql || true
echo "Docker pull completed"

# The docker container test script will know to copy out
echo "Using workspace: $WORKSPACE"
mkdir -p $WORKSPACE/artifacts/

# make selinux happy via http://stackoverflow.com/a/24334000
chcon -Rt svirt_sandbox_file_t $WORKSPACE//artifacts/

# Run the linter
docker run -P --rm -t -v $WORKSPACE/artifacts/:/artifacts/ docker-registry.usersys.redhat.com/candlepin/candlepin-postgresql cp-test -l -b jenkins -c "${sha1}"
