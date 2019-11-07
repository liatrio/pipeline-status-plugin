package io.jenkins.plugins.kubernetes.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone; 

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class LiatrioV1BuildSpec {
  private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
  static {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    df.setTimeZone(tz);
  }

  private String branch = null;

  @JsonProperty("build_id")
  private String buildId = null;

  @JsonProperty("commit_id")
  private String commitId = null;

  @JsonProperty("commit_message")
  private String commitMessage = null;

  private List<String> committers = null;

  private LiatrioV1Pipeline pipeline = null;

  private String product = null;

  private LiatrioV1ResultType result = null;

  private List<String> stages = null;

  @JsonProperty("start_time")
  private String startTime = null;

  @JsonProperty("end_time")
  private String endTime = null;

  private LiatrioV1BuildType type = null;

  private String url = null;

  public LiatrioV1BuildSpec() {
  }

  public LiatrioV1BuildSpec branch(String branch) {
    this.branch = branch;
    return this;
  }

  public LiatrioV1BuildSpec buildId(String buildId) {
    this.buildId = buildId;
    return this;
  }

  public LiatrioV1BuildSpec commitId(String commitId) {
    this.commitId = commitId;
    return this;
  }

  public LiatrioV1BuildSpec commitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
    return this;
  }

  public LiatrioV1BuildSpec pipeline(LiatrioV1Pipeline pipeline) {
    this.pipeline = pipeline;
    return this;
  }

  public LiatrioV1BuildSpec product(String product) {
    this.product = product;
    return this;
  }

  public LiatrioV1BuildSpec result(LiatrioV1ResultType result) {
    this.result = result;
    return this;
  }

  public LiatrioV1BuildSpec stages(List<String> stages) {
    this.stages = stages;
    return this;
  }

  public LiatrioV1BuildSpec startTime(Date startTime) {
    this.startTime = df.format(startTime);
    return this;
  }

  public LiatrioV1BuildSpec endTime(Date endTime) {
    this.endTime = df.format(endTime);
    return this;
  }

  public LiatrioV1BuildSpec type(LiatrioV1BuildType type) {
    this.type = type;
    return this;
  }

  public LiatrioV1BuildSpec url(String url) {
    this.url = url;
    return this;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getBuildId() {
    return buildId;
  }

  public void setBuildId(String buildId) {
    this.buildId = buildId;
  }

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public LiatrioV1Pipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(LiatrioV1Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public LiatrioV1ResultType getResult() {
    return result;
  }

  public void setResult(LiatrioV1ResultType result) {
    this.result = result;
  }

  public List<String> getStages() {
    return stages;
  }

  public void setStages(List<String> stages) {
    this.stages = stages;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public LiatrioV1BuildType getType() {
    return type;
  }

  public void setType(LiatrioV1BuildType type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<String> getCommitters() {
    return this.committers;
  }

  public void setCommitters(List<String> committers) {
    this.committers = committers;
  }

  public LiatrioV1BuildSpec committers(List<String> committers) {
    this.committers = committers;
    return this;
  }

  @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof LiatrioV1BuildSpec)) {
            return false;
        }
        LiatrioV1BuildSpec liatrioV1BuildSpec = (LiatrioV1BuildSpec) o;
        return Objects.equals(branch, liatrioV1BuildSpec.branch) && Objects.equals(buildId, liatrioV1BuildSpec.buildId) && Objects.equals(commitId, liatrioV1BuildSpec.commitId) && Objects.equals(commitMessage, liatrioV1BuildSpec.commitMessage) && Objects.equals(pipeline, liatrioV1BuildSpec.pipeline) && Objects.equals(product, liatrioV1BuildSpec.product) && Objects.equals(result, liatrioV1BuildSpec.result) && Objects.equals(stages, liatrioV1BuildSpec.stages) && Objects.equals(startTime, liatrioV1BuildSpec.startTime) && Objects.equals(type, liatrioV1BuildSpec.type) && Objects.equals(url, liatrioV1BuildSpec.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(branch, buildId, commitId, commitMessage, pipeline, product, result, stages, startTime, type, url);
  }

  @Override
  public String toString() {
    return "{" +
      " branch='" + getBranch() + "'" +
      ", buildId='" + getBuildId() + "'" +
      ", commitId='" + getCommitId() + "'" +
      ", commitMessage='" + getCommitMessage() + "'" +
      ", pipeline='" + getPipeline() + "'" +
      ", product='" + getProduct() + "'" +
      ", result='" + getResult() + "'" +
      ", stages='" + getStages() + "'" +
      ", startTime='" + getStartTime() + "'" +
      ", type='" + getType() + "'" +
      ", url='" + getUrl() + "'" +
      "}";
  }


}
