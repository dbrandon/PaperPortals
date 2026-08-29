package com.netgear.tubba.mc.portalpower;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorEncoder extends NumberEncoder {
  private final static String PREFIX = "clr" + SEP;
  
  private final static Pattern COLOR_PARSER = Pattern.compile(PREFIX + "([^/]*)/([^/]*)/([^/]*)");

  public Color decode(String encoded) {
    if(encoded == null) {
      return null;
    }
    
    Matcher m = COLOR_PARSER.matcher(encoded);
    if(!m.matches()) {
      return null;
    }
    
    return new Color(
        decodeInt(m.group(1), encoded),
        decodeInt(m.group(2), encoded),
        decodeInt(m.group(3), encoded));
  }
  
  public String encode(Color color) {
    return PREFIX + color.getRed() + SEP + color.getGreen() + SEP + color.getBlue();
  }
}
