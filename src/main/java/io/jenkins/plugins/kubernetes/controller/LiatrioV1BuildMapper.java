package io.jenkins.plugins.kubernetes.controller;

<<<<<<< HEAD
import java.math.BigInteger;
import java.security.MessageDigest;
=======
>>>>>>> origin/master
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Pipeline;
import io.jenkins.plugins.kubernetes.model.LiatrioV1PipelineType;

public class LiatrioV1BuildMapper {
  public static final String LIATRIO_GROUP = "stable.liatr.io";
  public static final String LIATRIO_VERSION = "v1";
  public static final String KIND = "Build";


  public static LiatrioV1Build asBuild(PipelineEvent event) {
    LiatrioV1Pipeline pipeline = parseGitUrl(event.getGitUrl());

    String productPipelineHash = "";
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      messageDigest.reset();
      messageDigest.update(event.getProduct().getBytes("UTF-8"));
      messageDigest.update(pipeline.getUrl().getBytes("UTF-8"));
      productPipelineHash = new BigInteger(1, messageDigest.digest()).toString(16);
    } catch (Exception ex) {}

    LiatrioV1Build build = 
      new LiatrioV1Build()
        .apiVersion(LIATRIO_GROUP+"/"+LIATRIO_VERSION).kind(KIND)
        .metadata(new ObjectMetaBuilder()
          .withName(event.getBuildName())
          .addToLabels("product", event.getProduct())
          .addToLabels("pipelineOrg", pipeline.getOrg())
          .addToLabels("pipelineName", pipeline.getName())
          .addToLabels("productPipelineHash", productPipelineHash)
          .addToLabels("timestamp", String.valueOf(event.getTimestamp().getTime()))
          .build())
        .spec(new LiatrioV1BuildSpec()
          .branch(event.getBranch())
          .buildId(event.getBuildId())
          .startTime(event.getTimestamp())
          .commitId(event.getCommitId())
          .commitMessage(event.getCommitMessage())
          .committers(event.getCommitters())
          .pipeline(pipeline)
          .product(event.getProduct())
          .stages(event.getStages())
          .type(buildType(event.getBranch()))
          .url(event.getJobDisplayUrl()));
    return build;
  }

  public static LiatrioV1BuildType buildType(String branch) {
    if(branch == null) {
      return null;
    } if (branch.equals("master")) {
      return LiatrioV1BuildType.Master;
    } else if (branch.startsWith("PR-")) {
      return LiatrioV1BuildType.PullRequest;
    } else {
      return LiatrioV1BuildType.Branch;
    }
  }

  public static LiatrioV1Pipeline parseGitUrl(String gitUrl) {
    LiatrioV1Pipeline pipeline = new LiatrioV1Pipeline();

    if (gitUrl != null) {
      Pattern p = Pattern.compile("(https://|http://|git@)([^/]+)(/|:)([^/]+)/([^/]+)");
      Matcher m = p.matcher(gitUrl);
      if (m.matches()) {
        if (m.group(2).endsWith("github.com")) {
          pipeline
              .type(LiatrioV1PipelineType.github)
              .url(gitUrl).host(m.group(2))
              .name(m.group(5)
              .substring(0, m.group(5).length() - 4))
              .org(m.group(4));
        }
      }
    } else {
      return pipeline.type(LiatrioV1PipelineType.github)
      .url("")
      .name("")
      .org("");
        
    }

    return pipeline;
  }
}
