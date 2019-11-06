package io.jenkins.plugins.kubernetes.controller;

import java.util.UUID;
import java.util.logging.Logger;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventBuilder;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
import io.jenkins.plugins.StageEvent;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;

public class V1EventController implements PipelineEventHandler {
  private static Logger logger = Logger.getLogger(V1EventController.class.getName());

  private NamespacedKubernetesClient client;

  public V1EventController(NamespacedKubernetesClient client) {
    this.client = client;
  }

  @Override
  public void handlePipelineStartEvent(PipelineEvent pipelineEvent) {
    logger.info("PipelineStartEvent --> New event CR");
    Event event = asEvent(pipelineEvent);
    event.setMessage("pipeline "+pipelineEvent.getError()
                      .map(t -> LiatrioV1ResultType.fail)
                      .orElse(LiatrioV1ResultType.inProgress)
                      .toString());
    client.events().create(event);
  }
  @Override
  public void handlePipelineEndEvent(PipelineEvent pipelineEvent) {
    logger.info("PipelineEndEvent --> New event CR");
    Event event = asEvent(pipelineEvent);
    event.setMessage("pipeline "+pipelineEvent.getError()
                      .map(t -> LiatrioV1ResultType.fail)
                      .orElse(LiatrioV1ResultType.success)
                      .toString());
    client.events().create(event);
  }
  @Override
  public void handleStageStartEvent(StageEvent stageEvent) {
    logger.info("StageStartEvent --> New event CR for stage: "+stageEvent.getStageName());
    // TODO: create event resouce
  }
  @Override
  public void handleStageEndEvent(StageEvent stageEvent) {
    logger.info("StageEndEvent --> New event CR: "+stageEvent.getStageName());
    // TODO: create event resouce
  }

  public static Event asEvent(PipelineEvent pipelineEvent) {
    Event event = 
      new EventBuilder()
        .withNewMetadata()
          .withName(UUID.randomUUID().toString().toLowerCase())
          .endMetadata()
        .build();
    return event; 
  }
}
