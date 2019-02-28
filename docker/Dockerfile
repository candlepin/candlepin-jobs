FROM jenkins/jenkins:lts

# for resuse
ARG JENKINS_HOME=/var/jenkins_home

# pre load all the plugins with plugins.txt as per jenkins documentation
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# load up your config as code
ENV CASC_JENKINS_CONFIG=${JENKINS_HOME}/jenkins.yaml
COPY jenkins.yaml ${JENKINS_HOME}/jenkins.yaml

# for mounting in dsl dev directory; runs as jenkins user
RUN mkdir -p ${JENKINS_HOME}/dsl-dev

# place your own init.groovy for things JCasC can't do
COPY script_approval.groovy ${JENKINS_HOME}/init.groovy.d/script_approval.groovy
# start_seed: https://github.com/jenkinsci/configuration-as-code-plugin/issues/619
COPY start_seed.groovy ${JENKINS_HOME}/init.groovy.d/start_seed.groovy

# use seed job instead of inline in the jcaac
COPY seed.groovy ${JENKINS_HOME}/seed.groovy

EXPOSE 8080
