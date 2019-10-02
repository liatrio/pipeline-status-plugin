package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.BuildStepListener;
import hudson.tasks.BuildStep;

import java.util.logging.Logger;

@Extension
public class MyBuildStepListener extends BuildStepListener {
    private static Logger log = Logger.getLogger(MyBuildStepListener.class.getName());

    @Override
    public void started(AbstractBuild abstractBuild, BuildStep buildStep, BuildListener buildListener) {
        log.info("");
        log.info("******** STARTED ********");
        log.info("");
        log.info("abstractBuild: " + abstractBuild.toString());
        log.info("");
        log.info("buildStep: " + buildStep.toString());
        log.info("");
        log.info("buildListener: " + buildListener.toString());
        log.info("");
        log.info("******* /STARTED ********");
        log.info("");
    }

    @Override
    public void finished(AbstractBuild abstractBuild, BuildStep buildStep, BuildListener buildListener, boolean b) {
        log.info("");
        log.info("******** FINISHED ********");
        log.info("");
        log.info("abstractBuild: " + abstractBuild.toString());
        log.info("");
        log.info("buildStep: " + buildStep.toString());
        log.info("");
        log.info("buildListener: " + buildListener.toString());
        log.info("");
        log.info("b: " + String.valueOf(b));
        log.info("");
        log.info("******* /FINISHED ********");
        log.info("");
    }
}
