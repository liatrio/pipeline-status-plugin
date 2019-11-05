package io.jenkins.plugins.kubernetes.model;

public class LiatrioV1Pipeline {
  private String host = null;

  private String name = null;

  private String org = null;

  private LiatrioV1PipelineType type = null;

  private String url = null;

  public LiatrioV1Pipeline host(String host) {
    this.host = host;
    return this;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public LiatrioV1Pipeline name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LiatrioV1Pipeline org(String org) {
    this.org = org;
    return this;
  }

  public String getOrg() {
    return org;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public LiatrioV1Pipeline type(LiatrioV1PipelineType type) {
    this.type = type;
    return this;
  }

  public LiatrioV1PipelineType getType() {
    return type;
  }

  public void setType(LiatrioV1PipelineType type) {
    this.type = type;
  }

  public LiatrioV1Pipeline url(String url) {
    this.url = url;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
