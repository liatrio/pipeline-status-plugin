package io.jenkins.plugins.kubernetes.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildList;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Client;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Pipeline;
import io.jenkins.plugins.kubernetes.model.LiatrioV1PipelineType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;

public class LiatrioV1BuildControllerTest {
  @Rule
  public KubernetesServer server = new KubernetesServer(true, true);

  private LiatrioV1BuildController controller;

  @Before
  public void setupController() {
    controller = new LiatrioV1BuildController(server.getClient().inNamespace("default"));
  }

  @After
  public void teardownController() {
    controller = null;
  }

  @Test
  public void testHandlePipelineStartEvent() {
    PipelineEvent event = 
      new PipelineEvent()
        .buildName("xxxxxxx")
        .jobName("test-job-name")
        .timestamp(new Date())
        .product("chatops-dev")
        .gitUrl("https://www.github.com/liatrio/springtrader-test.git")
        .commitId("123456789abcd")
        .branch("PR-11111")
        .buildId("2")
        .jobDisplayUrl("http://jenkins/job/url");

    controller.handlePipelineStartEvent(event);

    LiatrioV1Client client = new LiatrioV1Client(server.getClient().inNamespace("default"));
    LiatrioV1BuildList builds = client.builds().list();
    assertNotNull(builds);
    assertEquals("build count", 1, builds.getItems().size());

    LiatrioV1Build build = builds.getItems().get(0);
    Map<String, String> labels = build.getMetadata().getLabels();
    assertEquals("build timestamp", String.valueOf(event.getTimestamp().getTime()), labels.get("timestamp"));
    assertEquals("build pipelineName", "springtrader-test", labels.get("pipelineName"));
    assertEquals("build pipelineOrg", "liatrio", labels.get("pipelineOrg"));
    assertEquals("build product", "chatops-dev", labels.get("product"));

    LiatrioV1BuildSpec spec = build.getSpec();
    assertEquals("branch", "PR-11111", spec.getBranch());
    assertEquals("buildId", "2", spec.getBuildId());
    assertEquals("commitId", "123456789abcd", spec.getCommitId());
    assertEquals("product", "chatops-dev", spec.getProduct());
    assertEquals("type", LiatrioV1BuildType.PullRequest, spec.getType());
    assertEquals("result", LiatrioV1ResultType.inProgress, spec.getResult());
    assertEquals("url", "http://jenkins/job/url", spec.getUrl());
    assertNull("end_time", spec.getEndTime());

    LiatrioV1Pipeline pipeline = spec.getPipeline();
    assertEquals("host", "www.github.com", pipeline.getHost());
    assertEquals("name", "springtrader-test", pipeline.getName());
    assertEquals("org", "liatrio", pipeline.getOrg());
    assertEquals("type", LiatrioV1PipelineType.github, pipeline.getType());
    assertEquals("url", "https://www.github.com/liatrio/springtrader-test.git", pipeline.getUrl());

    client.close();
  }
  @Test
  public void testHandlePipelineEndEvent() {
    PipelineEvent event = 
      new PipelineEvent()
        .buildName("xxxxxxx")
        .jobName("test-job-name")
        .timestamp(new Date())
        .product("chatops-dev")
        .gitUrl("https://www.github.com/liatrio/springtrader-test.git")
        .commitId("123456789abcd")
        .branch("PR-11111")
        .buildId("2")
        .jobDisplayUrl("http://jenkins/job/url");

    controller.handlePipelineStartEvent(event);

    LiatrioV1Client client = new LiatrioV1Client(server.getClient().inNamespace("default"));
    LiatrioV1BuildList builds = client.builds().list();
    assertNotNull(builds);
    assertEquals("build count", 1, builds.getItems().size());

    event.error(Optional.empty());
    controller.handlePipelineEndEvent(event);

    builds = client.builds().list();
    assertNotNull(builds);
    assertEquals("build count", 1, builds.getItems().size());

    LiatrioV1Build build = builds.getItems().get(0);
    Map<String, String> labels = build.getMetadata().getLabels();
    assertEquals("build timestamp", String.valueOf(event.getTimestamp().getTime()), labels.get("timestamp"));
    assertEquals("build pipelineName", "springtrader-test", labels.get("pipelineName"));
    assertEquals("build pipelineOrg", "liatrio", labels.get("pipelineOrg"));
    assertEquals("build product", "chatops-dev", labels.get("product"));

    LiatrioV1BuildSpec spec = build.getSpec();
    assertEquals("branch", "PR-11111", spec.getBranch());
    assertEquals("buildId", "2", spec.getBuildId());
    assertEquals("commitId", "123456789abcd", spec.getCommitId());
    assertEquals("product", "chatops-dev", spec.getProduct());
    assertEquals("type", LiatrioV1BuildType.PullRequest, spec.getType());
    assertEquals("result", LiatrioV1ResultType.success, spec.getResult());
    assertEquals("url", "http://jenkins/job/url", spec.getUrl());
    assertNotEquals("end_time", "", spec.getEndTime());

    LiatrioV1Pipeline pipeline = spec.getPipeline();
    assertEquals("host", "www.github.com", pipeline.getHost());
    assertEquals("name", "springtrader-test", pipeline.getName());
    assertEquals("org", "liatrio", pipeline.getOrg());
    assertEquals("type", LiatrioV1PipelineType.github, pipeline.getType());
    assertEquals("url", "https://www.github.com/liatrio/springtrader-test.git", pipeline.getUrl());

    event.error(Optional.of(new Throwable("error")));
    controller.handlePipelineEndEvent(event);

    builds = client.builds().list();
    assertNotNull(builds);
    assertEquals("build count", 1, builds.getItems().size());
    build = builds.getItems().get(0);

    spec = build.getSpec();
    assertNotEquals("end_time", "", spec.getEndTime());
    assertEquals("result", LiatrioV1ResultType.fail, spec.getResult());

    client.close();
  }
}
