/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.git;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

@Ignore
/**
 * @TODO Fix the hardcoded git repo url.
 */
public class GitRepositoryManagerTest {
  private static final String REPO_PATH = "build/tmp/repo";

  private GitRepositoryManager repositoryManager = new GitRepositoryManager();

  /**
   * Clones the test repo.
   */
  @Before
  public void setup() {
    // download
    String url = "git@man-cisrv-1.ultra-as.net:tia/TestRepo.git";
    boolean result = repositoryManager.download(url, REPO_PATH);
    assertTrue(result);
    assertTrue(Files.exists(Paths.get("build/tmp/repo/.git")));
  }

  @Test
  public void getChangedFiles() {
    String[] list = repositoryManager.getChangedFiles(REPO_PATH);
    assertNotNull(list);
    assertTrue(list.length > 0);
  }

  @Test
  public void downloadLastVersion() {
    String file = "Module/src/main/java/com/ueas/tia/module/Module.java";
    boolean success = repositoryManager.downloadLastVersion(file, REPO_PATH, "_pre");
    assertTrue(success);
  }
}