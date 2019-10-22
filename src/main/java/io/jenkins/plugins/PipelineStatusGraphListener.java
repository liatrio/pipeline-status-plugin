package io.jenkins.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.kubernetes.client.apis.CustomObjectsApi;

import java.util.ArrayList;
import java.util.HashMap;
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
        CustomObjectsApi apiInstance = new CustomObjectsApi();
    // invokes the CoreV1Api client
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            log.info(item.getMetadata().getName());
        }

    

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
            log.info("trying to build event");
            String group = "stable.liatr.io"; // String | The custom resource's group name
            String version = "v1"; // String | The custom resource's version
            String namespace = "default"; // String | The custom resource's namespace
            String plural = "logs"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
            String json = "{\n" +
                    "  \"apiVersion\": \"stable.liatr.io/v1\",\n" +
                    "  \"kind\": \"JenkinsLog\",\n" +
                    "  \"metadata\": {\n" +
                    "    \"name\": \"event3\"\n" +
                    "  },\n" +
                    "  \"spec\": {\n" +
                    "    \"logMessage\": \"Stage 3 complete\"\n" +
                    "  }\n" +
                    "}"; // Object | The JSON schema of the Resource to create.
            HashMap<String,Object> body =
                    new ObjectMapper().readValue(json, HashMap.class);
            String pretty = "true"; // String | If 'true', then the output is pretty printed.

            Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, body, pretty);
            log.info((String)result);
            log.info("finished building event");

        }
    } catch (Exception exception) {
        log.info("exception caught");
        log.info(exception.toString());
    }
    }
}
