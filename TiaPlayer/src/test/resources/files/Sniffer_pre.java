/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2017        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tai;

import static org.jdiscript.util.Utils.println;

import com.sun.jdi.VirtualMachine;
import com.ueas.tai.events.MethodTracer;
import com.ueas.tai.printer.FilePrinter;
import com.ueas.tai.vm.VirtualMachineFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiscript.JDIScript;

import java.io.File;
import java.io.IOException;

public class Sniffer {

  private static final Logger LOGGER = LogManager.getLogger(Sniffer.class);
  private static final int DUMMY = 0;

  public void startSniffing(VirtualMachine vm) {
    try {
      LOGGER.info("Starting sniffing ." + vm.name());
      JDIScript j = new JDIScript(vm);
      MethodTracer methodTracer = new MethodTracer();

      j.methodEntryRequest()
          .addClassFilter(Configuration.getConfiguration().getProperty("class.filter"))
          .addHandler(methodTracer).enable();

      // to print the info out before termination
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        LOGGER.info("Printing method tracking info");
        try {
          methodTracer.print(new FilePrinter(new File(Configuration.getConfiguration().getProperty("output.path") +
              "method-trace.log")));
        } catch (IOException e) {
          LOGGER.error("Exception occurred during trace printout.", e);
        }
      }));

      j.run();
      println("Shutting down");
    } catch (Exception exception) {
      LOGGER.error("Error occurred during JVM sniffing ", exception);
    }
  }

  public static void main(String[] args) {
    // pid
    //      VirtualMachine vm = VirtualMachineFactory.getVirtualMachine("8524");
    // socket
    try {
      String host = Configuration.getConfiguration().getProperty("host");
      String port = Configuration.getConfiguration().getProperty("port");

      VirtualMachine vm = VirtualMachineFactory.getVirtualMachine(host, port);
      Sniffer sniffer = new Sniffer();
      sniffer.startSniffing(vm);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}