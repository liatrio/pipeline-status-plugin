package io.jenkins.plugins.model;

import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

import io.kubernetes.client.models.V1Event;

public class V1EventBuilderTest {
  private V1EventBuilder builder;

  @Before
  public void setupBuilder() {
    builder = new V1EventBuilder();
  }

  @After
  public void teardownBuilder() {
    builder = null;
  }

  @Test
  public void testCreate() {
    V1Event event = builder.create();

    assertThat("event", event, is(notNullValue()));
  }
}
