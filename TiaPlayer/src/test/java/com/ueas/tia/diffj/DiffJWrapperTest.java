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
import org.incava.diffj.app.Options;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DiffJWrapperTest {

  private static final Logger LOGGER = LogManager.getLogger(DiffJWrapperTest.class);

  @Test
  public void getDiff() {
    String file1 = "files/Sniffer_current.java";
    String file2 = "files/Sniffer_pre.java";

    Options opts = new Options();
    List<String> list = new ArrayList<String>();
    list.add("--brief");
    list.add(getClass().getClassLoader().getResource(file1).getPath());
    list.add(getClass().getClassLoader().getResource(file2).getPath());

    List<String> names = opts.process(list);
    DiffJWrapper diffJ = new DiffJWrapper(opts.showBriefOutput(),
        opts.showContextOutput(),
        opts.highlightOutput(),
        opts.recurse(),
        opts.getFirstFileName(), opts.getFromSource(),
        opts.getSecondFileName(), opts.getToSource());

    diffJ.processNames(names);
    LOGGER.debug(diffJ.getWriter().toString());
    assertTrue(diffJ.getWriter().toString().contains("code changed in com.ueas.tai.Sniffer.startSniffing(VirtualMachine)"));
  }
}