#!/bin/bash -xe

echo $USER
groups $USER
env

echo "Using workspace: $WORKSPACE"
export CONTAINER_ENGINE="podman"
"${CONTAINER_ENGINE}" --version

"${CONTAINER_ENGINE}" login -u "$CANDLEPIN_QUAY_BOT_USER" -p "$CANDLEPIN_QUAY_BOT_TOKEN" quay.io
./containers/build-images -p -c

