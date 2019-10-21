package io.jenkins.plugins;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import groovy.util.logging.Log;
import io.kubernetes.client.*;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.ClientBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Extension
public class PipelineStatusGraphListener implements GraphListener {
    private static Logger log = Logger.getLogger(PipelineStatusGraphListener.class.getName());

    private List<String> Ids = new ArrayList<String>();

    @Override
    public void onNewHead(FlowNode flowNode) {
    log.info("not broke");
    try {
        ApiClient client = ClientBuilder.cluster().build();

    // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

    // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();

    // invokes the CoreV1Api client
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            log.info(item.getMetadata().getName());
        }
    } catch (Exception exception) { log.info("exception caught");}
    

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
