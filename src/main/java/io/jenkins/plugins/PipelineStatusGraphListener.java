package io.jenkins.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.*;
import hudson.model.*;
import hudson.model.Queue;

import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.*;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import io.jenkins.plugins.model.LiatrioV1Build;
import io.jenkins.plugins.model.LiatrioV1Builder;
import io.kubernetes.client.*;
import io.kubernetes.client.util.ClientBuilder;
import net.sf.json.JSONObject;
import io.kubernetes.client.apis.CustomObjectsApi;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepEnvironmentContributor;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.jenkinsci.plugins.githubautostatus.GithubBuildStatusGraphListener;
import org.jenkinsci.plugins.githubautostatus.model.BuildStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.ExecutionModelAction;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTEnvironment;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;

import javax.annotation.Nonnull;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

@Extension
public class PipelineStatusGraphListener extends StepEnvironmentContributor implements GraphListener {
    private static Logger log = Logger.getLogger(PipelineStatusGraphListener.class.getName());

    private List<String> Ids = new ArrayList<String>();

    private EnvVars envVars= new EnvVars();

    @Override
    public void buildEnvironmentFor(@Nonnull StepContext stepContext, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        envVars = envs;
    }

    private static String currentTimestamp() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public LiatrioV1Build generateBuild(String stageStatus, ArrayList<String> stages){
        /*Basic sanitization of the job names, this can be expanded*/
        //eventName = eventName.toLowerCase();
        //eventName = eventName.replace(' ','-');

        LiatrioV1Build build = new LiatrioV1Builder().withEnvVars(envVars).build();

        String job = envVars.get("JOB_NAME", "unknown").toLowerCase();
        if (job.contains("/")) {
            job = job.replace("/", "-");
        }

        String stageString = "[";

        for (int i = 0; i < stages.size(); i++) {
          stageString = stageString + "\"" + stages.get(i)+ "\"";
          log.info("info = " + stages.get(i));
          if (i != stages.size() - 1) {
            stageString = stageString + ", ";
          }
        }
        stageString = stageString + "]";



        long timestamp = Instant.now().toEpochMilli();

        return body;
    }

    @Override
    public void onNewHead(FlowNode flowNode) {
        //log.info(envVars.toString());
        FlowExecution exec = flowNode.getExecution();
        Run<?, ?> run = runFor(exec);
        List<BuildStage> stageNames = getDeclarativeStages(run);
        ArrayList<String> stageNameList = new ArrayList<>();

        log.info("flownode = " + flowNode.getDisplayName());
        String currentFlownode = flowNode.getDisplayName();

        if (currentFlownode.equals("Set environment variables : End"))
        {
          for (int i = 0; i < stageNames.size(); i++)
            {
              log.info("STAGE NAME @ " + i + " = " + stageNames.get(i).getStageName());
              stageNameList.add(stageNames.get(i).getStageName());
            }
        }

        log.info("Built Stage List - " + stageNameList);

        try {
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);
            client.setDebugging(true);

            CustomObjectsApi apiInstance = new CustomObjectsApi();

            String group = "stable.liatr.io"; // String | The custom resource's group name
            String version = "v1"; // String | The custom resource's version

            String plural = "builds"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
            String pretty = "true"; // String | If 'true', then the output is pretty printed.
            HashMap<String, Object> body;
            Object result;

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz);
            String startTime = df.format(new Date());

            if (currentFlownode.equals("Set environment variables : End")) {     
                LiatrioV1Build build = new LiatrioV1Builder().withEnvVars(envVars).build();
                //build.getSpec().startTime(currentTimestamp());

                body = generateBuild("inProgress", stageNameList);
                result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
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
                    log.info("STEPNODE CONTAINS " + stepNode);
                    //body = generateEvent(stepNode.getDisplayName(), "inProgress", startTime, "null", stageNameList);
                    //result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                    //log.info(result.toString());
                }
            }

            if (flowNode.getClass() == StepEndNode.class && Ids.contains(((StepEndNode) flowNode).getStartNode().getId())) {
                ErrorAction errorAction = flowNode.getError();
                //String endTime = df.format(new Date());
                if(errorAction != null){
                    log.info("");
                    log.info("### Error Info ###");
                    log.warning("Error Action: " + errorAction);
                    log.warning("Error: " + errorAction.getError());
                    log.warning("Error Display Name: " + errorAction.getDisplayName());
                    log.info("### /Error Info ###");
                    log.info("");

                    //body = generateEvent(errorAction.getDisplayName(), "fail", startTime, endTime, stageNameList);
                    //result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                    //log.info(result.toString());
                }

                StepEndNode endNode = (StepEndNode) flowNode;
                log.info("");
                log.info("### Ending Stage for " +
                        ((StepEndNode) flowNode).getStartNode().getDisplayName() +
                        " (" + ((StepEndNode) flowNode).getStartNode().getId() + ") ###");
                log.info("Display Name: '" + endNode.getDisplayName() + "'");
                log.info("### /Ending Stage ###");
                log.info("");

                //body = generateEvent(((StepEndNode) flowNode).getStartNode().getDisplayName(), "success", startTime, endTime, stageNameList);
                //result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                //log.info(result.toString());

            }
        } catch (Exception exception) {
            log.info("exception caught");
            log.info(exception.toString());
            log.info("temp");

        }
    }





    protected static List<BuildStage> getDeclarativeStages(Run<?, ?> run) {
        ExecutionModelAction executionModelAction = run.getAction(ExecutionModelAction.class);
        if (null == executionModelAction) {
            return null;
        }
        ModelASTStages stages = executionModelAction.getStages();
        if (null == stages) {
            return null;
        }
        List<ModelASTStage> stageList = stages.getStages();
        if (null == stageList) {
            return null;
        }
        return convertList(stageList);
    }

    /**
     * Converts a list of {@link ModelASTStage} objects to a list of stage names.
     *
     * @param modelList list to convert
     * @return list of stage names
     */
    private static List<BuildStage> convertList(List<ModelASTStage> modelList) {
        ArrayList<BuildStage> result = new ArrayList<>();
        for (ModelASTStage stage : modelList) {
            HashMap<String, Object> environmentVariables = new HashMap<String, Object>();
            ModelASTEnvironment modelEnvironment = stage.getEnvironment();
            if (modelEnvironment != null) {
                stage.getEnvironment().getVariables().forEach((key, value) -> {
                    String groovyValue = value.toGroovy();
                    if (groovyValue.startsWith("'")) {
                        groovyValue = groovyValue.substring(1);
                    }
                    if (groovyValue.endsWith("'")) {
                        groovyValue = groovyValue.substring(0, groovyValue.length() - 1);
                    }
                    environmentVariables.put(key.getKey(), groovyValue);
                });
            }
            ModelASTOptions options = stage.getOptions();
            if (options != null) {
                for (ModelASTOption option : options.getOptions()) {
                    for (ModelASTMethodArg arg : option.getArgs()) {
                        if (arg instanceof ModelASTKeyValueOrMethodCallPair) {
                            ModelASTKeyValueOrMethodCallPair arg2 = (ModelASTKeyValueOrMethodCallPair) arg;
                            JSONObject value = (JSONObject) arg2.getValue().toJSON();

                            environmentVariables.put(String.format("%s.%s", option.getName(), arg2.getKey().getKey()),
                                    value.get("value"));
                        }
                    }
                }
            }

            for (String stageName : getAllStageNames(stage)) {
                result.add(new BuildStage(stageName, environmentVariables));
            }
        }
        return result;
    }

    private static List<String> getAllStageNames(ModelASTStage stage) {
        List<String> stageNames = new ArrayList<>();
        stageNames.add(stage.getName());
        List<ModelASTStage> stageList = null;
        if (stage.getStages() != null) {
            stageList = stage.getStages().getStages();
        } else {
            stageList = stage.getParallelContent();
        }
        if (stageList != null) {
            for (ModelASTStage innerStage : stageList) {
                stageNames.addAll(getAllStageNames(innerStage));
            }
        }
        return stageNames;
    }

    private static @CheckForNull Run<?, ?> runFor(FlowExecution exec) {
        Queue.Executable executable;
        try {
            executable = exec.getOwner().getExecutable();
        } catch (IOException x) {
            getLogger().log(Level.WARNING, null, x);
            return null;
        }
        if (executable instanceof Run) {
            return (Run<?, ?>) executable;
        } else {
            return null;
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(GithubBuildStatusGraphListener.class.getName());
    }


}
