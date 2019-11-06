package io.jenkins.plugins;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.EnvVars;
import hudson.plugins.git.GitSCM;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.triggers.SCMTrigger;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Build;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildList;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildSpec;
import io.jenkins.plugins.kubernetes.model.LiatrioV1BuildType;
import io.jenkins.plugins.kubernetes.model.LiatrioV1Client;
import io.jenkins.plugins.kubernetes.model.LiatrioV1ResultType;

public class PipelineEventGraphListenerIT {
    @Rule 
    public JenkinsRule j = new JenkinsRule();

    @Rule 
    public KubernetesServer server = new KubernetesServer(true, true);

    @Test 
    public void customizeWorkspace() throws Exception {
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
    }

    private String getResource(String name) throws Exception {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("Jenkinsfile.simple");
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
