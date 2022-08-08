#!/usr/bin/python3

import argparse
import json

from jenkins import Jenkins

parser = argparse.ArgumentParser(
    description="Checks testPlugins in a build.gradle against a live jenkins"
)
parser.add_argument("BUILD_SCRIPT", help="path to a build.gradle script")
parser.add_argument("JENKINS_URL", help="full jenkins URL")
parser.add_argument("USER", help="jenkins username")
parser.add_argument("TOKEN", help="jenkins api token")
args = parser.parse_args()

with open(args.BUILD_SCRIPT) as f:
    content = f.readlines()
plugins = [x.strip() for x in content if x.strip().startswith("testPlugins")]
plugins.remove("testPlugins {}")

J = Jenkins(args.JENKINS_URL, username=args.USER, password=args.TOKEN)

jenkins_script = """Jenkins.instance.pluginManager.plugins
    .findAll { !(it.shortName in ['job-dsl', 'structs']) }
    .collect { "{'group': '${it.manifest.mainAttributes.getValue("Group-Id")}', 'name': '${it.shortName}', 'version': '${it.version}' }" }
    .sort()
    .each { println it }
"""

server_plugins = J.run_script(jenkins_script).splitlines()
server_plugins = [x.strip() for x in server_plugins if x.strip().startswith("{")]
server_plugins = [x.replace("'", '"') for x in server_plugins]
server_plugins = ",".join(server_plugins)
server_plugins = f"[ {server_plugins} ]"
server_plugins = json.loads(server_plugins)

updated_plugins = []
plugin_names = []
for i in plugins:
    parts = i.split(":")
    if "group:" not in i:
        name = parts[1]
        update = [x for x in server_plugins if x["name"] == name]
        if not update:
            print(f"**Could not find plugin: {name} on server!\n")
            continue
        update = update[0]
        # testPlugins 'org.jenkins-ci.plugins:github-branch-source:2.8.2'
        updated_plugins.append(
            f"testPlugins '{update['group']}:{name}:{update['version']}'"
        )
        plugin_names.append(name)

    elif "group:" in i:
        name = parts[2].split(",")[0].replace("'", "").strip()
        update = [x for x in server_plugins if x["name"] == name]
        if not update:
            print(f"**Could not find plugin: {name} on server!\n")
            continue
        update = update[0]
        # testPlugins group: 'org.jenkins-ci.plugins.workflow', name: 'workflow-aggregator', version: '2.6'
        updated_plugins.append(
            f"testPlugins group: '{update['group']}', name: '{name}', version: '{update['version']}'"
        )
        plugin_names.append(name)

print("\n<< plugins.txt >>\n")
print("\n".join(plugin_names))
print("\n<< build.gradle >>\n")
print("\n".join(updated_plugins))
