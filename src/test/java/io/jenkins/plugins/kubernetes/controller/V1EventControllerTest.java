package io.jenkins.plugins.kubernetes.controller;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.jenkins.plugins.PipelineEvent;

public class V1EventControllerTest {
  private V1EventController controller;

  @Before
  public void setupController() {
    controller = new V1EventController();
  }

  @After
  public void teardownController() {
    controller = null;
  }

  @Test
  public void testPipelineStart() {
    PipelineEvent event = new PipelineEvent();
    controller.handlePipelineStartEvent(event);
  }
}
