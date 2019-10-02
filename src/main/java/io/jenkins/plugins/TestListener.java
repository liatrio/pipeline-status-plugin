package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStep;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.logging.Logger;

@Extension
public class TestListener implements GraphListener {
    private static Logger log = Logger.getLogger(TestListener.class.getName());

    @Override
    public void onNewHead(FlowNode flowNode) {
        log.info(flowNode.toString());

        System.out.println("");
        System.out.println("**********************************");
        System.out.println("*********      HERE      *********");
        System.out.println("**********************************");
        System.out.println("");

    }

    private static boolean isStage(FlowNode node) {
        return node != null && ((node.getAction(StageAction.class) != null)
                || (node.getAction(LabelAction.class) != null && node.getAction(ThreadNameAction.class) == null));
    }
}
