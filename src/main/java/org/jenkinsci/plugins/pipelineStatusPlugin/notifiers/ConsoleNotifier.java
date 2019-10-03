package org.jenkinsci.plugins.pipelineStatusPlugin.notifiers;

import org.jenkinsci.plugins.pipelineStatusPlugin.BuildStageModel;

import java.util.Map;
import java.util.logging.Logger;

public class ConsoleNotifier extends BuildNotifier {
    private static Logger log = Logger.getLogger(ConsoleNotifier.class.toString());

    /**
     * Determine whether notifier is enabled.
     *
     * @return true if enabled; false otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Send a state change with timing info
     *
     * @param jobName   the name of the job
     * @param stageItem stage item
     */
    @Override
    public void notifyBuildStageStatus(String jobName, BuildStageModel stageItem) {
//        log.info("##### notifyBuildStageStatus:" + jobName + " " + stageItem.getStageName());
//        log.info("Job Name: " + jobName);
//        log.info("Stage Name: " + stageItem.getStageName());
//        log.info("Build State: " + stageItem.getBuildState().toString());
    }

    /**
     * Send a notification when the job is complete
     *
     * @param buildState state indicating success or failure
     * @param parameters build parameters
     */
    @Override
    public void notifyFinalBuildStatus(BuildState buildState, Map<String, Object> parameters) {
//        log.info("##### notifyFinalBuildStatus #####");
//        log.info("buildState: " + buildState);
//        log.info("parameters: ");
//        parameters.forEach((key, value) -> {
//            try {
//                log.info(key + ": " + value.toString());
//            }catch (NullPointerException e){
//                log.warning(e.getMessage());
//            }
//        });
    }
}
