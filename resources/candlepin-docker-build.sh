#!/bin/bash -xe

echo $USER
groups $USER
env

echo "Using workspace: $WORKSPACE"
docker --version

./docker/rebuild.sh -p
