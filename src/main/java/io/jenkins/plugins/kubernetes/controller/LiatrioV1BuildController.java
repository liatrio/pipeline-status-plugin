package io.jenkins.plugins.kubernetes.controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.Gson;

import io.jenkins.plugins.PipelineEvent;
import io.jenkins.plugins.PipelineEventHandler;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Pipeline;
import io.jenkins.plugins.kubernetes.model.LiatrioV1PipelineType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.ClientBuilder;

public class LiatrioV1BuildController implements PipelineEventHandler {
  private static Logger logger = Logger.getLogger(LiatrioV1BuildController.class.getName());
  public static final String API_VERSION = "stable.liatr.io/v1";
  public static final String KIND = "Build";

  // TODO: not testable
  static {
      ApiClient client;
      try {
          client = ClientBuilder.cluster().build();
          Configuration.setDefaultApiClient(client);
          client.setDebugging(true);
      } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to initialize kubernetes client", e);
      }
  }

  @Override
  public void handlePipelineStartEvent(PipelineEvent event) {
    logger.info("PipelineStartEvent --> Creating build CR");
    LiatrioV1Build build = asBuild(event);
    build.getSpec()
         .startTime(event.getTimestamp())
         .result(LiatrioV1ResultType.inProgress);

    Gson gson = new Gson();
    logger.info(gson.toJson(build));
        /*
        String group = "stable.liatr.io"; // String | The custom resource's group name
        String version = "v1"; // String | The custom resource's version
        String plural = "builds"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
        CustomObjectsApi apiInstance = new CustomObjectsApi();
        apiInstance.createNamespacedCustomObject(group, version, "default", plural, build, true);
        */
  }

  @Override
  public void handlePipelineEndEvent(PipelineEvent event) {
    logger.info("PipelineEndEvent --> Updating build CR");
    LiatrioV1Build build = asBuild(event);
    build.getSpec()
         .endTime(event.getTimestamp())
         .result(event.getError().map(t -> LiatrioV1ResultType.fail).orElse(LiatrioV1ResultType.success));

    Gson gson = new Gson();
    logger.info(gson.toJson(build));
  }

  public static LiatrioV1Build asBuild(PipelineEvent event) {
    LiatrioV1Pipeline pipeline = parseGitUrl(event.getGitUrl());
    LiatrioV1Build build = 
      new LiatrioV1Build()
        .apiVersion(API_VERSION).kind(KIND)
        .metadata(new V1ObjectMeta()
          .name(buildName(event.getProduct(), pipeline.getOrg(), pipeline.getName(), String.valueOf(event.getTimestamp().getTime())))
          .putLabelsItem("product", event.getProduct())
          .putLabelsItem("pipeline_org", pipeline.getOrg())
          .putLabelsItem("pipeline_name", pipeline.getName())
          .putLabelsItem("timestamp", String.valueOf(event.getTimestamp().getTime())))
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
