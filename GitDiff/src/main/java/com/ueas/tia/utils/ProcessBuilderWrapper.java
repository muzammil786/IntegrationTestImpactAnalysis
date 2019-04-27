/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Wrapper for ProcessBuilder.
 */
public class ProcessBuilderWrapper {
  private static final Logger LOGGER = LogManager.getLogger(ProcessBuilderWrapper.class);

  private StringWriter infos;
  private StringWriter errors;
  private int status;
  private boolean showOutputOnConsole = false;

  /**
   * Constructor.
   */
  public ProcessBuilderWrapper() {
  }

  /**
   * Creates ProcessBuilder and changes directory before executing the given commands.
   */
  public void execute(File directory, String[] command) throws IOException {

    infos = new StringWriter();
    errors = new StringWriter();
    ProcessBuilder pb = new ProcessBuilder(command);
    if (directory != null) {
      pb.directory(directory);
    }
    LOGGER.info("Running command: " + pb.command());
    Process process = pb.start();
    StreamRedirectThread seInfo = new StreamRedirectThread(process.getInputStream(), new PrintWriter(infos, true));
    StreamRedirectThread seError = new StreamRedirectThread(process.getErrorStream(), new PrintWriter(errors, true));

    seInfo.setShowOnConsole(showOutputOnConsole);
    seInfo.start();
    seError.start();

    try {
      status = process.waitFor();
      seInfo.join();
      seError.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error(e);
    }
  }

  /**
   * Creates ProcessBuilder to execute the given commands.
   */
  public void execute(String[] command) throws IOException {
    execute(null, command);
  }

  /**
   * Returns true if status is zero. False otherwise.
   */
  public boolean isSuccessful() {
    if (status != 0) {
      LOGGER.error("Status: " + status);
      LOGGER.error("Error(s): " + errors);
      return false;
    }
    return true;
  }

  public String getErrors() {
    return errors.toString();
  }

  public String getInfos() {
    return infos.toString();
  }

  public int getStatus() {
    return status;
  }

  public ProcessBuilderWrapper showOutputOnConsole(boolean showOutputOnConsole) {
    this.showOutputOnConsole = showOutputOnConsole;
    return this;
  }
}