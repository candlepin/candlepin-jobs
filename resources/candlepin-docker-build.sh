#!/bin/bash -xe

echo $USER
groups $USER
env

echo "Using workspace: $WORKSPACE"
docker --version

./docker/build-images -p
