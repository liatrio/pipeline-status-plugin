package io.jenkins.plugins;


import hudson.*;
import hudson.model.*;
import hudson.model.Queue;
import hudson.plugins.git.GitException;
import hudson.plugins.git.util.BuildData;
import hudson.remoting.VirtualChannel;
import hudson.util.LogTaskListener;

import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.*;
import org.jenkinsci.plugins.workflow.graph.FlowEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.FlowStartNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jenkins.plugins.kubernetes.controller.LiatrioV1BuildController;
import io.jenkins.plugins.kubernetes.controller.V1EventController;
import jenkins.MasterToSlaveFileCallable;
import jenkins.scm.RunWithSCM;
import net.sf.json.JSONObject;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.eclipse.jgit.lib.ObjectId;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.ExecutionModelAction;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTEnvironment;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.logging.Level;

@Extension
public class PipelineEventGraphListener implements GraphListener {
    private static Logger logger = Logger.getLogger(PipelineEventGraphListener.class.getName());
    private static NamespacedKubernetesClient client;

    private final ArrayList<PipelineEventHandler> eventHandlers = new ArrayList<>();

    public PipelineEventGraphListener() {
        eventHandlers.add(new LiatrioV1BuildController(client));
        eventHandlers.add(new V1EventController(client));
    }

    public static void setClient(NamespacedKubernetesClient client) {
        PipelineEventGraphListener.client = client;
    }

    public static NamespacedKubernetesClient getClient() {
        if (client == null) {
            client = new DefaultKubernetesClient();
        }
        return client;
    }

    @Override
    public void onNewHead(FlowNode flowNode) {
        try {
            if (isPipelineNode(flowNode)) {
                PipelineEvent event = asPipelineEvent(flowNode);
                if (flowNode.getClass() == FlowStartNode.class) {
                    eventHandlers.forEach(h -> h.handlePipelineStartEvent(event));
                } else if (flowNode.getClass() == FlowEndNode.class) {
                    eventHandlers.forEach(h -> h.handlePipelineEndEvent(event));
                }
            } else if (isStageNode(flowNode)) {
                StageEvent event = asStageEvent(flowNode);

                if (flowNode.getClass() == StepStartNode.class) {
                    eventHandlers.forEach(h -> h.handleStageStartEvent(event));
                } else if (flowNode.getClass() == StepEndNode.class) {
                    eventHandlers.forEach(h -> h.handleStageEndEvent(event));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing step node", e);
        }
    }

    private PipelineEvent asPipelineEvent(FlowNode flowNode) throws IOException, InterruptedException {
        Run<?, ?> run = runFor(flowNode.getExecution());
        TaskListener taskListener = new LogTaskListener(logger, Level.INFO);
        EnvVars envVars = run.getEnvironment(taskListener);
        PipelineEvent event = 
            new PipelineEvent()
                .product(envVars.get("product","unknown"))
                .jobName(envVars.get("JOB_NAME", "unknown"))
                .stages(getDeclarativeStages(run))
                .buildId(envVars.get("BUILD_ID", "1"))
                .timestamp(run.getTime())
                .error(Optional.ofNullable(flowNode.getError()).map(ErrorAction::getError))
                .gitUrl(getGitRepo(run).orElse(null))
                .branch(envVars.get("GIT_BRANCH", envVars.get("BRANCH_NAME",null)))
                .commitId(getGitCommitId(run).orElse(null))
                .commitMessage(getGitCommitMessage(run).orElse(null))
                .committers(getGitCommitters(run));
        return event;
    }

    private StageEvent asStageEvent(FlowNode flowNode) throws IOException, InterruptedException {
        String stageName = flowNode.getDisplayName();
        String stageMessage = "";
        if (flowNode instanceof StepEndNode) {
            StepStartNode stageStartNode = ((StepEndNode) flowNode).getStartNode();
            stageName = stageStartNode.getDisplayName();
            StageMessageAction action = stageStartNode.getAction(StageMessageAction.class);
            if(action != null) {
                stageMessage = action.getMessage();
            }

        }
        StageEvent event = 
            new StageEvent()
                .pipelineEvent(asPipelineEvent(flowNode))
                .statusMessage(stageMessage)
                .stageName(stageName);
        return event;
    }

    private boolean isPipelineNode(FlowNode flowNode) {
        return (flowNode instanceof FlowStartNode || flowNode instanceof FlowEndNode);
    }

    private boolean isStageNode(FlowNode flowNode) {
        // Check for StepStartNode with stepName == 'Stage'
        if (flowNode instanceof StepStartNode) {
            StepStartNode stepNode = (StepStartNode) flowNode;
            return (stepNode.isBody() && stepNode.getStepName().equals("Stage")); 
        } 
        
        // Check for StepEndNode with a startNode that isStageNode
        if (flowNode instanceof StepEndNode) {
            StepEndNode endNode = (StepEndNode) flowNode;
            return isStageNode(endNode.getStartNode());
        }

        return false;
    }

    private Optional<String> getGitRepo(Run<?, ?> run) {
        Optional<String> uri = 
            Stream.of(run.getAction(BuildData.class))
                  .filter(Objects::nonNull)
                  .map(bd -> bd.getRemoteUrls())
                  .filter(Objects::nonNull)
                  .flatMap(Set::stream)
                  .findFirst();
        return uri;
    }

    private Optional<String> getGitCommitId(Run<?, ?> run) {
        Optional<String> sha1 = 
            Stream.of(run.getAction(BuildData.class))
                  .filter(Objects::nonNull)
                  .map(bd -> bd.getLastBuiltRevision())
                  .filter(Objects::nonNull)
                  .map(r -> r.getSha1String())
                  .findFirst();
        return sha1;
    }

    private Optional<String> getGitCommitMessage(Run<?, ?> run) {
        FilePath workspace = run.getExecutor().getCurrentWorkspace();
        Object rtn = workspace.act(new MasterToSlaveFileCallable() {
            public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                try {
                    ObjectId oid = Git.with(null, null).in(f).getClient().getRepository()
                            .resolve("refs/heads/" + branch);
                    return oid.name();
                } catch (GitException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return Optional.empty();
    }

    private List<String> getGitCommitters(Run<?, ?> run) {
        List<String> committers= 
            Stream.of(run)
                  .filter(WorkflowRun.class::isInstance)
                  .map(WorkflowRun.class::cast)
                  .map(wr -> wr.getCulpritIds()).flatMap(Set::stream)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
        return committers;
    }

    protected static List<String> getDeclarativeStages(Run<?, ?> run) {
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
    private static List<String> convertList(List<ModelASTStage> modelList) {
        ArrayList<String> result = new ArrayList<>();
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
                result.add(stageName);
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
            logger.log(Level.WARNING, null, x);
            return null;
        }
        if (executable instanceof Run) {
            return (Run<?, ?>) executable;
        } else {
            return null;
        }
    }
}
