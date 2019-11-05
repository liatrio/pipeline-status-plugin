package io.jenkins.plugins.kubernetes.controller;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Pipeline;
import io.jenkins.plugins.kubernetes.model.LiatrioV1PipelineType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;

public class LiatrioV1BuildController implements PipelineEventHandler {
  private static Logger logger = Logger.getLogger(LiatrioV1BuildController.class.getName());
  public static final String API_VERSION = "stable.liatr.io/v1";
  public static final String KIND = "Build";

  private NamespacedKubernetesClient client;

  public LiatrioV1BuildController(NamespacedKubernetesClient client) {
    this.client = client;
  }

  @Override
  public void handlePipelineStartEvent(PipelineEvent event) {
    logger.info("PipelineStartEvent --> Creating build CR");
    LiatrioV1Build build = asBuild(event);
    build.getSpec()
         .startTime(event.getTimestamp())
         .result(LiatrioV1ResultType.inProgress);

    client.resource(build).createOrReplace();
  }

  @Override
  public void handlePipelineEndEvent(PipelineEvent event) {
    logger.info("PipelineEndEvent --> Updating build CR");
    LiatrioV1Build build = asBuild(event);
    build.getSpec()
         .endTime(event.getTimestamp())
         .result(event.getError().map(t -> LiatrioV1ResultType.fail).orElse(LiatrioV1ResultType.success));

    ObjectMapper mapper = new ObjectMapper();
    try {
      logger.info(mapper.writeValueAsString(build));
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Error writing JSON", ex);
    }

    // TODO: patch the build resource
  }

  public static LiatrioV1Build asBuild(PipelineEvent event) {
    LiatrioV1Pipeline pipeline = parseGitUrl(event.getGitUrl());

    String name = buildName(event.getProduct(), pipeline.getOrg(), pipeline.getName(), String.valueOf(event.getTimestamp().getTime()));
    Map<String,String> labels = new HashMap<>();
    labels.put("product", event.getProduct());
    labels.put("pipeline_org", pipeline.getOrg());
    labels.put("pipeline_name", pipeline.getName());
    labels.put("timestamp", String.valueOf(event.getTimestamp().getTime()));
    ObjectMeta meta = new ObjectMeta();
    meta.setName(name);
    meta.setLabels(labels);

    LiatrioV1Build build = 
      new LiatrioV1Build()
        .apiVersion(API_VERSION).kind(KIND)
        .metadata(meta)
        .spec(new LiatrioV1BuildSpec()
          .branch(event.getBranch())
          .buildId(event.getBuildId())
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
    if (branch.equals("master")) {
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
            .map(String::getBytes)
            .forEach(md::update);
      return Base64.getEncoder().encodeToString(md.digest());
    } catch (Exception ex) {
      throw new RuntimeException("Unable to create a name for build",ex);
    }
  }
}
