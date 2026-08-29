package com.netgear.tubba.mc.portalpower;

public class NumberEncoder {
  public final static String SEP = "/";

  /**
   * Decode the integer value from the text field, part of the full encoded string
   * @param text  field containing the text to parse to an integer
   * @param full  full encoded location string
   * @return
   */
  public static int decodeInt(String text, String full) {
    try {
      return Integer.parseInt(text);
    }
    catch(Exception ex) {
      throw new IllegalStateException("Failed to parse component '" + text + "' of '" + full + "'", ex);
    }
  }
}
