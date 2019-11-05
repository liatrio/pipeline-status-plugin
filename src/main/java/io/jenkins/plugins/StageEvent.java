package io.jenkins.plugins;

import java.util.Objects;

public class StageEvent {
  private String stageName;

  public StageEvent() {
  }

  public StageEvent(String stageName) {
    this.stageName = stageName;
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
