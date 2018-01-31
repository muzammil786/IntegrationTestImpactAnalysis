/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import com.ueas.tia.config.Configuration;
import com.ueas.tia.config.Constants;
import com.ueas.tia.diffj.DiffJManager;
import com.ueas.tia.exceptions.TargetProgramManagerException;
import com.ueas.tia.git.GitRepositoryManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TargetProgramManager {
  private static final Logger LOGGER = LogManager.getLogger(TargetProgramManager.class);

  private GitRepositoryManager gitRepositoryManager = new GitRepositoryManager();
  private DiffJManager diffJManager = new DiffJManager();

  /**
   * Downloads the target repo from the URL set in target.repo property.
   */
  public void downloadRepo() throws TargetProgramManagerException {
    String targetRepo = Configuration.getConfiguration().getProperty("target.repo");
    boolean success = gitRepositoryManager.download(targetRepo, Constants.REPO_PATH);
    if (!success) {
      throw new TargetProgramManagerException("Could not download the target repo");
    }
  }

  /**
   * Get the list of method changes in the last commit of the target repo.
   */
  public Set<String> getMethodsChanged() {
    // get the list of files changed
    final String[] changedFiles = gitRepositoryManager.getChangedFiles(Constants.REPO_PATH);
    LOGGER.info("Files changed in last commit: ");
    Arrays.stream(changedFiles).forEach(LOGGER::debug);
    Set<String> methodsChanged = new HashSet<>();

    Arrays.stream(changedFiles)
        .map(String::trim)
        .filter(changedFile -> changedFile.toLowerCase().endsWith(".java"))
        .forEach(changedFile -> {
          // get the last version of the file changed
          LOGGER.debug("Downloading the previous version of " + changedFile);
          boolean success = gitRepositoryManager.downloadLastVersion(changedFile, Constants.REPO_PATH, Constants
              .PRE_VERSION_SUFFIX);
          // compare the two versions to get the list of methods changed (if any) 
          if (success) {
            Set<String> methodChangedInThisFile = diffJManager.getMethodsChanged(Constants.REPO_PATH + "/" + changedFile,
                Constants.REPO_PATH + "/" + changedFile + Constants.PRE_VERSION_SUFFIX);
            methodsChanged.addAll(methodChangedInThisFile);
          }
        });
    return methodsChanged;
  }

}