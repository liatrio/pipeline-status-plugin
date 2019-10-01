FROM jenkins/jenkins:lts

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY --chown=jenkins:jenkins target/pipeline-status-plugin.hpi /var/jenkins_home/plugins/
RUN touch /var/jenkins_home/plugins/pipeline-status-plugin.hpi.pinned


