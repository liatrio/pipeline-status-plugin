package io.jenkins.plugins;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
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
import java.util.List;
import java.util.logging.Logger;

@Extension
public class MyGraphListener implements GraphListener {
    private static Logger log = Logger.getLogger(MyGraphListener.class.getName());

    private List<String> Ids = new ArrayList<String>();

    @Override
    public void onNewHead(FlowNode flowNode) {

        if (flowNode.getClass() == StepStartNode.class) {
            StepStartNode stepNode = (StepStartNode) flowNode;
            if (stepNode.isBody() && stepNode.getStepName().equals("Stage")) {
                log.info("");
                log.info("### Starting Stage for " + stepNode.getDisplayName() + " (" + stepNode.getId() + ") ###");
                log.info("Display Name: " + stepNode.getDisplayName());
                log.info("### /Starting Stage ###");
                Ids.add(stepNode.getId());
                log.info("");
            }
        }

        if (flowNode.getClass() == StepEndNode.class && Ids.contains(((StepEndNode) flowNode).getStartNode().getId())) {
            ErrorAction errorAction = flowNode.getError();
            if(errorAction != null){
                log.info("");
                log.info("### Error Info ###");
                log.warning("Error Action: " + errorAction);
                log.warning("Error: " + errorAction.getError());
                log.warning("Error Display Name: " + errorAction.getDisplayName());
                log.info("### /Error Info ###");
                log.info("");
            }

            StepEndNode endNode = (StepEndNode) flowNode;
            BlockStartNode startNode = endNode.getStartNode();
            log.info("");
            log.info("### Ending Stage for " +
                    ((StepEndNode) flowNode).getStartNode().getDisplayName() +
                    " (" + ((StepEndNode) flowNode).getStartNode().getId() + ") ###");
            log.info("Display Name: '" + endNode.getDisplayName() + "'");
            log.info("### /Ending Stage ###");
            log.info("");
        }
    }
}
