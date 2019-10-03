package io.jenkins.plugins;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Logger;

@Extension
public class MyGraphListener implements GraphListener {
    private static Logger log = Logger.getLogger(MyGraphListener.class.getName());


    @Override
    public void onNewHead(FlowNode flowNode) {

        if (flowNode.getClass() == StepStartNode.class) {
            StepStartNode stepNode = (StepStartNode) flowNode;
            if (stepNode.isBody() && stepNode.getStepName().equals("Stage")) {
                log.info("");
                log.info("### Starting Stage ###");
                log.info("Step Name: " + stepNode.getStepName());
                log.info("isBody: " + stepNode.isBody());
                log.info("displayName: " + stepNode.getDisplayName());
                log.info("### /Starting Stage ###");
                log.info("");
            }
        }

        if (flowNode.getClass() == StepEndNode.class) {
            StepEndNode endNode = (StepEndNode) flowNode;
            BlockStartNode startNode = endNode.getStartNode();

            log.info("");
            log.info("### Ending Stage ###");
            log.info("getDisplayFunctionName(): '" + endNode.getDisplayFunctionName() + "'");
            log.info("getDisplayName(): '" + endNode.getDisplayName() + "'");
            log.info("getSearchName(): '" + endNode.getSearchName() + "'");
            log.info("getSearchUrl(): '" + endNode.getSearchUrl() + "'");
            log.info("");
            log.info("Start Node below here:");
            log.info("startNode class: " + startNode.getClass().toString());
            log.info("getDisplayName(): '" + startNode.getDisplayName() + "'");
            log.info("getId(): '" + startNode.getId() + "'");
            log.info("getDisplayFunctionName(): '" + startNode.getDisplayFunctionName() + "'");
            if(startNode.getClass() == StepStartNode.class){
                StepStartNode stepStartNode = (StepStartNode) startNode;
                log.info("stepStartNode.isBody(): " + stepStartNode.isBody());
            }
            log.info("### /Ending Stage ###");
            log.info("");
        }
    }
}
