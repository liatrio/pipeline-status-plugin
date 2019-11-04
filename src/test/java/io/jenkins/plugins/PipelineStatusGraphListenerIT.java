package io.jenkins.plugins;

import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PipelineStatusGraphListenerIT {
    @Rule 
    public JenkinsRule j = new JenkinsRule();

    @Test 
    public void customizeWorkspace() throws Exception {
      String sampleJenkinsfile = getResource("Jenkinsfile.simple");

      // Create a new Pipeline with the given (Scripted Pipeline) definition
      WorkflowJob project = j.createProject(WorkflowJob.class);
      project.setDefinition(new CpsFlowDefinition(sampleJenkinsfile, true));

      // Run pipeline and get logs
      j.buildAndAssertSuccess(project);

      // TODO: verify that build resource was created
    }

    private String getResource(String name) throws Exception {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("Jenkinsfile.simple");
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
