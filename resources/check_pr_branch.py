#!/usr/bin/env python
from __future__ import unicode_literals

from configparser import ConfigParser
import argparse
import os
import re
import sys

from bugzilla import Bugzilla
import requests

config = ConfigParser()
with open(os.path.join(os.path.expanduser('~'), 'automation.properties'), 'rb') as config_file:
    file_content = config_file.read().decode('utf-8')
    config_contents = '[defaults]\n{}'.format(file_content)

config.read_string(config_contents)

bugzilla_user = config.get('defaults', 'bugzilla.login')
bugzilla_password = config.get('defaults', 'bugzilla.password')
bugzilla_url = config.get('defaults', 'bugzilla.url')

github_token = os.environ.get('GITHUB_TOKEN')
if not github_token:
    raise EnvironmentError('GITHUB_TOKEN not specified')


parser = argparse.ArgumentParser(description='check that a candlepin PR references the correct BZ')
parser.add_argument('--sha1', help='the commit hash to examine', required=True)
parser.add_argument('--target', help='the target branch', required=True)
args = parser.parse_args()

bz = Bugzilla(bugzilla_url, user=bugzilla_user, password=bugzilla_password)

version = None
if args.target == 'master':
    # fetch master spec and then parse from it
    spec_file = requests.get('https://raw.githubusercontent.com/candlepin/candlepin/master/server/candlepin.spec.tmpl').text
    for line in spec_file.split('\n'):
        if line.startswith('Version:'):
            match = re.search('^Version: (\d+\.\d+)\.\d+$', line)
            version = match.group(1)
else:
    version_match = re.search('^candlepin-(.*)-HOTFIX$', args.target)
    if version_match:
        version = version_match.group(1)
if not version:
    print('Skipping because target branch is not master or HOTFIX branch')
    sys.exit(0)

message = requests.get('https://api.github.com/repos/candlepin/candlepin/git/commits/{sha1}'.format(sha1=args.sha1),
                       headers={'Authorization': 'token {}'.format(github_token)}).json()['message']

first_line = message.split('\n')[0]

match = re.search('^(\d+):? ', first_line)
if match:
    bz_number = match.group(1)
    bug = bz.getbug(bz_number)

    target_release = bug.target_release[0]
    if '---' in target_release:
        target_release = None


    final_version = target_release or bug.version
    if final_version != version:
        print('BZ references {final_version}, while PR references {version}'.format(final_version=final_version, version=version))
        sys.exit(1)
    print('Looks good, both BZ and PR reference {version}').format(version=version)
else:
    print('Skipping as commit message does not appear to reference a BZ number.')
