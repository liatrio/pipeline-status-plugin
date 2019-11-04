package io.jenkins.plugins.model;

import io.kubernetes.client.models.V1Event;

public class V1EventBuilder {
  public V1Event create() {
    V1Event event = new V1Event();
    return event;
  }
}
