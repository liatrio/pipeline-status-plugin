package io.jenkins.plugins.kubernetes.controller;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Client;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Pipeline;
import io.jenkins.plugins.kubernetes.model.LiatrioV1PipelineType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;

public class LiatrioV1BuildController implements PipelineEventHandler {
  private static Logger logger = Logger.getLogger(LiatrioV1BuildController.class.getName());
  public static final String LIATRIO_GROUP = "stable.liatr.io";
  public static final String LIATRIO_VERSION = "v1";
  public static final String KIND = "Build";
  private LiatrioV1Client client;
  private String namespace;

  public LiatrioV1BuildController(NamespacedKubernetesClient client) {
    this.client = new LiatrioV1Client(client);
    this.namespace = Optional.ofNullable(client.getNamespace()).orElse("default");
  }

  @Override
  public void handlePipelineStartEvent(PipelineEvent event) {
    logger.info("PipelineStartEvent --> Creating build CR");
    LiatrioV1Build build = asBuild(event);
    build.getSpec()
         .result(LiatrioV1ResultType.inProgress);

    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }

  @Override
  public void handlePipelineEndEvent(PipelineEvent event) {
    logger.info("PipelineEndEvent --> Updating build CR");
    LiatrioV1Build build = asBuild(event);
    build.getSpec()
         .endTime(new Date())
         .result(event.getError().map(t -> LiatrioV1ResultType.fail).orElse(LiatrioV1ResultType.success));

    client.builds().inNamespace(this.namespace).createOrReplace(build);
  }

  public static LiatrioV1Build asBuild(PipelineEvent event) {
    LiatrioV1Pipeline pipeline = parseGitUrl(event.getGitUrl());

    String name = buildName(event.getProduct(), pipeline.getOrg(), pipeline.getName(), String.valueOf(event.getTimestamp().getTime()));
    LiatrioV1Build build = 
      new LiatrioV1Build()
        .apiVersion(LIATRIO_GROUP+"/"+LIATRIO_VERSION).kind(KIND)
        .metadata(new ObjectMetaBuilder()
          .withName(name)
          .addToLabels("product", event.getProduct())
          .addToLabels("pipeline_org", pipeline.getOrg())
          .addToLabels("pipeline_name", pipeline.getName())
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
    }

    return pipeline;
  }

  public static String buildName(String... parts) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      Stream.of(parts)
            .filter(Objects::nonNull)
            .map(String::getBytes)
            .forEach(md::update);
      return new BigInteger(1, md.digest()).toString(16);
    } catch (Exception ex) {
      throw new RuntimeException("Unable to create a name for build",ex);
    }
  }
}
