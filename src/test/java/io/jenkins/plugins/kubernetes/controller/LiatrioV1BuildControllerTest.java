package io.jenkins.plugins.kubernetes.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Pipeline;
import io.jenkins.plugins.kubernetes.model.LiatrioV1PipelineType;

public class LiatrioV1BuildControllerTest {
  @Rule
  public KubernetesServer server = new KubernetesServer(true, true);

  private LiatrioV1BuildController controller;

  @Before
  public void setupController() {
    controller = new LiatrioV1BuildController(server.getClient());
  }

  @After
  public void teardownController() {
    controller = null;
  }

  @Test
  public void testHandlePipelineStartEvent() {
    PipelineEvent event = new PipelineEvent().jobName("test-job-name").timestamp(new Date()).product("chatops-dev")
        .gitUrl("https://www.github.com/liatrio/springtrader-test.git").commitId("123456789abcd").branch("PR-11111")
        .buildId("2").jobDisplayUrl("http://jenkins/job/url");

    controller.handlePipelineStartEvent(event);

    NamespacedKubernetesClient client = server.getClient();
    List<HasMetadata> buildList = client.resourceList(new LiatrioV1Build()).get();
    assertNotNull(buildList);
    assertEquals(1, buildList.size());
  }

  @Test
  public void testAsBuild() {
    PipelineEvent event = 
      new PipelineEvent()
        .jobName("test-job-name")
        .timestamp(new Date())
        .product("chatops-dev")
        .gitUrl("https://www.github.com/liatrio/springtrader-test.git")
        .commitId("123456789abcd")
        .branch("PR-11111")
        .buildId("2")
        .jobDisplayUrl("http://jenkins/job/url");

    LiatrioV1Build build = LiatrioV1BuildController.asBuild(event);

    assertThat("build", build, is(notNullValue()));
    assertThat("build.apiVersion", build.getApiVersion(), is(equalTo("stable.liatr.io/v1")));
    assertThat("build.kind", build.getKind(), is(equalTo("Build")));
    //assertThat("build.meta.name", build.getMetadata().getName(),
    //    is(equalTo("chatops-dev-liatrio-springtrader-test-12345678-2")));

    Map<String, String> labels = build.getMetadata().getLabels();
    assertThat("labels", labels, is(notNullValue()));
    assertThat("labels.product", labels.get("product"), is("chatops-dev"));
    assertThat("labels.pipeline_org", labels.get("pipeline_org"), is("liatrio"));
    assertThat("labels.pipeline_name", labels.get("pipeline_name"), is("springtrader-test"));
    assertThat("labels.timestamp", labels.get("timestamp"), not(is("")));

    LiatrioV1BuildSpec spec = build.getSpec();
    assertThat("spec.branch", spec.getBranch(), is(equalTo("PR-11111")));
    assertThat("spec.buildId", spec.getBuildId(), is(equalTo("2")));
    assertThat("spec.commitId", spec.getCommitId(), is(equalTo("123456789abcd")));
    assertThat("spec.product", spec.getProduct(), is(equalTo("chatops-dev")));
    assertThat("spec.type", spec.getType(), is(LiatrioV1BuildType.PullRequest));
    assertThat("spec.url", spec.getUrl(), is(equalTo("http://jenkins/job/url")));

    LiatrioV1Pipeline p = spec.getPipeline();
    assertThat("pipeline", p, is(notNullValue()));
    assertThat("pipeline.host", p.getHost(), is("www.github.com"));
    assertThat("pipeline.name", p.getName(), is("springtrader-test"));
    assertThat("pipeline.org", p.getOrg(), is("liatrio"));
    assertThat("pipeline.type", p.getType(), is(LiatrioV1PipelineType.github));
    assertThat("pipeline.url", p.getUrl(), is("https://www.github.com/liatrio/springtrader-test.git"));
  }

  @Test
  public void testParseGitUrlHttpsGithub() {
    LiatrioV1Pipeline p = LiatrioV1BuildController.parseGitUrl("https://www.github.com/liatrio/springtrader-test.git");

    assertThat("pipeline", p, is(notNullValue()));
    assertThat("pipeline.host", p.getHost(), is("www.github.com"));
    assertThat("pipeline.name", p.getName(), is("springtrader-test"));
    assertThat("pipeline.org", p.getOrg(), is("liatrio"));
    assertThat("pipeline.type", p.getType(), is(LiatrioV1PipelineType.github));
    assertThat("pipeline.url", p.getUrl(), is("https://www.github.com/liatrio/springtrader-test.git"));
  }

  @Test
  public void testParseGitUrlHttpGithub() {
    LiatrioV1Pipeline p = LiatrioV1BuildController.parseGitUrl("http://github.com/liatrio/springtrader-test.git");

    assertThat("pipeline", p, is(notNullValue()));
    assertThat("pipeline.host", p.getHost(), is("github.com"));
    assertThat("pipeline.name", p.getName(), is("springtrader-test"));
    assertThat("pipeline.org", p.getOrg(), is("liatrio"));
    assertThat("pipeline.type", p.getType(), is(LiatrioV1PipelineType.github));
    assertThat("pipeline.url", p.getUrl(), is("http://github.com/liatrio/springtrader-test.git"));
  }

  @Test
  public void testParseGitUrlGithub() {
    LiatrioV1Pipeline p = LiatrioV1BuildController.parseGitUrl("git@github.com:liatrio/lead-shared-library.git");

    assertThat("pipeline", p, is(notNullValue()));
    assertThat("pipeline.host", p.getHost(), is("github.com"));
    assertThat("pipeline.name", p.getName(), is("lead-shared-library"));
    assertThat("pipeline.org", p.getOrg(), is("liatrio"));
    assertThat("pipeline.type", p.getType(), is(LiatrioV1PipelineType.github));
    assertThat("pipeline.url", p.getUrl(), is("git@github.com:liatrio/lead-shared-library.git"));
  }

  @Test
  public void testBuildTypeMaster() {
    LiatrioV1BuildType type = LiatrioV1BuildController.buildType("master");
    assertThat("type",type,is(LiatrioV1BuildType.Master));
  }

  @Test
  public void testBuildTypePR() {
    LiatrioV1BuildType type = LiatrioV1BuildController.buildType("PR-12345");
    assertThat("type",type,is(LiatrioV1BuildType.PullRequest));
  }

  @Test
  public void testBuildTypeBranch() {
    LiatrioV1BuildType type = LiatrioV1BuildController.buildType("foo-bar");
    assertThat("type",type,is(LiatrioV1BuildType.Branch));
  }
}
