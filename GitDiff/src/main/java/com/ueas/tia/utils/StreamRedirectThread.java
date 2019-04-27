/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * StreamRedirectThread is a thread which copies its input to its output and
 * terminates when it completes.
 */
public class StreamRedirectThread extends Thread {
  private static final Logger LOGGER = LogManager.getLogger(StreamRedirectThread.class);

  private InputStream in;
  private PrintWriter pw;
  private boolean showOnConsole = false;

  /**
   * Constructor.
   */
  StreamRedirectThread(InputStream in, PrintWriter pw) {
    this.in = in;
    this.pw = pw;
  }

  @Override
  @SuppressWarnings("squid:S106")
  public void run() {
    try {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
        String line;
        while ((line = br.readLine()) != null) {
          // prints on console as the output is streamed
          if (showOnConsole) {
            System.out.println(line);
          }
          pw.println(line);
        }
      }
    } catch (IOException e) {
      LOGGER.error(e);
    }
  }

  /**
   * Setter.
   */
  public void setShowOnConsole(boolean showOnConsole) {
    this.showOnConsole = showOnConsole;
  }
}
