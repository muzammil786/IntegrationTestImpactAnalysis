/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.exceptions;

public class JvmSnifferException extends Exception {

  public JvmSnifferException(String message) {
    super(message);
  }

  public JvmSnifferException(Exception ex) {
    super(ex);
  }

  public JvmSnifferException(String message, Exception ex) {
    super(message, ex);
  }
}