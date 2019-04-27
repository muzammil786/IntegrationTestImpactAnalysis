/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.player;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.ueas.tia.config.Configuration;
import com.ueas.tia.exceptions.JvmSnifferException;
import com.ueas.tia.utils.JSchHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class JvmSnifferManager {
  private static final Logger LOGGER = LogManager.getLogger(JvmSnifferManager.class);
  private String jvmSnifferDir;
  private JSchHelper jschHelper;
  private String methodTraceLogFile;

  /**
   * Constructor.
   */
  public JvmSnifferManager() {
    jschHelper = new JSchHelper(
        Configuration.getConfiguration().getProperty("target.host"),
        Configuration.getConfiguration().getProperty("target.host.user"),
        Configuration.getConfiguration().getProperty("target.host.password"));
    jvmSnifferDir = Configuration.getConfiguration().getProperty("jvmsniffer.home") + "/bin";
    methodTraceLogFile = Configuration.getConfiguration().getProperty("jvmsniffer.output") + "/method-trace.log";
  }

  /**
   * Reads the method trace file outputted by JvmSniffer for specific tags.
   */
  public String getMethodTraceForTag(String tags) throws JSchException, SftpException, IOException {
    String filePath = methodTraceLogFile + tags;
    LOGGER.info("Getting traces from file " + filePath);
    return jschHelper.getFileAsStringFromRemoteServer(filePath);
  }

  /**
   * Starts the JvmSniffer on the target machine from path jvmsniffer.home/bin.
   * This is started via nohup. Make sure the target program is running with debug options before starting the sniffer.
   * Otherwise the sniffer will not start and errors will be
   * logged in nohup and not captured by Jsch.
   */
  public void startSniffer() throws JvmSnifferException {
    // stop if Sniffer is already running
    try {
      if (confirmJvmSnifferRunning()) {
        stopSniffer();
      }
    } catch (Exception ex) {
      LOGGER.debug("Could not confirm if the JVM Sniffer is already running.");
    }

    LOGGER.info("Starting JVM Sniffer");
    try {
      // execute nohup process and output the pid in a file
      jschHelper.executeCommand(
          ". ~/.bash_profile;\n"
              + "cd " + jvmSnifferDir + ";\n"
              + "nohup bash -c \"exec -a sniffer ./sniffer\" > nohup.log 2>&1 & echo $! > pid\n"
      );
      // confirm JvmSniffer is running successfully
      if (!confirmJvmSnifferRunning()) {
        throw new JvmSnifferException("Could not start JVM sniffer successfully. Please check nohup.log");
      }
    } catch (JvmSnifferException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new JvmSnifferException(ex);
    }
  }

  /**
   * Returns true if pid is found for the JvmSniffer.
   */
  public boolean confirmJvmSnifferRunning() throws IOException, JSchException, SftpException {
    LOGGER.info("Checking if the JVM Sniffer process running");
    // read pid
    String pid = jschHelper.getFileAsStringFromRemoteServer(jvmSnifferDir + "/pid");
    LOGGER.debug("Expected JvmSniffer PID " + pid);
    String result = jschHelper.executeCommand(
        "ps -o pid= -p " + pid
    );
    LOGGER.debug("PS command output " + result);
    return StringUtils.equals(StringUtils.trimToEmpty(result), StringUtils.trimToEmpty(pid));
  }

  /**
   * Soft kills the JvmSniffer process on the target machine. This will invoke the shutdown hooks in JvmSniffer before
   * termination.
   */
  public void stopSniffer() throws IOException, JSchException {
    LOGGER.info("Stopping JVM Sniffer");
    String result = jschHelper.executeCommand("pkill -f sniffer");
    LOGGER.debug(result);
  }

  /**
   * Renames method-trace.log to method-trace.log@tags.
   */
  public void renameTraceFile(String tags) throws JvmSnifferException {
    String toFile = methodTraceLogFile + tags;
    try {
      jschHelper.renameFile(methodTraceLogFile, toFile);
    } catch (Exception ex) {
      throw new JvmSnifferException("Exception occurred while renaming " + methodTraceLogFile, ex);
    }
  }
}