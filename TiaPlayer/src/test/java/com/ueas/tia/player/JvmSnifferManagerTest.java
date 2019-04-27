/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import com.ueas.tia.config.Configuration;
import com.ueas.tia.utils.JSchHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore
public class JvmSnifferManagerTest {
  private static final Logger LOGGER = LogManager.getLogger(JvmSnifferManagerTest.class);

  /**
   * Reads method trace file from the remote machine.
   */
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
      JvmSnifferManager manager = new JvmSnifferManager();
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
  @Test
  public void renameFile() {
    try {
      JvmSnifferManager manager = new JvmSnifferManager();
      manager.renameTraceFile("@DUMMY1 @DUMMY2");
    } catch (Exception e) {
      LOGGER.error(e);
      fail();
    }

  }

  /**
   * Starts sniffer on the remote machine.
   */
  @Test
  public void startSniffer() {
    try {
      JvmSnifferManager manager = new JvmSnifferManager();
      manager.startSniffer();

    } catch (Exception exception) {
      LOGGER.error(exception);
      fail();
    }
  }

  /**
   * Stops sniffer on the remote machine.
   */
  @Test
  public void stopSniffer() {
    try {
      JvmSnifferManager manager = new JvmSnifferManager();
      manager.stopSniffer();

    } catch (Exception exception) {
      LOGGER.error(exception);
      fail();
    }
  }
}