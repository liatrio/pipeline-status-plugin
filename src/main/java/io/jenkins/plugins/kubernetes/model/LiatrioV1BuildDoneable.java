package io.jenkins.plugins.kubernetes.model;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class LiatrioV1BuildDoneable extends CustomResourceDoneable<LiatrioV1Build> {
  public LiatrioV1BuildDoneable(LiatrioV1Build resource, Function<LiatrioV1Build, LiatrioV1Build> function) {
    super(resource, function);
  }
}
