FROM jenkins/jenkins:2.138.1-alpine

LABEL maintainer = "udaya.balagalla@pearson.com"

COPY travis/script/plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY travis/script/security.groovy /usr/share/jenkins/ref/init.groovy.d/security.groovy
COPY build/libs/*.jar /usr/share/jenkins/lib/

USER root

RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

USER jenkins

COPY build/libs/environment-operator-deployer.hpi /usr/share/jenkins/ref/plugins
RUN echo 2.138.1 > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state
RUN echo 2.138.1 > /usr/share/jenkins/ref/jenkins.install.InstallUtil.lastExecVersion