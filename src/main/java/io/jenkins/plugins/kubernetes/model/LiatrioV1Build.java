package io.jenkins.plugins.kubernetes.model;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class LiatrioV1Build extends CustomResource {
  /**
   *
   */
  private static final long serialVersionUID = -7400108945817673943L;

  private LiatrioV1BuildSpec spec = null;

  public LiatrioV1Build apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  public LiatrioV1Build kind(String kind) {
    this.setKind(kind);
    return this;
  }

  public LiatrioV1Build metadata(ObjectMeta metadata) {
    this.setMetadata(metadata);
    return this;
  }

  public LiatrioV1BuildSpec getSpec() {
    return spec;
  }

  public void setSpec(LiatrioV1BuildSpec spec) {
    this.spec = spec;
  }

  public LiatrioV1Build spec(LiatrioV1BuildSpec spec) {
    this.spec = spec;
    return this;
  }

  @Override
  public ObjectMeta getMetadata() { return super.getMetadata(); }

  @Override
  public String toString() {
    return "Build{" +
        "apiVersion='" + getApiVersion() + '\'' +
        ", metadata=" + getMetadata() +
        ", spec=" + spec +
        '}';
  }
}
