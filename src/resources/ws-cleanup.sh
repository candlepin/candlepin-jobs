#!/bin/bash -x

sudo find /home/jenkins/workspace -name '*candlepin_PR*' -type d -mtime +10 -prune -exec rm -rf {} +
