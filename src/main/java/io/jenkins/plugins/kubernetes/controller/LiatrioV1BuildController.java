package io.jenkins.plugins.kubernetes.controller;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
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
    logger.fine("PipelineStartEvent --> Creating build CR");
    LiatrioV1Build build = LiatrioV1BuildMapper.asBuild(event);
    build.getSpec()
         .result(LiatrioV1ResultType.inProgress);

    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }

  @Override
  public void handlePipelineEndEvent(PipelineEvent event) {
    logger.fine("PipelineEndEvent --> Updating build CR");
    LiatrioV1Build build = LiatrioV1BuildMapper.asBuild(event);
    build.getSpec()
         .endTime(new Date())
         .result(event.getError().map(t -> LiatrioV1ResultType.fail).orElse(LiatrioV1ResultType.success));

    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }

}
