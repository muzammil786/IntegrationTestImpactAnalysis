/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.ueas.tia.config.Configuration;
import com.ueas.tia.utils.JSchHelper;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

public class JvnSnifferManagerTest {
  private static final Logger LOGGER = LogManager.getLogger(JvnSnifferManagerTest.class);

  /**
   * Reads method trace file from the remote machine.
   */
  @Ignore
  @Test
  public void getMethodTraceForTag() {
    try {
      // copy the file to remote server
      JSchHelper jschHelper = new JSchHelper(
          Configuration.getConfiguration().getProperty("target.host"),
          Configuration.getConfiguration().getProperty("target.host.user"),
          Configuration.getConfiguration().getProperty("target.host.password"));
      jschHelper.copyResourceToRemoteServer(
          getClass().getClassLoader().getResource("files/method-trace.log@FOO").getPath(),
          Configuration.getConfiguration().getProperty("jvmsniffer.output"));

      // read from remote server
      JvnSnifferManager manager = new JvnSnifferManager();
      String file = manager.getMethodTraceForTag("@FOO");
      assertTrue(file.contains("com.ueas.ib.aftn.tcp.client.TcpClient.getTrackingUtils()"));
    } catch (Exception exception) {
      LOGGER.error(exception);
      fail();
    }
  }

  /**
   * Renames file on the remote machine.
   */
  @Ignore
  @Test
  public void renameFile() {
    try {
      JvnSnifferManager manager = new JvnSnifferManager();
      manager.renameTraceFile("@DUMMY1 @DUMMY2");
    } catch (Exception e) {
      LOGGER.error(e);
      fail();
    }

  }

  /**
   * Starts sniffer on the remote machine.
   */
  @Ignore
  @Test
  public void startSniffer() {
    try {
      JvnSnifferManager manager = new JvnSnifferManager();
      manager.startSniffer();

    } catch (Exception exception) {
      LOGGER.error(exception);
      fail();
    }
  }

  /**
   * Stops sniffer on the remote machine.
   */
  @Ignore
  @Test
  public void stopSniffer() {
    try {
      JvnSnifferManager manager = new JvnSnifferManager();
      manager.stopSniffer();

    } catch (Exception exception) {
      LOGGER.error(exception);
      fail();
    }
  }
}