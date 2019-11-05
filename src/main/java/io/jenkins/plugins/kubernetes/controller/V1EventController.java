package io.jenkins.plugins.kubernetes.controller;

import java.util.logging.Logger;

import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
import io.jenkins.plugins.StageEvent;
import io.kubernetes.client.models.V1Event;

public class V1EventController implements PipelineEventHandler {
  private static Logger logger = Logger.getLogger(V1EventController.class.getName());
  @Override
  public void handlePipelineStartEvent(PipelineEvent event) {
    logger.info("PipelineStartEvent --> New event CR");
  }
  @Override
  public void handlePipelineEndEvent(PipelineEvent event) {
    logger.info("PipelineEndEvent --> New event CR");
  }
  @Override
  public void handleStageStartEvent(StageEvent event) {
    logger.info("StageStartEvent --> New event CR");
  }
  @Override
  public void handleStageEndEvent(StageEvent event) {
    logger.info("StageEndEvent --> New event CR");
  }
}
