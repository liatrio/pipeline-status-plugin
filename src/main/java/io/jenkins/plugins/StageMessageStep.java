package io.jenkins.plugins;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

public class StageMessageStep extends Builder implements SimpleBuildStep {
  private static Logger logger = Logger.getLogger(StageMessageStep.class.getName());
  private String message;

  public String getMessage() { 
    return message;
  }

  @DataBoundConstructor
  public StageMessageStep(String message) {
    this.message = message;
  }

  @Override
  public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
    if(message != null && !message.isEmpty()) {
      if(run instanceof WorkflowRun) {
        FlowNode stageNode = getStepStartNode(((WorkflowRun)run).getExecution().getCurrentHeads());
        if(stageNode != null) {
          stageNode.addAction(new StageMessageAction(message));
        }
      }
    }
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }


  private FlowNode getStepStartNode(List<FlowNode> flowNodes){
    FlowNode currentFlowNode = null;
    boolean labelAction;

    for (FlowNode flowNode: flowNodes){
        currentFlowNode = flowNode;
        labelAction = false;

        if (flowNode instanceof StepStartNode){
            labelAction = hasLabelAction(flowNode);
        }

        if (labelAction){
            return flowNode;
        }
    }

    if (currentFlowNode == null) {
        return null;
    }

    return getStepStartNode(currentFlowNode.getParents());
  }

  private boolean hasLabelAction(FlowNode flowNode){
    List<Action> actions = flowNode.getActions();

    for (Action action: actions){
        if (action instanceof LabelAction) {
            return true;
        }
    }

    return false;
  }


  @Symbol("stageMessage") // For Jenkins pipeline workflow. This lets pipeline refer to step using the defined identifier
  @Extension // This indicates to Jenkins that this is an implementation of an extension point.
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Set message for stage";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
      req.bindJSON(this, formData); // Use stapler request to bind
      save();
      return true;
    }
  }
}
