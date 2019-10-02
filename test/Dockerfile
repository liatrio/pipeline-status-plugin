FROM jenkins/jenkins:lts

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# install plugins
COPY test/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# install plugin for testing
COPY --chown=jenkins:jenkins target/pipeline-status-plugin.hpi /var/jenkins_home/plugins/
RUN touch /var/jenkins_home/plugins/pipeline-status-plugin.hpi.pinned

# use CasC for initial job setup
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc/
COPY --chown=jenkins:jenkins test/casc/ /var/jenkins_home/casc/
RUN ls -lsah /var/jenkins_home/casc/
