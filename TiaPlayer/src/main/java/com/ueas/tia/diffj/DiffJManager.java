/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.diffj;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.incava.diffj.app.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiffJManager {
  private static final Logger LOGGER = LogManager.getLogger(DiffJManager.class);

  private static final Pattern METHOD_CHANGED_MARKER = Pattern.compile("code (changed|removed|added) in");

  /**
   * Gets the list of methods changed.
   */
  public Set<String> getMethodsChanged(String file1, String file2) {
    Options opts = new Options();
    List<String> names = opts.process(getArguments(file1, file2));
    DiffJWrapper diffJ = new DiffJWrapper(opts.showBriefOutput(),
        opts.showContextOutput(),
        opts.highlightOutput(),
        opts.recurse(),
        opts.getFirstFileName(), opts.getFromSource(),
        opts.getSecondFileName(), opts.getToSource());

    diffJ.processNames(names);
    LOGGER.debug("Changes reported: " + diffJ.getWriter().toString());

    return parseReport(diffJ.getWriter().toString());
  }

  private List<String> getArguments(String file1, String file2) {
    List<String> list = new ArrayList<>(3);
    list.add("--brief");
    list.add(file1);
    list.add(file2);
    return list;
  }

  /**
   * Parses report to extract the method names changed.
   */
  public Set<String> parseReport(String diffJReport) {

    return Arrays.stream(diffJReport.split("\\n"))
        .filter(s -> METHOD_CHANGED_MARKER.matcher(s).find())
        .map(s -> s.replaceFirst(".*" + METHOD_CHANGED_MARKER, "").trim())
        .collect(Collectors.toSet());
  }


}