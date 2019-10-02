package io.jenkins.plugins;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.logging.Logger;

@Extension
public class MyGraphListener implements GraphListener {
    private static Logger log = Logger.getLogger(MyGraphListener.class.getName());

    @Override
    public void onNewHead(FlowNode flowNode) {

        log.info("");
        log.info("**********************************");
        log.info("**********   FLOW NODE   *********");
        log.info("");
        log.info(flowNode.toString());
        log.info("");
        log.info("**********************************");
        log.info("");

    }
}
