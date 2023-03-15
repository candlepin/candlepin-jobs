#!/bin/bash -x

# this does all of the above now
if [ "$CLEAR_BUILD_CACHE" = 'true' ]; then
  podman system prune -a -f
  podman volume prune -f
else
  # everything but the build cache
  podman image prune -a -f
  podman volume prune -f
  podman container prune -f
  podman network prune -f
  podman pods prune -f
fi
