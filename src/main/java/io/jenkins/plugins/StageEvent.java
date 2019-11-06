package io.jenkins.plugins;

import java.util.Objects;

public class StageEvent {
  private PipelineEvent pipelineEvent;
  private String stageName;

  public PipelineEvent getPipelineEvent() {
    return this.pipelineEvent;
  }

  public void setPipelineEvent(PipelineEvent pipelineEvent) {
    this.pipelineEvent = pipelineEvent;
  }

  public StageEvent pipelineEvent(PipelineEvent pipelineEvent) {
    this.pipelineEvent = pipelineEvent;
    return this;
  }

  public String getStageName() {
    return this.stageName;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
  }

  public StageEvent stageName(String stageName) {
    this.stageName = stageName;
    return this;
  }

  @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof StageEvent)) {
            return false;
        }
        StageEvent stageEvent = (StageEvent) o;
        return Objects.equals(stageName, stageEvent.stageName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(stageName);
  }

  @Override
  public String toString() {
    return "{" +
      " stageName='" + getStageName() + "'" +
      "}";
  }
}
