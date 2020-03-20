package io.jenkins.plugins.kubernetes.controller;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.Date;


import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.StageEvent;

public class V1EventControllerTest {
  @Rule
  public KubernetesServer server = new KubernetesServer(true, true);

  private V1EventController controller;
  

  @Before
  public void setupController() {
    controller = new V1EventController(server.getClient());
  }

  @After
  public void teardownController() {
    controller = null;
  }

  @Test
  public void testPipelineStartSuccess() {
    PipelineEvent event = 
      new PipelineEvent()
          .buildName("xxxxxxx")
          .timestamp(new Date())
          .error(Optional.empty());

    controller.handlePipelineStartEvent(event);

    NamespacedKubernetesClient client = server.getClient();
    EventList events = client.events().list();
    assertNotNull(events);
    assertEquals(1, events.getItems().size());
    assertEquals("pipeline inProgress", events.getItems().get(0).getMessage());
  }

  @Test
  public void testPipelineStartFail() {
    PipelineEvent event = 
      new PipelineEvent()
          .buildName("xxxxxxx")
          .timestamp(new Date())
          .error(Optional.of(new Throwable("error")));

    controller.handlePipelineStartEvent(event);

    NamespacedKubernetesClient client = server.getClient();
    EventList events = client.events().list();
    assertNotNull(events);
    assertEquals(1, events.getItems().size());
    assertEquals("pipeline fail", events.getItems().get(0).getMessage());
  }

  @Test
  public void testPipelineEndFail() {
    PipelineEvent event = 
      new PipelineEvent()
          .buildName("xxxxxxx")
          .timestamp(new Date())
          .error(Optional.of(new Throwable("error")));
    controller.handlePipelineEndEvent(event);

    NamespacedKubernetesClient client = server.getClient();
    EventList events = client.events().list();
    assertNotNull(events);
    assertEquals(1, events.getItems().size());
    assertEquals("pipeline fail", events.getItems().get(0).getMessage());
  }


  @Test
  public void testPipelineEndSuccess() {
    PipelineEvent event = 
      new PipelineEvent()
          .buildName("xxxxxxx")
          .timestamp(new Date())
          .error(Optional.empty());
    controller.handlePipelineEndEvent(event);

    NamespacedKubernetesClient client = server.getClient();
    EventList events = client.events().list();
    assertNotNull(events);
    assertEquals(1, events.getItems().size());
    assertEquals("pipeline success", events.getItems().get(0).getMessage());
  }

  @Test
  public void testPipelineTypeEndSuccess() {
    PipelineEvent event = new PipelineEvent()
          .buildName("xxxxxxx")
          .timestamp(new Date()).error(Optional.empty());
    controller.handlePipelineEndEvent(event);

    NamespacedKubernetesClient client = server.getClient();
    EventList events = client.events().list();
    assertNotNull(events);
    assertEquals(1, events.getItems().size());
    assertEquals("Normal", events.getItems().get(0).getType());
    assertEquals("pipeline success", events.getItems().get(0).getMessage());
  }

  @Test
  public void testStageEndSuccessWithMessage() {
    PipelineEvent event = 
      new PipelineEvent()
          .buildName("xxxxxxx")
          .timestamp(new Date())
          .error(Optional.empty());
    StageEvent stageEvent = 
      new StageEvent() 
          .pipelineEvent(event)
          .stageName("my stage")
          .statusMessage("success message goes here");

    controller.handleStageEndEvent(stageEvent);

    NamespacedKubernetesClient client = server.getClient();
    EventList events = client.events().list();
    assertNotNull(events);
    assertEquals(1, events.getItems().size());
    Event e = events.getItems().get(0);
    assertEquals("Normal", e.getType());
    assertEquals("stage success", e.getMessage());

    Map<String, String> annotations = e.getMetadata().getAnnotations();
    assertEquals("my stage", annotations.get("stageName"));
    assertEquals("success message goes here", annotations.get("statusMessage"));
  }

  @Test
  public void testAsEvent() {
    PipelineEvent pipelineEvent = 
      new PipelineEvent()
          .buildName("xxxxxxx")
          .timestamp(new Date())
          .error(Optional.empty());

    LiatrioV1Build build = new LiatrioV1Build();
    ObjectMeta metadata = new ObjectMeta();
    metadata.setName("testbuildname");
    metadata.setNamespace( "default");
    metadata.setResourceVersion("499779");
    metadata.setUid("e6d57ef8-017e-11ea-ba85-025000000001");
    
    build.setApiVersion("sdm.liatr.io/v1alpha1");
    build.setKind("Build");
    build.setMetadata(metadata);
      
    Event event = V1EventController.asEvent(pipelineEvent, "pipeline", build);

    assertNotNull("event", event);
    assertNotEquals("event.name", "", event.getMetadata().getName());
    assertEquals("event.metadata.labels.type", event.getMetadata().getLabels().get("type"), "pipeline");
    assertNotEquals("event.metadata.labels.correlationId", "", event.getMetadata().getLabels().get("correlationId"));
    assertEquals("event.type", event.getType(), "Normal");
    assertEquals("event.reportingcomponent", event.getReportingComponent(), "sdm.lead.liatrio/operator-jenkins");
    assertEquals("event.source.component", event.getSource().getComponent(), "sdm.lead.liatrio/operator-jenkins");
    assertNotEquals("event.involvedobject.name", "", event.getInvolvedObject().getName());
    assertEquals("event.involvedobject.namespace", event.getInvolvedObject().getNamespace(), "default");
    assertEquals("event.involvedobject.apiversion",event.getInvolvedObject().getApiVersion(), "sdm.liatr.io/v1alpha1");
    assertEquals("event.involvedobject.kind", event.getInvolvedObject().getKind(), "Build");
    assertEquals("event.involvedobject.resourceversion", event.getInvolvedObject().getResourceVersion(), "499779");
    assertEquals("event.involvedobject.uid", event.getInvolvedObject().getUid(), "e6d57ef8-017e-11ea-ba85-025000000001");
    assertEquals("event.count", event.getCount(), Integer.valueOf(1));
    assertNotNull(event.getFirstTimestamp());
    assertNotNull(event.getLastTimestamp());
    }

    @Test
    public void testDateToString() {
      Date testDate = new Date("05 October 2011 14:48 UTC");
      assertEquals("date", V1EventController.dateToString(testDate), "2011-10-05T14:48:00Z");
    }
}
