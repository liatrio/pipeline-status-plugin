package io.jenkins.plugins;

import java.util.UUID;

import hudson.model.InvisibleAction;

public class PipelineEventAction extends InvisibleAction {
  private final String buildName;

  public PipelineEventAction() {
    this.buildName = UUID.randomUUID().toString();
  }

  public String getBuildName() {
    return buildName;
  }
}
