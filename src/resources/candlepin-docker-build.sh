#!/bin/bash -xe

echo $USER
groups $USER
env

echo "Using workspace: $WORKSPACE"
docker --version

docker login -u "$CANDLEPIN_QUAY_BOT_USER" -p "$CANDLEPIN_QUAY_BOT_TOKEN" quay.io
./docker/build-images -p -c

sudo setenforce 0
cd docker/gating-test-images
./build.sh
