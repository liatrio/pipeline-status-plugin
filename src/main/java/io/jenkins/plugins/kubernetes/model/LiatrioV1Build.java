package io.jenkins.plugins.kubernetes.model;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class LiatrioV1Build implements KubernetesResource, HasMetadata {
  /**
   *
   */
  private static final long serialVersionUID = -7400108945817673943L;

  private String apiVersion = null;

  private String kind = null;

  private ObjectMeta metadata = null;

  private LiatrioV1BuildSpec spec = null;

  public LiatrioV1Build apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public LiatrioV1Build kind(String kind) {
    this.kind = kind;
    return this;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public LiatrioV1Build metadata(ObjectMeta metadata) {
    this.metadata = metadata;
    return this;
  }

  public ObjectMeta getMetadata() {
    return metadata;
  }

  public void setMetadata(ObjectMeta metadata) {
    this.metadata = metadata;
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


}
