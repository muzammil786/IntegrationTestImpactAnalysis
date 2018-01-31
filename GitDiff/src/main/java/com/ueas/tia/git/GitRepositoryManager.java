/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.git;


import com.ueas.tia.utils.ProcessBuilderWrapper;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Manages a GIT repo.
 */
public class GitRepositoryManager {

  private static final Logger LOGGER = LogManager.getLogger(GitRepositoryManager.class);

  /**
   * Cleans the path by removing all files.
   */
  public void clean(String repoPath) throws IOException {
    LOGGER.info("Cleaning " + repoPath);
    Path rootPath = Paths.get(repoPath);
    if (rootPath.toFile().exists()) {
      Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  /**
   * Downloads a GIT repo from repoUrl into repoPath.
   */
  public boolean download(String repoUrl, String repoPath) {
    String[] command = {
        "git",
        "clone",
        repoUrl,
        repoPath
    };
    try {
      clean(repoPath);
      ProcessBuilderWrapper pbd = new ProcessBuilderWrapper();
      pbd.execute(command);

      if (pbd.isSuccessful()) {
        LOGGER.info("Response: " + pbd.getInfos());
        return true;
      }
      LOGGER.error("Error: " + pbd.getErrors());
    } catch (Exception e) {
      LOGGER.error(e);
    }
    return false;
  }

  /**
   * Get the list of changed files in the last commit in the repo in repoPath.
   */
  public String[] getChangedFiles(String repoPath) {
    // git diff-tree --no-commit-id --name-only -r HEAD
    String[] command = {
        "git",
        "diff-tree",
        "--no-commit-id",
        "--name-only",
        "-r",
        "HEAD"
    };
    try {
      ProcessBuilderWrapper pbd = new ProcessBuilderWrapper();
      pbd.execute(new File(repoPath), command);
      if (pbd.isSuccessful()) {
        String response = pbd.getInfos();
        LOGGER.info("Response: " + response);
        return response.split("\\n");
      }
      LOGGER.error("Error: " + pbd.getErrors());
    } catch (Exception e) {
      LOGGER.error(e);
    }
    return new String[0];
  }

  /**
   * Downloads the last version of the file from the GIT repo in REPO_PATH and exports to REPO_PATH + "/" + file +
   * PRE_VERSION_SUFFIX.
   */
  public boolean downloadLastVersion(String file, String repoPath, String lastVersionSuffix) {
    String[] command = {
        "git",
        "show",
        "HEAD^:" + file
    };
    try {
      ProcessBuilderWrapper pbd = new ProcessBuilderWrapper();
      pbd.execute(new File(repoPath), command);
      if (pbd.isSuccessful()) {
        String response = pbd.getInfos();
        try (FileWriter fileWriter = new FileWriter(new File(repoPath + File.separator + file + lastVersionSuffix))) {
          fileWriter.write(response);
        }
        return true;
      }
      LOGGER.error("Error: " + pbd.getErrors());
    } catch (Exception e) {
      LOGGER.error(e);
    }
    return false;
  }

}
