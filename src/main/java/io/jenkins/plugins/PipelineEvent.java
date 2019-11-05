package io.jenkins.plugins;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PipelineEvent {

  private String product;
  private String jobDisplayUrl;
  private String branch;
  private String gitUrl;
  private Date timestamp;
  private String jobName;
  private String buildId;
  private String commitId;
  private String commitMessage;
  private List<String> committers;
  private List<String> stages;
  private Optional<Throwable> error;

  public PipelineEvent(String product, String jobDisplayUrl, String branch, String gitUrl, Date timestamp, String jobName, String buildId, String commitId, String commitMessage, List<String> committers, List<String> stages, Optional<Throwable> error) {
    this.product = product;
    this.jobDisplayUrl = jobDisplayUrl;
    this.branch = branch;
    this.gitUrl = gitUrl;
    this.timestamp = timestamp;
    this.jobName = jobName;
    this.buildId = buildId;
    this.commitId = commitId;
    this.commitMessage = commitMessage;
    this.committers = committers;
    this.stages = stages;
    this.error = error;
  }

  public Optional<Throwable> getError() {
    return this.error;
  }

  public void setError(Optional<Throwable> error) {
    this.error = error;
  }

  public PipelineEvent error(Optional<Throwable> error) {
    this.error = error;
    return this;
  }


  public PipelineEvent() {
  }

  public List<String> getStages() {
    return this.stages;
  }

  public void setStages(List<String> stages) {
    this.stages = stages;
  }

  public PipelineEvent stages(List<String> stages) {
    this.stages = stages;
    return this;
  }

  public String getJobName() {
    return this.jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public PipelineEvent jobName(String jobName) {
    this.jobName = jobName;
    return this;
  }

  public String getProduct() {
    return this.product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public String getJobDisplayUrl() {
    return this.jobDisplayUrl;
  }

  public void setJobDisplayUrl(String jobDisplayUrl) {
    this.jobDisplayUrl = jobDisplayUrl;
  }

  public PipelineEvent product(String product) {
    this.product = product;
    return this;
  }

  public PipelineEvent jobDisplayUrl(String jobDisplayUrl) {
    this.jobDisplayUrl = jobDisplayUrl;
    return this;
  }

  public String getCommitMessage() {
    return this.commitMessage;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public List<String> getCommitters() {
    return this.committers;
  }

  public void setCommitters(List<String> committers) {
    this.committers = committers;
  }

  public PipelineEvent commitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
    return this;
  }

  public PipelineEvent committers(List<String> committers) {
    this.committers = committers;
    return this;
  }

  public String getCommitId() {
    return this.commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public PipelineEvent commitId(String commitId) {
    this.commitId = commitId;
    return this;
  }

  public String getBuildId() {
    return this.buildId;
  }

  public void setBuildId(String buildId) {
    this.buildId = buildId;
  }

  public PipelineEvent buildId(String buildId) {
    this.buildId = buildId;
    return this;
  }

  public Date getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public PipelineEvent timestamp(Date timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public String getGitUrl() {
    return this.gitUrl;
  }

  public void setGitUrl(String gitUrl) {
    this.gitUrl = gitUrl;
  }

  public PipelineEvent gitUrl(String gitUrl) {
    this.gitUrl = gitUrl;
    return this;
  }

  public String getBranch() {
    return this.branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public PipelineEvent branch(String branch) {
    this.branch = branch;
    return this;
  }

  @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PipelineEvent)) {
            return false;
        }
        PipelineEvent pipelineEvent = (PipelineEvent) o;
        return Objects.equals(branch, pipelineEvent.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(branch);
  }

  @Override
  public String toString() {
    return "{" +
      " branch='" + getBranch() + "'" +
      "}";
  }
}
