package io.jenkins.plugins;

import java.util.List;
import java.util.Objects;

import hudson.model.InvisibleAction;

public class CheckoutAction extends InvisibleAction {
  private final String repoUrl;
  private final String branch;
  private final String commitId;
  private final String commitMessage;
  private final List<String> committers;


  public CheckoutAction(String repoUrl, String branch, String commitId, String commitMessage, List<String> committers) {
    this.repoUrl = repoUrl;
    this.branch = branch;
    this.commitId = commitId;
    this.commitMessage = commitMessage;
    this.committers = committers;
  }

  public String getRepoUrl() {
    return this.repoUrl;
  }


  public String getBranch() {
    return this.branch;
  }


  public String getCommitId() {
    return this.commitId;
  }


  public String getCommitMessage() {
    return this.commitMessage;
  }


  public List<String> getCommitters() {
    return this.committers;
  }


  @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CheckoutAction)) {
            return false;
        }
        CheckoutAction checkoutAction = (CheckoutAction) o;
        return Objects.equals(repoUrl, checkoutAction.repoUrl) && Objects.equals(branch, checkoutAction.branch) && Objects.equals(commitId, checkoutAction.commitId) && Objects.equals(commitMessage, checkoutAction.commitMessage) && Objects.equals(committers, checkoutAction.committers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repoUrl, branch, commitId, commitMessage, committers);
  }

  @Override
  public String toString() {
    return "{" +
      " repoUrl='" + getRepoUrl() + "'" +
      ", branch='" + getBranch() + "'" +
      ", commitId='" + getCommitId() + "'" +
      ", commitMessage='" + getCommitMessage() + "'" +
      ", committers='" + getCommitters() + "'" +
      "}";
  }

}
