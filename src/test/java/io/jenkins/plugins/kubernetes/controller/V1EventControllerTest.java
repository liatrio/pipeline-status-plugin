package io.jenkins.plugins.kubernetes.controller;


import static org.junit.Assert.*;

import java.util.Optional;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.jenkins.plugins.PipelineEvent;

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
    PipelineEvent event = 
      new PipelineEvent()
          .error(Optional.empty());
    controller.handlePipelineEndEvent(event);

    NamespacedKubernetesClient client = server.getClient();
    EventList events = client.events().list();
    assertNotNull(events);
    assertEquals(1, events.getItems().size());
    assertEquals("Normal", events.getItems().get(0).getType());
    assertEquals("pipeline success", events.getItems().get(0).getMessage());
  }

  @Test
  public void testAsEvent() {
    PipelineEvent pipelineEvent = 
      new PipelineEvent()
          .error(Optional.empty());
    Event event = V1EventController.asEvent(pipelineEvent, "pipeline");

    assertNotNull("event", event);
    assertNotEquals("event.name", "", event.getMetadata().getName());
    assertEquals("event.metadata.labels.type", event.getMetadata().getLabels().get("type"), "pipeline");
    assertEquals("event.reportingcomponent", event.getReportingComponent(), "sdm.lead.liatrio/operator-jenkins");
    assertEquals("event.source.component", event.getSource().getComponent(), "sdm.lead.liatrio/operator-jenkins");
    //Should we test everything for involved object?
    assertEquals("event.count", event.getCount(), Integer.valueOf(1));
    assertNotNull(event.getFirstTimestamp());
    assertNotNull(event.getLastTimestamp());
    }

    @Test
    public void testDateToString() {
      Date testDate = new Date("05 October 2011 14:48 UTC");
      assertEquals("date", controller.dateToString(testDate), "2011-10-05T14:48:00Z");
    }
}
