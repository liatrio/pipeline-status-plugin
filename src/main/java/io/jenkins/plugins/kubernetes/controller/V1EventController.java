package io.jenkins.plugins.kubernetes.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventBuilder;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
import io.jenkins.plugins.StageEvent;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;

public class V1EventController implements PipelineEventHandler {
  private static Logger logger = Logger.getLogger(V1EventController.class.getName());

  private NamespacedKubernetesClient client;
  private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
  static {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    df.setTimeZone(tz);
  }

  public V1EventController(NamespacedKubernetesClient client) {
    this.client = client;
  }

  @Override
  public void handlePipelineStartEvent(PipelineEvent pipelineEvent) {
    logger.fine("PipelineStartEvent --> New event CR");

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
    logger.fine("PipelineEndEvent --> New event CR");
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
    logger.fine("StageStartEvent --> New event CR for stage: "+stageEvent.getStageName());
    Event event = asEvent(stageEvent.getPipelineEvent(), "stage");
    event.setMessage("stage "+stageEvent.getPipelineEvent().getError()
                      .map(t -> LiatrioV1ResultType.fail)
                      .orElse(LiatrioV1ResultType.inProgress)
                      .toString());
    event.setReason(stageEvent.getPipelineEvent().getError()
                    .map(t -> LiatrioV1ResultType.fail)
                    .orElse(LiatrioV1ResultType.inProgress)
                    .toString());
    event.getMetadata().getAnnotations().put("stageName",stageEvent.getStageName());
    client.events().create(event);
  }
  @Override
  public void handleStageEndEvent(StageEvent stageEvent) {
    logger.fine("StageEndEvent --> New event CR: "+stageEvent.getStageName());
    Event event = asEvent(stageEvent.getPipelineEvent(), "stage");
    event.setMessage("stage "+stageEvent.getPipelineEvent().getError()
                      .map(t -> LiatrioV1ResultType.fail)
                      .orElse(LiatrioV1ResultType.success)
                      .toString());
    event.setReason(stageEvent.getPipelineEvent().getError()
                    .map(t -> LiatrioV1ResultType.fail)
                    .orElse(LiatrioV1ResultType.success)
                    .toString());
    event.getMetadata().getAnnotations().put("stageName",stageEvent.getStageName());
    event.getMetadata().getAnnotations().put("statusMessage",stageEvent.getStatusMessage());
    client.events().create(event);
  }

  public static Event asEvent(PipelineEvent pipelineEvent, String type) {
    LiatrioV1Build build = LiatrioV1BuildMapper.asBuild(pipelineEvent);

    Map<String, String> labels = new HashMap<>();
    labels.put("type", type);
    labels.put("correlationid", build.getMetadata().getName());
    Map<String, String> annotations = new HashMap<>();
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
          .withName(build.getMetadata().getName())
          .withApiVersion(build.getApiVersion())
          .withKind(build.getKind())
          .endInvolvedObject()
        .withCount(1)
        .withFirstTimestamp(dateToString(new Date()))
        .withLastTimestamp(dateToString(new Date()))
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
    return df.format(d);
  }
}
