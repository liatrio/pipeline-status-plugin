package io.jenkins.plugins;

import hudson.model.InvisibleAction;

public class StageMessageAction extends InvisibleAction {
  private final String message;

  public StageMessageAction(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
