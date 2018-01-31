/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2018        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.exceptions;

public class AcceptanceTestsManagerException extends Exception {

  public AcceptanceTestsManagerException(String message) {
    super(message);
  }

  public AcceptanceTestsManagerException(Exception ex) {
    super(ex);
  }

  public AcceptanceTestsManagerException(String message, Exception ex) {
    super(message, ex);
  }
}