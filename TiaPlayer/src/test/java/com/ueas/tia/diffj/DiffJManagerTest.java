/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.diffj;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Set;

public class DiffJManagerTest {
  private static final Logger LOGGER = LogManager.getLogger(DiffJManagerTest.class);

  @Test
  public void getMethodChanges() {
    String file1 = "files/Sniffer_current.java";
    String file2 = "files/Sniffer_pre.java";

    DiffJManager diffJManager = new DiffJManager();
    Set<String> methods = diffJManager.getMethodsChanged(
        getClass().getClassLoader().getResource(file1).getPath(), getClass()
            .getClassLoader().getResource(file2).getPath());

    methods.stream().forEach(s -> LOGGER.debug(s));

    assertTrue(methods.contains("com.ueas.tai.Sniffer.main(String)"));
    assertTrue(methods.contains("com.ueas.tai.Sniffer.startSniffing(VirtualMachine)"));
  }

  @Test
  public void parseReport() {
    String report = "24,71a27: field added: DUMMY\n"
        + "30c31: code changed in startSniffing(VirtualMachine)\n"
        + "67c68: code removed in main(String[])\n"
        + "68c69: code added in main(String[])";

    DiffJManager diffJManager = new DiffJManager();
    Set<String> methods = diffJManager.parseReport(report);

    methods.stream().forEach(s -> LOGGER.debug(s));

    assertTrue(methods.contains("startSniffing(VirtualMachine)"));
    assertTrue(methods.contains("main(String[])"));
  }

  @Test
  public void parseReportNoChange() {
    String report = "24,71a27: field added: DUMMY";

    DiffJManager diffJManager = new DiffJManager();
    Set<String> methods = diffJManager.parseReport(report);

    methods.stream().forEach(s -> LOGGER.debug(s));
    assertTrue(methods.size() == 0);
  }

}