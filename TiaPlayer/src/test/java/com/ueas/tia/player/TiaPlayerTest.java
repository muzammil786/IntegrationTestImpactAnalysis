/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ueas.tia.config.Configuration;
import com.ueas.tia.config.Constants;
import com.ueas.tia.git.GitRepositoryManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class TiaPlayerTest {

  private static final Logger LOGGER = LogManager.getLogger(TiaPlayerTest.class);

  private GitRepositoryManager repositoryManager = new GitRepositoryManager();

  @Test
  public void isAnyMethodChangedInTrace() {
    TiaPlayer player = new TiaPlayer();
    Set<String> methodChangeSet = new HashSet<>();
    methodChangeSet.add("com.ueas.ib.aftn.tcp.client.channel.ChannelDetailsVo.getHost()");

    String methodTrace = "com.ueas.ib.aftn.tcp.client.TcpClient.getTrackingUtils()\n"
        + "com.ueas.ib.aftn.tcp.common.HeaderMaker.setRemote(String)\n"
        + "com.ueas.ib.aftn.tcp.client.channel.ChannelDetailsVo.getHost()\n"
        + "com.ueas.ib.aftn.tcp.client.TcpClient.handleConnectionAttempt()";

    assertTrue(player.isAnyMethodChangedInTrace(methodTrace, methodChangeSet));
  }

  @Test
  public void isAnyMethodChangedInTraceNegative() {
    TiaPlayer player = new TiaPlayer();
    Set<String> methodChangeSet = new HashSet<>();
    methodChangeSet.add("com.ueas.ib.aftn.tcp.client.channel.ChannelDetailsV1.getHost()");

    String methodTrace = "com.ueas.ib.aftn.tcp.client.TcpClient.getTrackingUtils()\n"
        + "com.ueas.ib.aftn.tcp.common.HeaderMaker.setRemote(String)\n"
        + "com.ueas.ib.aftn.tcp.client.channel.ChannelDetailsVo.getHost()\n"
        + "com.ueas.ib.aftn.tcp.client.TcpClient.handleConnectionAttempt()";

    assertFalse(player.isAnyMethodChangedInTrace(methodTrace, methodChangeSet));
  }

  /**
   * Runs tests on remote host with JVM sniffer.
   */
  @Ignore
  @Test
  public void executeTestsWithSniffer() {
    // download
    String url = Configuration.getConfiguration().getProperty("test.repo");
    repositoryManager.download(url, Constants.TEST_REPO_PATH);
    assertTrue(Files.exists(Paths.get(Constants.TEST_REPO_PATH + "/.git")));

    Set<String> tags = new HashSet<>();
    // execute these tags with sniffer
    // tags.add("@Connection @KeepAlive");
    // tags.add("@SequenceNumber @Inbound @OutSequenceFor100");
    // tags.add("@MultiChannel @ChannelDesignator @Channel1 @DPI");
    // tags.add("@DPI @Resubscribe");

    TiaPlayer player = new TiaPlayer();
    player.executeTestsWithSniffer(tags);

  }
}