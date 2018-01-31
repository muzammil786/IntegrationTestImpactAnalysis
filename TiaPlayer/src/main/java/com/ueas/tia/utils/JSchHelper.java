/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Helper class for JSch utility.
 */
public class JSchHelper {
  private static final Logger LOGGER = LogManager.getLogger(JSchHelper.class);

  private String hostname;
  private String username;
  private String password;

  private Session jschSession = null;

  /**
   * Constructor for setting the parameters.
   *
   * @param hostname - server host
   * @param username - username to login to the host
   * @param password - password for the username
   */
  public JSchHelper(String hostname, String username, String password) {
    this.hostname = hostname;
    this.username = username;
    this.password = password;
  }

  /**
   * Connects the JSch session.
   *
   * @throws JSchException Throws exception if error at connection.
   */
  public void connect() throws JSchException {
    disconnect();
    JSch jsch = new JSch();
    jschSession = jsch.getSession(username, hostname, 22);
    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    jschSession.setConfig(config);
    jschSession.setPassword(password);
    jschSession.connect();
  }

  /**
   * Reads the file contents into a string hosted on the remote machine.
   *
   * @param filePath absolute file path.
   * @return String contents of the file.
   */
  public String getFileAsStringFromRemoteServer(String filePath)
      throws JSchException, SftpException, IOException {
    connect();
    Channel channel = getJschSession().openChannel("sftp");
    channel.connect();
    ChannelSftp sftpChannel = (ChannelSftp) channel;
    String contents;
    try (InputStream stream = sftpChannel.get(filePath)) {
      contents = IOUtils.toString(stream, Charset.defaultCharset());
    }

    sftpChannel.exit();
    sftpChannel.disconnect();
    disconnect();
    return contents;
  }

  /**
   * Copy resources to the remote host.
   *
   * @param sourcePath - The source path which is to be copied
   * @param destPath   - The destination path where the files will be copied. The path must exist on the host server.
   * @throws JSchException         - Throws exception
   * @throws SftpException         - Throws exception
   * @throws FileNotFoundException - Throws exception if the source file/folder is not found.
   */
  public void copyResourceToRemoteServer(String sourcePath, String destPath)
      throws JSchException, SftpException, FileNotFoundException {
    connect();
    Channel channel = getJschSession().openChannel("sftp");
    channel.connect();
    ChannelSftp sftpChannel = (ChannelSftp) channel;
    // copy the src path to destination path on the remote host
    copyResourceToRemoteServer(sourcePath, destPath, sftpChannel);

    sftpChannel.exit();
    sftpChannel.disconnect();
    disconnect();
  }

  /**
   * Copy resources to the remote host.
   *
   * @param sourcePath  - The source path which is to be copied
   * @param destPath    - The destination path where the files will be copied. The path must exist on the host server.
   * @param sftpChannel - The channel
   * @throws JSchException         - Throws exception
   * @throws SftpException         - Throws exception
   * @throws FileNotFoundException - Throws exception if the source file/folder is not found.
   */
  public void copyResourceToRemoteServer(String sourcePath, String destPath, ChannelSftp sftpChannel)
      throws JSchException, SftpException, FileNotFoundException {

    File localFile = new File(sourcePath);
    // copy if it is a file
    if (localFile.isFile()) {
      sftpChannel.cd(destPath);
      sftpChannel.put(new FileInputStream(localFile), localFile.getName(), ChannelSftp.OVERWRITE);
    } else { // copy directory
      LOGGER.debug("Copying dir: {}", localFile.getName());
      File[] files = localFile.listFiles();
      LOGGER.debug("Copying {} files/folders: ", files.length);
      if (files.length > 0) {
        try {
          sftpChannel.cd(destPath);
        } catch (SftpException e) {
          throw new SftpException(e.id, "The destination path does not exit.");
        }
        createDirectory(destPath, localFile.getName(), sftpChannel);
        // recursive call to copying files
        for (int i = 0; i < files.length; i++) {
          copyResourceToRemoteServer(files[i].getAbsolutePath(), destPath + "/" + localFile.getName(), sftpChannel);
        }
      }
    }
  }

  private void createDirectory(String destPath, String name, ChannelSftp sftpChannel) throws SftpException {
    SftpATTRS attrs = null;
    // check if the directory is already existing
    try {
      attrs = sftpChannel.stat(destPath + "/" + name);
    } catch (Exception e) {
      // ignoring
    }
    // else create a directory
    if (attrs != null) {
      LOGGER.debug("Directory exists IsDir=" + attrs.isDir());
    } else {
      LOGGER.debug("Creating dir " + name);
      sftpChannel.mkdir(name);
    }
  }

  /**
   * Excecutes the commands on the remote server.
   *
   * @param script The command to execute. Use semi-colon for multiple commands.
   * @return The result of the execution.
   * @throws JSchException Throws {@link JSchException} if any problem with opening the exec channel
   * @throws IOException   Throws {@link IOException} for any problem with the channel I/O stream.
   */
  public String executeCommand(String script) throws JSchException, IOException {
    connect();
    ChannelExec channel = (ChannelExec) getJschSession().openChannel("exec");
    channel.setCommand(script);

    InputStream in = channel.getInputStream();
    channel.setErrStream(IoBuilder.forLogger(JSchHelper.class).setLevel(Level.ERROR).buildOutputStream());
    channel.connect();

    StringBuilder builder = new StringBuilder();
    byte[] tmp = new byte[1024];
    while (true) {
      while (in.available() > 0) {
        int read = in.read(tmp, 0, 1024);
        if (read < 0) {
          break;
        }
        builder.append(new String(tmp, 0, read));
      }
      if (channel.isClosed()) {
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
        LOGGER.error(ee);
      }
    }
    channel.disconnect();
    disconnect();
    return builder.toString();
  }

  /**
   * Disconnects the JSch session.
   *
   * @throws JSchException - Throws exception if problem with disconnection
   */
  public void disconnect() {
    if (getJschSession() != null && getJschSession().isConnected()) {
      getJschSession().disconnect();
    }
  }

  /**
   * Getter for Jsch session.
   */
  public Session getJschSession() {
    return this.jschSession;
  }

  /**
   * Renames oldFilePath to newPath.
   */
  public void renameFile(String oldFilePath, String newPath) throws JSchException, SftpException {
    connect();
    Channel channel = getJschSession().openChannel("sftp");
    channel.connect();
    ChannelSftp sftpChannel = (ChannelSftp) channel;
    sftpChannel.rename(oldFilePath, newPath);
    sftpChannel.exit();
    sftpChannel.disconnect();
    disconnect();
  }
}
