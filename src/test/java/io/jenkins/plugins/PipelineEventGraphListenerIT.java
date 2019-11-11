package io.jenkins.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.EnvVars;
import hudson.plugins.git.GitSCM;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildList;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Client;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;

public class PipelineEventGraphListenerIT {
    @Rule 
    public JenkinsRule j = new JenkinsRule();

    @Rule 
    public KubernetesServer server = new KubernetesServer(true, true);

    @Test 
    public void testPipelineFromGitHub() throws Exception {
      EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
      EnvVars envVars = prop.getEnvVars();
      envVars.put("product", "sample-product");
      j.jenkins.getGlobalNodeProperties().add(prop);

      // Create a new Pipeline with the given (Scripted Pipeline) definition
      WorkflowJob project = j.createProject(WorkflowJob.class);
      GitSCM scm = new GitSCM("https://github.com/liatrio/pipeline-status-plugin.git");
      project.setDefinition(new CpsScmFlowDefinition(scm, "src/test/resources/Jenkinsfile.simple"));

      NamespacedKubernetesClient client = server.getClient().inNamespace("default");
      PipelineEventGraphListener.setClient(client);

      // Run pipeline and get logs
      j.buildAndAssertSuccess(project);


      LiatrioV1Client liatrioClient = new LiatrioV1Client(client);
      LiatrioV1BuildList builds = liatrioClient.builds().list();
      assertNotNull(builds);
      assertEquals("build count", 1, builds.getItems().size());
      LiatrioV1Build build = builds.getItems().get(0);
      LiatrioV1BuildSpec spec = build.getSpec();
      assertEquals("result", LiatrioV1ResultType.success, spec.getResult());
      assertNotEquals("end_time", "", spec.getEndTime());
      liatrioClient.close();

      EventList events = client.events().list();
      assertNotNull(events);
      assertEquals(8, events.getItems().size());
      assertEquals("first event = build", build.getMetadata().getName(), events.getItems().get(0).getInvolvedObject().getName());
      assertEquals("last event = build", build.getMetadata().getName(), events.getItems().get(7).getInvolvedObject().getName());


    }

    @Test 
    public void testStageMessageStep() throws Exception {
      EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
      EnvVars envVars = prop.getEnvVars();
      envVars.put("product", "sample-product");
      j.jenkins.getGlobalNodeProperties().add(prop);

      // Create a new Pipeline with the given (Scripted Pipeline) definition
      WorkflowJob project = j.createProject(WorkflowJob.class);
      GitSCM scm = new GitSCM("https://github.com/liatrio/pipeline-status-plugin.git");
      project.setDefinition(new CpsScmFlowDefinition(scm, "src/test/resources/Jenkinsfile.simple"));

      NamespacedKubernetesClient client = server.getClient().inNamespace("default");
      PipelineEventGraphListener.setClient(client);

      // Run pipeline and get logs
      j.buildAndAssertSuccess(project);

      LiatrioV1Client liatrioClient = new LiatrioV1Client(client);
      LiatrioV1BuildList builds = liatrioClient.builds().list();
      assertNotNull(builds);
      assertEquals("build count", 1, builds.getItems().size());
      LiatrioV1Build build = builds.getItems().get(0);
      LiatrioV1BuildSpec spec = build.getSpec();
      assertEquals("result", LiatrioV1ResultType.success, spec.getResult());
      assertNotEquals("end_time", "", spec.getEndTime());

      EventList events = client.events().list();
      assertNotNull(events);
      assertEquals(8, events.getItems().size());

      Event stage2EndEvent = events
        .getItems()
        .stream()
        .filter(e -> "stage".equals(e.getMetadata().getLabels().get("type")))
        .filter(e -> "stage success".equals(e.getMessage()))
        .filter(e -> "Stage2".equals(e.getMetadata().getAnnotations().get("stageName")))
        .findFirst()
        .orElse(null);

      assertEquals("stageName", "Stage2", stage2EndEvent.getMetadata().getAnnotations().get("stageName"));
      assertEquals("statusMessage", "sample status message", stage2EndEvent.getMetadata().getAnnotations().get("statusMessage"));

      liatrioClient.close();
    }
}
