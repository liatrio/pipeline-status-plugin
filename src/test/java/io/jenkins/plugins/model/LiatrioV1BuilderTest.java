package io.jenkins.plugins.model;

import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import hudson.EnvVars;

import static org.hamcrest.CoreMatchers.*;


import io.kubernetes.client.models.V1Event;

public class LiatrioV1BuilderTest {
  private LiatrioV1Builder builder;

  @Before
  public void setupBuilder() {
    builder = new LiatrioV1Builder();
  }

  @After
  public void teardownBuilder() {
    builder = null;
  }

  @Test
  public void testCreate() {
    EnvVars vars = new EnvVars();
    vars.put("product", "chatops-dev");
    vars.put("GIT_URL", "https://www.github.com/liatrio/springtrader-test.git");
    vars.put("GIT_COMMIT", "123456789abcd");
    vars.put("GIT_BRANCH", "PR-11111");
    vars.put("BUILD_ID", "2");
    vars.put("JOB_DISPLAY_URL", "http://jenkins/job/url");

    LiatrioV1Build build = builder.withEnvVars(vars).build();

    assertThat("build", build, is(notNullValue()));
    assertThat("build.apiVersion", build.getApiVersion(), is(equalTo("stable.liatr.io/v1")));
    assertThat("build.kind", build.getKind(), is(equalTo("Build")));
    assertThat("build.meta.name", build.getMetadata().getName(),
        is(equalTo("chatops-dev-liatrio-springtrader-test-12345678-2")));

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
    LiatrioV1Pipeline p = builder.parseGitUrl("https://www.github.com/liatrio/springtrader-test.git");

    assertThat("pipeline", p, is(notNullValue()));
    assertThat("pipeline.host", p.getHost(), is("www.github.com"));
    assertThat("pipeline.name", p.getName(), is("springtrader-test"));
    assertThat("pipeline.org", p.getOrg(), is("liatrio"));
    assertThat("pipeline.type", p.getType(), is(LiatrioV1PipelineType.github));
    assertThat("pipeline.url", p.getUrl(), is("https://www.github.com/liatrio/springtrader-test.git"));
  }

  @Test
  public void testParseGitUrlHttpGithub() {
    LiatrioV1Pipeline p = builder.parseGitUrl("http://github.com/liatrio/springtrader-test.git");

    assertThat("pipeline", p, is(notNullValue()));
    assertThat("pipeline.host", p.getHost(), is("github.com"));
    assertThat("pipeline.name", p.getName(), is("springtrader-test"));
    assertThat("pipeline.org", p.getOrg(), is("liatrio"));
    assertThat("pipeline.type", p.getType(), is(LiatrioV1PipelineType.github));
    assertThat("pipeline.url", p.getUrl(), is("http://github.com/liatrio/springtrader-test.git"));
  }

  @Test
  public void testParseGitUrlGithub() {
    LiatrioV1Pipeline p = builder.parseGitUrl("git@github.com:liatrio/lead-shared-library.git");

    assertThat("pipeline", p, is(notNullValue()));
    assertThat("pipeline.host", p.getHost(), is("github.com"));
    assertThat("pipeline.name", p.getName(), is("lead-shared-library"));
    assertThat("pipeline.org", p.getOrg(), is("liatrio"));
    assertThat("pipeline.type", p.getType(), is(LiatrioV1PipelineType.github));
    assertThat("pipeline.url", p.getUrl(), is("git@github.com:liatrio/lead-shared-library.git"));
  }

  @Test
  public void testBuildLabels() {
    LiatrioV1Pipeline pipeline = builder.parseGitUrl("git@github.com:liatrio/lead-shared-library.git");
    EnvVars envVars = new EnvVars();
    envVars.put("product", "chatops-dev");

    Map<String, String> labels = builder.buildLabels(envVars, pipeline);

    assertThat("labels", labels, is(notNullValue()));
    assertThat("labels.product", labels.get("product"), is("chatops-dev"));
    assertThat("labels.pipeline_org", labels.get("pipeline_org"), is("liatrio"));
    assertThat("labels.pipeline_name", labels.get("pipeline_name"), is("lead-shared-library"));
    assertThat("labels.timestamp", labels.get("timestamp"), not(is("")));
  }

  @Test
  public void testBuildTypeMaster() {
    EnvVars envVars = new EnvVars();
    envVars.put("GIT_BRANCH", "master");
    LiatrioV1BuildType type = builder.buildType(envVars);
    assertThat("type",type,is(LiatrioV1BuildType.Master));
  }

  @Test
  public void testBuildTypePR() {
    EnvVars envVars = new EnvVars();
    envVars.put("GIT_BRANCH", "PR-12345");
    LiatrioV1BuildType type = builder.buildType(envVars);
    assertThat("type",type,is(LiatrioV1BuildType.PullRequest));
  }

  @Test
  public void testBuildTypeBranch() {
    EnvVars envVars = new EnvVars();
    envVars.put("GIT_BRANCH", "foo-bar-baz");
    LiatrioV1BuildType type = builder.buildType(envVars);
    assertThat("type",type,is(LiatrioV1BuildType.Branch));
  }
}
