package io.jenkins.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.jenkinsci.plugins.gitclient.Git;
import jenkins.MasterToSlaveFileCallable;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.util.BuildData;
import hudson.remoting.VirtualChannel;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;

@Extension
public class PipelineEventSCMListener extends SCMListener {
  private static final Logger logger = Logger.getLogger(PipelineEventSCMListener.class.getName());

  @Override
  public void onCheckout(Run<?, ?> build, SCM scm, FilePath workspace, TaskListener listener, File changelogFile,
      SCMRevisionState pollingBaseline) throws Exception {
    if (scm instanceof GitSCM) {
      GitSCM gitSCM = (GitSCM) scm;
      BuildData buildData = gitSCM.getBuildData(build);
      CheckoutActionBuilder builder = new CheckoutActionBuilder(buildData);
      CheckoutAction action = workspace.act(builder);
      logger.info(() -> "Attaching action: "+action);
      build.addAction(action);
    }
  }

  private static class CheckoutActionBuilder extends MasterToSlaveFileCallable<CheckoutAction> {
    private static final long serialVersionUID = -8527731677169346677L;
    String repoUrl = "";
    String branch = "";
    String commitId = "";
    String commitMessage = "";
    List<String> committers = new ArrayList<>();

    CheckoutActionBuilder(BuildData buildData) {
      this.branch = buildData.getLastBuiltRevision().getBranches().stream().map(Branch::getName).findFirst()
          .orElse(null);
      if (branch.contains("/")) {
        this.branch = branch.split("/")[1];
      }
      this.repoUrl = buildData.getRemoteUrls().stream().findFirst().orElse(null);
      this.commitId = buildData.getLastBuiltRevision().getBranches().stream().map(Branch::getSHA1String).findFirst()
          .orElse(null);

    }

    public CheckoutAction invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
      try {
        Git.with(null, null).in(file).getClient().withRepository((repo, c) -> {
          try (RevWalk walk = new RevWalk(repo)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(commitId));
            this.commitMessage = commit.getFullMessage();
            this.committers.add(commit.getAuthorIdent().getEmailAddress());
            walk.dispose();
          }
          return null;
        });
      } catch (GitException e) {
        logger.log(Level.SEVERE, "Unable to get git information", e);
      }
      return new CheckoutAction(repoUrl, branch, commitId, commitMessage, committers);
    }
  }
}
