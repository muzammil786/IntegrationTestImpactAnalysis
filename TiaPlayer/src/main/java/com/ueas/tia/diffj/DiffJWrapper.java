/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.diffj;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.incava.analysis.BriefReport;
import org.incava.analysis.DetailedReport;
import org.incava.analysis.Report;
import org.incava.diffj.app.DiffJ;
import org.incava.diffj.io.JavaFSElement;
import org.incava.diffj.lang.DiffJException;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Wrapper for DiffJ that redefines Report.
 */
public class DiffJWrapper extends DiffJ {

  private static final Logger LOGGER = LogManager.getLogger(DiffJWrapper.class);

  private Report report;
  private Writer writer = new StringWriter();

  /**
   * Constructor to override report.
   */
  public DiffJWrapper(boolean briefOutput,
                      boolean contextOutput,
                      boolean highlightOutput,
                      boolean recurseDirectories,
                      String fromLabel, String fromSource,
                      String toLabel, String toSource) {
    super(briefOutput, contextOutput, highlightOutput, recurseDirectories, fromLabel, fromSource, toLabel, toSource);
    // redefining report
    this.report = briefOutput ? new BriefReport(writer) : new DetailedReport(writer, contextOutput, highlightOutput);
  }

  public Writer getWriter() {
    return writer;
  }

  @Override
  public boolean compareElements(String fromName, JavaFSElement toElmt) {
    try {
      JavaFSElement fromElmt = getFromElement(fromName);
      if (fromElmt == null) {
        return false;
      }
      fromElmt.compareTo(report, toElmt);
      if (getFileDiffs().wasAdded()) {
        setExitValue(1);
      }
      return true;
    } catch (DiffJException de) {
      LOGGER.error(de);
      setExitValue(1);
      return false;
    }
  }
}