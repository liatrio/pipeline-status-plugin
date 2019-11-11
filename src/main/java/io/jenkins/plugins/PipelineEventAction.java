package io.jenkins.plugins;

import java.io.Serializable;
import java.util.UUID;

import hudson.model.InvisibleAction;

public class PipelineEventAction extends InvisibleAction implements Serializable {
  private static final long serialVersionUID = -7646540789208006630L;
  private final String buildName;

  public PipelineEventAction() {
    this.buildName = UUID.randomUUID().toString();
  }

  public String getBuildName() {
    return buildName;
  }
}
