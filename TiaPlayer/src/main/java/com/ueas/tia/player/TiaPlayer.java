/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import com.jcraft.jsch.SftpException;
import com.ueas.tia.exceptions.JvmSnifferException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TiaPlayer {

  private static final Logger LOGGER = LogManager.getLogger(TiaPlayer.class);

  private TargetProgramManager targetProgramManager;
  private AcceptanceTestsManager acceptanceTestsManager;
  private JvnSnifferManager jvnSnifferManager;

  /**
   * Constructor.
   */
  public TiaPlayer() {
    targetProgramManager = new TargetProgramManager();
    acceptanceTestsManager = new AcceptanceTestsManager();
    jvnSnifferManager = new JvnSnifferManager();
  }

  /**
   * Returns the list of tags to run by checking if the method trace for each given tag is included in the given change list.
   */
  public Set<String> getTagsToRun(Set<String> tags, Set<String> methodChangeSet) {
    LOGGER.info("List of tags: " + tags.toString());
    Set<String> tagsToRunSet = new HashSet<>(tags.size());
    // check which tags should we run 
    tags.stream()
        .map(String::trim)
        // ignore comments
        .filter(tag -> !tag.startsWith("#"))
        // for each tag, get its method trace and check if this includes any method in the methodChangeSet
        .forEach(tag -> {
          String methodTrace = "";
          try {
            // get the method trace file for the tag if available
            methodTrace = jvnSnifferManager.getMethodTraceForTag(tag);
          } catch (SftpException exception) {
            // normally this exception occurs when no file is found. 
            // This is normal when JvmSniffer never ran for this tag
            LOGGER.warn(exception.getMessage());
          } catch (Exception exception) {
            LOGGER.error("Exception occurred while reading method trace for tag: " + tag, exception);
          }
          // if no trace is found or if trace contains one of the method changed
          if (methodTrace.isEmpty() || isAnyMethodChangedInTrace(methodTrace, methodChangeSet)) {
            tagsToRunSet.add(tag);
          } else {
            LOGGER.info("No method changes are found for tag: " + tag);
          }
        });
    LOGGER.info("Number of tags to run: " + tagsToRunSet.size());
    LOGGER.info("Tags to run: " + tagsToRunSet.toString());
    return tagsToRunSet;
  }

  /**
   * Returns true of any trace in the method traces found in the method changed set.
   */
  public boolean isAnyMethodChangedInTrace(String methodTrace, Set<String> methodChangeSet) {
    return methodChangeSet
        .stream()
        .anyMatch(methodTrace::contains);
  }

  /**
   * Executes cucumber tests with the given tags with sniffer start/stop before and after respectively.
   */
  public void executeTestsWithSniffer(Set<String> tagsToRunSet) {

    tagsToRunSet.forEach(tagsToRun -> {
      try {
        jvnSnifferManager.startSniffer();
        // execute tests
        acceptanceTestsManager.runCucumberTest(
            Arrays.asList(tagsToRun.split("\\s")),
            "@Ignore");
        jvnSnifferManager.stopSniffer();
        jvnSnifferManager.renameTraceFile(tagsToRun);
        Thread.sleep(2000);
      } catch (JvmSnifferException ex) {
        throw new RuntimeException(ex);
      } catch (Exception e) {
        LOGGER.error("Error occurred during test execution", e);
      }
    });
  }

  /**
   * Main run of the Test Impact Analyzer.
   */
  public void run() {
    try {
      // get the method change list from the last commit
      Set<String> methodChangeSet;
      targetProgramManager.downloadRepo();
      methodChangeSet = targetProgramManager.getMethodsChanged();
      if (methodChangeSet.isEmpty()) {
        LOGGER.info("No methods are changed in the last commit. Aborting.");
        return;
      }

      LOGGER.info("List of methods changed in the last commit: " + methodChangeSet.toString());
      // download test repo and target program repo
      acceptanceTestsManager.downloadTestRepo();
      // get tags info
      Set<String> allTags = acceptanceTestsManager.getTags();
      // find the impact change and run the relevant tests only
      if (!allTags.isEmpty()) {
        Set<String> tagsToRunSet = getTagsToRun(allTags, methodChangeSet);
        executeTestsWithSniffer(tagsToRunSet);
      } else {
        LOGGER.warn("No tags are found in the test repo. Aborting");
        return;
      }
    } catch (Exception exception) {
      LOGGER.error(exception);
    }
  }

  /**
   * Main method to play TIA.
   */
  public static void main(String[] args) {
    try {
      TiaPlayer player = new TiaPlayer();
      player.run();
    } catch (Exception e) {
      LOGGER.error(e);
    }
  }


}