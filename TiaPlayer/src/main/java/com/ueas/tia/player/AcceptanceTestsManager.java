/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import com.ueas.tia.config.Configuration;
import com.ueas.tia.config.Constants;
import com.ueas.tia.exceptions.AcceptanceTestsManagerException;
import com.ueas.tia.git.GitRepositoryManager;
import com.ueas.tia.utils.ProcessBuilderWrapper;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AcceptanceTestsManager {
  private static final Logger LOGGER = LogManager.getLogger(AcceptanceTestsManager.class);

  /**
   * Downloads the test repo from the URL in test.repo property.
   */
  public void downloadTestRepo() throws AcceptanceTestsManagerException {
    String targetRepo = Configuration.getConfiguration().getProperty("test.repo");
    GitRepositoryManager gitRepositoryManager = new GitRepositoryManager();
    boolean success = gitRepositoryManager.download(targetRepo, Constants.TEST_REPO_PATH);
    if (!success) {
      throw new AcceptanceTestsManagerException("Could not download the test repo");
    }
  }

  /**
   * Get the list of tags from the Acceptance test repo. It reads the file called tags.
   */
  public Set<String> getTags() {
    String filename = Constants.TEST_REPO_PATH + File.separator + Constants.TAGS_FILE_NAME;
    try (Stream<String> stream = Files.lines(Paths.get(filename))) {
      return stream.collect(Collectors.toSet());
    } catch (Exception ex) {
      LOGGER.error(ex);
      return new HashSet<>(0);
    }
  }

  /**
   * Runs cucumber tests.
   *
   * @param tags      - List of tags to run
   * @param ignoreTag - tag to ignore or null
   */
  public void runCucumberTest(List<String> tags, String ignoreTag) {
    String options = getCucumberOptions(tags, ignoreTag);
    LOGGER.info("Running cucumber tests with options " + options);
    String[] command = {
        "bash",
        "-c",
        "\"./gradlew -d clean test '-D" + options + "'\""
    };
    try {
      new ProcessBuilderWrapper()
          .showOutputOnConsole(true)
          .execute(new File(Constants.TEST_REPO_PATH), command);
    } catch (Exception e) {
      LOGGER.error(e);
    }
  }

  /**
   * Builds cucumber options.
   */
  public String getCucumberOptions(List<String> tags, String ignoreTag) {
    StringBuilder builder = new StringBuilder();
    builder.append("cucumber.options=");
    tags.forEach(tag -> builder.append("--tags ").append(tag).append(" "));
    if (StringUtils.isNotBlank(ignoreTag)) {
      builder.append("--tags ~").append(ignoreTag);
    }
    return builder.toString();
  }

}