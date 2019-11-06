package io.jenkins.plugins.kubernetes.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
import java.util.logging.Logger;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventBuilder;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.openshift.api.model.GroupRestrictionFluent.LabelsNested;
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
    Map<String, String> labels = new HashMap<>();

    Event event = asEvent(pipelineEvent, "pipeline");
    event.setMessage("pipeline "+pipelineEvent.getError()
                      .map(t -> LiatrioV1ResultType.fail)
                      .orElse(LiatrioV1ResultType.inProgress)
                      .toString());
    event.setReason(pipelineEvent.getError()
                    .map(t -> LiatrioV1ResultType.fail)
                    .orElse(LiatrioV1ResultType.inProgress)
                    .toString());
    client.events().create(event);
  }
  @Override
  public void handlePipelineEndEvent(PipelineEvent pipelineEvent) {
    logger.info("PipelineEndEvent --> New event CR");
    Event event = asEvent(pipelineEvent, "pipeline");
    event.setMessage("pipeline "+pipelineEvent.getError()
                      .map(t -> LiatrioV1ResultType.fail)
                      .orElse(LiatrioV1ResultType.success)
                      .toString());
    event.setReason(pipelineEvent.getError()
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

  public static Event asEvent(PipelineEvent pipelineEvent, String type) {
    Map<String, String> labels = new HashMap<>();
    Map<String, String> annotations = new HashMap<>();
    labels.put("type", type);
    String name = UUID.randomUUID().toString().toLowerCase();
    Event event = 
      new EventBuilder()
        .withNewMetadata()
          .withName(name)
          .withLabels(labels)
          .withAnnotations(annotations)
          .endMetadata()
        .withType(eventType(pipelineEvent))
        .withReportingComponent("sdm.lead.liatrio/operator-jenkins")
        .withNewSource()
          .withComponent("sdm.lead.liatrio/operator-jenkins")
          .endSource()
        .withNewInvolvedObject()
          .withName(name)
          //.withNamespace("toolchain")
          .withApiVersion("stable.liatr.io/v1")
          .withKind("Build")
          .endInvolvedObject()
        .withCount(1)
        // .withFirstTimestamp(dateToString(pipelineEvent.getTimestamp()))
        .build();
    return event; 
  }

  public static String eventType(PipelineEvent pipelineEvent) {
    LiatrioV1ResultType type = pipelineEvent.getError()
    .map(t -> LiatrioV1ResultType.fail)
    .orElse(LiatrioV1ResultType.success);
    type.toString();
    if (type.toString() == "fail"){
      return "Warning";
    } else {
      return "Normal";
    }
  }
 
  public static String dateToString(Date d) {
    return String.format("%td-%tm-%ty-%<tH:%<tM:%<tS", d, d, d, d, d, d);
  }

}
