package io.jenkins.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.util.KubeConfig;
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

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@Extension
public class PipelineStatusGraphListener implements GraphListener {
    private static Logger log = Logger.getLogger(PipelineStatusGraphListener.class.getName());

    private List<String> Ids = new ArrayList<String>();

    public HashMap<String,Object> generateEvent(String eventName, String stageStatus){
        eventName = eventName.toLowerCase();
        eventName = eventName.replace(' ','-');
        long timestamp = Instant.now().toEpochMilli();
        String json = "{\n" +
                "   \"apiVersion\": \"stable.liatr.io/v1\",\n" +
                "   \"kind\": \"JenkinsLog\",\n" +
                "   \"metadata\": {\n" +
                "      \"name\": \"" + eventName + "-" + timestamp + "\"\n" +
                "   },\n" +
                "   \"spec\": {\n" +
                "      \"logMessage\":\"" + eventName + "-" + timestamp +" \",\n" +
                "      \"stageStatus\":\"" + stageStatus + " \",\n" +
                "      \"timestamp\":\"" + timestamp + "\"\n" +
                "   }\n" +
                "}";
        HashMap<String, Object> body = null;
        try {
            body =
                    new ObjectMapper().readValue(json, HashMap.class);
        }catch (IOException e){
            log.info(e.toString());
        }
        return body;
    }

    @Override
    public void onNewHead(FlowNode flowNode) {
        try {
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);
            //CoreV1Api api = new CoreV1Api(); //for pod information
            CustomObjectsApi apiInstance = new CustomObjectsApi();
            V1ObjectMeta meta = new V1ObjectMeta();
            String group = "stable.liatr.io"; // String | The custom resource's group name
            String version = "v1"; // String | The custom resource's version
            String namespace = meta.getNamespace(); // String | The custom resource's namespace
            log.info(namespace);
            SharedInformerFactory sh = new SharedInformerFactory();

            String plural = "logs"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
            String pretty = "true"; // String | If 'true', then the output is pretty printed.
            HashMap<String, Object> body;
            Object result;


            if (flowNode.getClass() == StepStartNode.class) {
                StepStartNode stepNode = (StepStartNode) flowNode;
                if (stepNode.isBody() && stepNode.getStepName().equals("Stage")) {
                    log.info("");
                    log.info("### Starting Stage for " + stepNode.getDisplayName() + " (" + stepNode.getId() + ") ###");
                    log.info("Display Name: " + stepNode.getDisplayName());
                    log.info("### /Starting Stage ###");
                    Ids.add(stepNode.getId());
                    log.info("");
                    body = generateEvent(stepNode.getDisplayName(), "IN_PROGRESS");
                    result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                    log.info(result.toString());
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

                    body = generateEvent(errorAction.getDisplayName(), "FAILURE");
                    result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                    log.info(result.toString());
                }

                StepEndNode endNode = (StepEndNode) flowNode;
                log.info("");
                log.info("### Ending Stage for " +
                        ((StepEndNode) flowNode).getStartNode().getDisplayName() +
                        " (" + ((StepEndNode) flowNode).getStartNode().getId() + ") ###");
                log.info("Display Name: '" + endNode.getDisplayName() + "'");
                log.info("### /Ending Stage ###");
                log.info("");

                body = generateEvent(((StepEndNode) flowNode).getStartNode().getDisplayName(), "SUCCESS");
                result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                log.info(result.toString());

            }
        } catch (Exception exception) {
            log.info("exception caught");
            log.info(exception.toString());
        }
    }
}
