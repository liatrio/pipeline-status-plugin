package io.jenkins.plugins.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import hudson.EnvVars;
import io.kubernetes.client.models.V1ObjectMeta;

public class LiatrioV1Builder {
  public static final String API_VERSION = "stable.liatr.io/v1";
  public static final String KIND = "Build";

  private LiatrioV1Build build = new LiatrioV1Build();

  public LiatrioV1Builder withEnvVars(EnvVars envVars) {
    LiatrioV1Pipeline pipeline = parseGitUrl(envVars.get("GIT_URL"));
    String buildName = buildName(envVars, pipeline);
    Map<String, String> buildLabels = buildLabels(envVars, pipeline);
    build.apiVersion(API_VERSION)
         .kind(KIND)
         .metadata(new V1ObjectMeta().name(buildName).labels(buildLabels))
         .spec(new LiatrioV1BuildSpec()
                    .branch(envVars.get("GIT_BRANCH", "unknown"))
                    .buildId(envVars.get("BUILD_ID", "1"))
                    .commitId(envVars.get("GIT_COMMIT", "unknown"))
                    //.commitMessage("TODO!!!")
                    //.committers(Lists.newArrayList("TODO!!!"))
                    //.endTime()
                    .product(envVars.get("product", "unknown"))
                    .pipeline(pipeline)
                    //.result()
                    //.stages()
                    //.startTime()
                    .type(buildType(envVars))
                    .url(envVars.get("JOB_DISPLAY_URL","")));
    return this;
  }

  public LiatrioV1Build build() {
    return build;
  }

  public LiatrioV1BuildType buildType(EnvVars envVars) {
    String branch = envVars.get("GIT_BRANCH","");
    if(branch.equals("master")) {
      return LiatrioV1BuildType.Master;
    } else if (branch.startsWith("PR-")) {
      return LiatrioV1BuildType.PullRequest;
    } else {
      return LiatrioV1BuildType.Branch;
    }
  }

  public Map<String,String> buildLabels(EnvVars envVars, LiatrioV1Pipeline pipeline) {
    Map<String, String> labels = new HashMap<>();
    labels.put("product", envVars.get("product",""));
    labels.put("pipeline_org", pipeline.getOrg());
    labels.put("pipeline_name", pipeline.getName());
    labels.put("timestamp", String.valueOf(System.currentTimeMillis()));
    return labels;
  }

  public LiatrioV1Pipeline parseGitUrl(String gitUrl) {
    LiatrioV1Pipeline pipeline = new LiatrioV1Pipeline();

    if(gitUrl != null) {
      Pattern p = Pattern.compile("(https://|http://|git@)([^/]+)(/|:)([^/]+)/([^/]+)");
      Matcher m = p.matcher(gitUrl);
      if(m.matches()) {
        if(m.group(2).endsWith("github.com")) {
          pipeline.type(LiatrioV1PipelineType.github)
                  .url(gitUrl)
                  .host(m.group(2))
                  .name(m.group(5).substring(0,m.group(5).length() - 4))
                  .org(m.group(4));
        }
      }
    }

    return pipeline;
  }

  public static String buildName(EnvVars envVars, LiatrioV1Pipeline pipeline) {
    String pipelineOrg = pipeline.getOrg();
    if(pipelineOrg != null) {
      pipelineOrg = pipelineOrg.toLowerCase();
    }
    String pipelineName = pipeline.getName();
    if(pipelineName != null) {
      pipelineName = pipelineName.toLowerCase();
    }
    String commitId = envVars.get("GIT_COMMIT", "unknown");
    if(commitId.length() > 8) {
      commitId = commitId.substring(0, 8);
    }
    return String.format("%s-%s-%s-%s-%s", 
                         envVars.get("product", "unknown"),
                         pipelineOrg,
                         pipelineName,
                         commitId,
                         envVars.get("BUILD_ID", "1"));
  }
}
