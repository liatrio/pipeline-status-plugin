package io.jenkins.plugins.kubernetes.controller;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
import io.jenkins.plugins.StageEvent;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Client;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;


public class LiatrioV1BuildController implements PipelineEventHandler {
  private static Logger logger = Logger.getLogger(LiatrioV1BuildController.class.getName());
  private LiatrioV1Client client;
  private String namespace;

  public LiatrioV1BuildController(NamespacedKubernetesClient client) {
    this.client = new LiatrioV1Client(client);
    this.namespace = Optional.ofNullable(client.getNamespace()).orElse("default");
  }

  @Override
  public void handlePipelineStartEvent(PipelineEvent event) {
    LiatrioV1Build build = LiatrioV1BuildMapper.asBuild(event);
    build.getSpec()
         .result(LiatrioV1ResultType.inProgress);
    logger.info(() -> "PipelineStartEvent --> Creating build "+build);
    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }

  @Override
  public void handlePipelineEndEvent(PipelineEvent event) {
    LiatrioV1Build build = LiatrioV1BuildMapper.asBuild(event);
    build.getSpec()
         .endTime(new Date())
         .result(event.getError().map(t -> LiatrioV1ResultType.fail).orElse(LiatrioV1ResultType.success));
    logger.info(() -> "PipelineEndEvent --> Updating build "+build);
    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }
  @Override
  public void handleStageStartEvent(StageEvent event) {
    LiatrioV1Build build = LiatrioV1BuildMapper.asBuild(event.getPipelineEvent());
    build.getSpec()
         .result(LiatrioV1ResultType.inProgress);
    logger.info(() -> "StageStartEvent --> Creating build "+build);
    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }
  @Override
  public void handleStageEndEvent(StageEvent event) {
    LiatrioV1Build build = LiatrioV1BuildMapper.asBuild(event.getPipelineEvent());
    build.getSpec()
         .result(LiatrioV1ResultType.inProgress);
    logger.info(() -> "StageStartEvent --> Creating build "+build);
    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }

}
