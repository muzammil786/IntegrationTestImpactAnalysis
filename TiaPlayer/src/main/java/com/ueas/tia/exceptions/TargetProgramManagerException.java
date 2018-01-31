/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.exceptions;

public class TargetProgramManagerException extends Exception {

  public TargetProgramManagerException(String message) {
    super(message);
  }

  public TargetProgramManagerException(Exception ex) {
    super(ex);
  }

  public TargetProgramManagerException(String message, Exception ex) {
    super(message, ex);
  }
}