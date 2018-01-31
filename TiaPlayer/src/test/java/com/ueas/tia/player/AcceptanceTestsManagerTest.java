/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.ueas.tia.config.Configuration;
import com.ueas.tia.config.Constants;
import com.ueas.tia.git.GitRepositoryManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AcceptanceTestsManagerTest {
  private static final Logger LOGGER = LogManager.getLogger(AcceptanceTestsManagerTest.class);
  private GitRepositoryManager repositoryManager = new GitRepositoryManager();
  private AcceptanceTestsManager acceptanceTestsManager = new AcceptanceTestsManager();

  /**
   * Clones the test repo.
   */
  @Before
  public void setup() {
    // download
    String url = Configuration.getConfiguration().getProperty("test.repo");
    repositoryManager.download(url, Constants.TEST_REPO_PATH);
    assertTrue(Files.exists(Paths.get(Constants.TEST_REPO_PATH + "/.git")));
  }

  @Test
  public void getTags() {
    try {
      Set<String> tags = acceptanceTestsManager.getTags();
      LOGGER.debug("tags: " + tags.toString());
      assertTrue(tags.size() > 0);
    } catch (Exception exception) {
      LOGGER.error(exception);
      fail();
    }
  }

  @Test
  public void getCucumberOptions() {
    List<String> tags = new ArrayList<>();
    tags.add("@MultiChannel");
    tags.add("@ChannelDesignator");
    tags.add("@Channel1");
    tags.add("@DPI");

    String options = acceptanceTestsManager.getCucumberOptions(tags, "@Ignore");
    assertEquals("cucumber.options=--tags @MultiChannel --tags @ChannelDesignator --tags @Channel1 --tags @DPI --tags ~@Ignore",
        options);
  }

  /**
   * Runs cucumber tests on the remote machine.
   */
  @Ignore
  @Test
  public void runCucumberTest() {
    List<String> tags = new ArrayList<>();
    tags.add("@MultiChannel");
    tags.add("@ChannelDesignator");
    tags.add("@Channel1");
    tags.add("@DPI");

    acceptanceTestsManager.runCucumberTest(tags, "@Ignore");

  }
}