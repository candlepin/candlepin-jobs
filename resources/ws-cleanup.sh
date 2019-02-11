#!/bin/bash -x

sudo find /home/jenkins/workspace/candlepin/ -name '*ws-cleanup*' -type d -prune -exec rm -rf {} +
