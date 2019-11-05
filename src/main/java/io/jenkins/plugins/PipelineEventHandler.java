package io.jenkins.plugins;

public interface PipelineEventHandler {
  default void handlePipelineStartEvent(PipelineEvent event) {}
  default void handlePipelineEndEvent(PipelineEvent event) {}
  default void handleStageStartEvent(StageEvent event) {}
  default void handleStageEndEvent(StageEvent event) {}
}
