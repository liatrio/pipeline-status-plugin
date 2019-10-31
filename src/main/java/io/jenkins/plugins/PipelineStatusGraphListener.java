package io.jenkins.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.*;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.*;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import io.kubernetes.client.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.apis.CustomObjectsApi;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepEnvironmentContributor;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import javax.annotation.Nonnull;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@Extension
public class PipelineStatusGraphListener extends StepEnvironmentContributor implements GraphListener {
    private static Logger log = Logger.getLogger(PipelineStatusGraphListener.class.getName());

    private List<String> Ids = new ArrayList<String>();

    private EnvVars envVars= new EnvVars();

    @Override
    public void buildEnvironmentFor(@Nonnull StepContext stepContext, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        envVars = envs;
    }

    public HashMap<String,Object> generateEvent(String eventName, String stageStatus, String start_time, String end_time){
        /*Basic sanitization of the job names, this can be expanded*/
        eventName = eventName.toLowerCase();
        eventName = eventName.replace(' ','-');
        String product =envVars.get("product", "unknown");
        String buildBranch = envVars.get("BRANCH_NAME", "master");
        String commitMessage = "unkown";
        String commitAuthor = "unknown";
        String startTime = start_time.equals("null") ? "null" : start_time;
        String endTime = end_time.equals("null") ? "null" : end_time;
        String job = envVars.get("JOB_NAME", "unknown").toLowerCase();
        if (job.contains("/")) {
            job = job.replace("/", ".");
        }
        String gitRepo = envVars.get("GIT_URL", "unknown.git").toLowerCase();
        gitRepo = gitRepo.substring(gitRepo.lastIndexOf("/") + 1);
        if (gitRepo.contains(".")) {
            int index = gitRepo.indexOf(".");
            gitRepo = gitRepo.substring(0, index);
        } 
        try {
            GitHubClient client = new GitHubClient();
            RepositoryService repoService = new RepositoryService(client);
            Repository repository = repoService.getRepository("Liatrio", gitRepo);
            CommitService commitService = new CommitService(client);
            Commit commit1 = commitService.getCommit(repository, buildBranch).getCommit();
            commitMessage = commit1.getMessage();
            commitAuthor = commit1.getAuthor().getName();

        } catch (Exception e) {
            log.info(e.toString());
        }

        long timestamp = Instant.now().toEpochMilli();

        /*
         * TODO Change the namespace to toolchain 
         * Preload the stages ahead of time 
         * Get the commit id, messages, and commiters from takumi
         *
         *
         * These are the variables that are retrieved by Jenkins
         * {__CF_USER_TEXT_ENCODING=0x1F5:0x0:0x0,
         * Apple_PubSub_Socket_Render=/private/tmp/com.apple.launchd.0dsQscHaqD/Render,
         * BRANCH_NAME=master, BUILD_DISPLAY_NAME=#2, BUILD_ID=2, BUILD_NUMBER=2,
         * BUILD_TAG=jenkins-multi-master-2, CLASSPATH=, EXECUTOR_NUMBER=0,
         * HOME=/Users/ahmedalsabag,
         * HUDSON_HOME=/Users/ahmedalsabag/Documents/liatrio/pipeline-status-plugin/
         * work, HUDSON_SERVER_COOKIE=081755e511485921,
         * JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home,
         * JAVA_MAIN_CLASS_12168=org.codehaus.plexus.classworlds.launcher.Launcher,
         * JENKINS_HOME=/Users/ahmedalsabag/Documents/liatrio/pipeline-status-plugin/
         * work, JENKINS_NODE_COOKIE=9bda2b87-fb9b-4464-bd56-f6b635ab1be3,
         * JENKINS_SERVER_COOKIE=081755e511485921, JOB_BASE_NAME=master,
         * JOB_DISPLAY_URL=http://unconfigured-jenkins-location/job/multi/job/master/
         * display/redirect, JOB_NAME=multi/master, LANG=en_US.UTF-8,
         * LOGNAME=ahmedalsabag, MAVEN_CMD_LINE_ARGS= hpi:run,
         * MAVEN_PROJECTBASEDIR=/Users/ahmedalsabag/Documents/liatrio/pipeline-status-
         * plugin, NODE_LABELS=master, NODE_NAME=master,
         * OLDPWD=/Users/ahmedalsabag/Documents/liatrio/pipeline-status-plugin,
         * PATH=/Users/ahmedalsabag/Documents/software:/Users/ahmedalsabag/Documents/
         * software/ruby-2.5.3/bin:/usr/local/opt/ruby/bin:/usr/local/bin:/usr/bin:/bin:
         * /usr/sbin:/sbin:/usr/local/go/bin,
         * PWD=/Users/ahmedalsabag/Documents/liatrio/pipeline-status-plugin,
         * RUN_CHANGES_DISPLAY_URL=http://unconfigured-jenkins-location/job/multi/job/
         * master/2/display/redirect?page=changes,
         * RUN_DISPLAY_URL=http://unconfigured-jenkins-location/job/multi/job/master/2/
         * display/redirect, SECURITYSESSIONID=186ab, SHELL=/bin/bash, SHLVL=1,
         * SSH_AUTH_SOCK=/private/tmp/com.apple.launchd.fB3FpHJtiZ/Listeners,
         * STAGE_NAME=Results, TERM=xterm-256color, TERM_PROGRAM=Apple_Terminal,
         * TERM_PROGRAM_VERSION=421.2,
         * TERM_SESSION_ID=C9841708-3872-43CA-A98E-D6D8FBBC7334,
         * TMPDIR=/var/folders/d1/j5m80b1n2yvg8qmkfjmkcb180000gn/T/, USER=ahmedalsabag,
         * WORKSPACE=/Users/ahmedalsabag/Documents/liatrio/pipeline-status-plugin/work/
         * workspace/multi_master, XPC_FLAGS=0x0, XPC_SERVICE_NAME=0}
         */
        String json = "{\n" +
                "   \"apiVersion\": \"stable.liatr.io/v1\",\n" +
                "   \"kind\": \"Build\",\n" +
                "   \"metadata\": {\n" +
                "      \"name\":  \"" + product + "-" + job + "-" + eventName + "-" + timestamp + "-" + envVars.get("BUILD_ID", "1") + "\",\n" +
                "      \"namespace\": \"default\",\n" +
                "      \"labels\": {\n" +
                "         \"pipeline\":  \"" + eventName + "-" + timestamp + "\",\n" +
                "         \"timestamp\": \"" + timestamp + "\"\n" +
                "      }\n" +
                "   },\n" +
                "   \"spec\": {\n" +
                "      \"branch\": \"" + envVars.get("GIT_BRANCH", "unknown") + "\",\n" +
                "      \"build_id\": \"" + envVars.get("BUILD_ID", "1") + "\",\n" +
                "      \"commit_id\": \"" + envVars.get("GIT_COMMIT", "unknown") + "\",\n" +
                "      \"commit_message\": \"" + commitMessage + "\",\n" +
                "      \"end_time\": \"" + endTime + "\",\n" +
                "      \"pipeline\": {\n" +
                "         \"host\": \"www.github.com\",\n" +
                "         \"name\": \"lead-shared-library\",\n" +
                "         \"org\": \"liatrio\",\n" +
                "         \"type\": \"unknown\",\n" +
                "         \"url\": \"" + envVars.get("JOB_DISPLAY_URL", "unknown") + "\"\n" +
                "      },\n" +
                "      \"product\": \"" + product + "\",\n" +
                "      \"result\": \"" + stageStatus + "\",\n" +
                "      \"committer\": \"" + commitAuthor + "\",\n" +
                "      \"stages\": [\n" +
                "         \"Build Stage\",\n" +
                "         \"Testing stage\",\n" +
                "         \"Deploy stage\"\n" +
                "      ],\n" +
                "      \"start_time\": \"" + startTime + "\",\n" +
                "      \"type\": \"" + envVars.get("GIT_BRANCH", "unknown") + "\",\n" +
                "      \"url\": \"" + envVars.get("JOB_DISPLAY_URL", "1") + "\"\n" +
                "   }\n" +
                "}";
        log.info(json);
        HashMap<String, Object> body = null;
        try {
            body = new ObjectMapper().readValue(json, HashMap.class);
        }catch (IOException e){
            log.info(e.toString());
        }
        return body;
    }

    @Override
    public void onNewHead(FlowNode flowNode) {
        //log.info(envVars.toString());

        try {
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);
            client.setDebugging(true);

            CustomObjectsApi apiInstance = new CustomObjectsApi();

            String group = "stable.liatr.io"; // String | The custom resource's group name
            String version = "v1"; // String | The custom resource's version

            String plural = "builds"; // String | The custom resource's plural name. For TPRs this would be lowercase plural kind.
            String pretty = "true"; // String | If 'true', then the output is pretty printed.
            HashMap<String, Object> body;
            Object result;

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz);
            String startTime = df.format(new Date());

            if (flowNode.getClass() == StepStartNode.class) {
                StepStartNode stepNode = (StepStartNode) flowNode;
                if (stepNode.isBody() && stepNode.getStepName().equals("Stage")) {
                    log.info("");
                    log.info("### Starting Stage for " + stepNode.getDisplayName() + " (" + stepNode.getId() + ") ###");
                    log.info("Display Name: " + stepNode.getDisplayName());
                    log.info("### /Starting Stage ###");
                    Ids.add(stepNode.getId());
                    log.info("");
                    log.info("STEPNODE CONTAINS " + stepNode);
                    body = generateEvent(stepNode.getDisplayName(), "inProgress", startTime, "null");
                    result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                    //log.info(result.toString());
                }
            }

            if (flowNode.getClass() == StepEndNode.class && Ids.contains(((StepEndNode) flowNode).getStartNode().getId())) {
                ErrorAction errorAction = flowNode.getError();
                String endTime = df.format(new Date());
                if(errorAction != null){
                    log.info("");
                    log.info("### Error Info ###");
                    log.warning("Error Action: " + errorAction);
                    log.warning("Error: " + errorAction.getError());
                    log.warning("Error Display Name: " + errorAction.getDisplayName());
                    log.info("### /Error Info ###");
                    log.info("");

                    body = generateEvent(errorAction.getDisplayName(), "fail", startTime, endTime);
                    result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                    //log.info(result.toString());
                }

                StepEndNode endNode = (StepEndNode) flowNode;
                log.info("");
                log.info("### Ending Stage for " +
                        ((StepEndNode) flowNode).getStartNode().getDisplayName() +
                        " (" + ((StepEndNode) flowNode).getStartNode().getId() + ") ###");
                log.info("Display Name: '" + endNode.getDisplayName() + "'");
                log.info("### /Ending Stage ###");
                log.info("");

                body = generateEvent(((StepEndNode) flowNode).getStartNode().getDisplayName(), "success", startTime, endTime);
                result = apiInstance.createNamespacedCustomObject(group, version, "default", plural, body, pretty);
                //log.info(result.toString());

            }
        } catch (Exception exception) {
            log.info("exception caught");
            log.info(exception.toString());

        }
    }

}
