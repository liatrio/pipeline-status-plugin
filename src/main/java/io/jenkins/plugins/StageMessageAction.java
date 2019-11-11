package io.jenkins.plugins;

import java.io.Serializable;

import hudson.model.InvisibleAction;

public class StageMessageAction extends InvisibleAction implements Serializable {
  private static final long serialVersionUID = 1091957594597087234L;
  private final String message;

  public StageMessageAction(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
