/**********************************************************************.
 *                                                                     *
 *         Copyright (c) Ultra Electronics Airport Systems 2017        *
 *                         All rights reserved                         *
 *                                                                     *
 ***********************************************************************/

package com.ueas.tia.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration from config.properties in the classpath.
 */
public class Configuration {
  private static Properties properties;

  private Configuration() {
  }

  /**
   * Gets the Properties object with loaded properties.
   */
  public static Properties getConfiguration() {
    if (properties == null) {
      properties = new Properties();
      try (final InputStream stream = Configuration.class.getClassLoader().getResourceAsStream("config.properties")) {
        properties.load(stream);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return properties;
  }

}