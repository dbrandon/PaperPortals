package com.netgear.tubba.mc.portalpower;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;

import net.kyori.adventure.text.format.NamedTextColor;

public class LocationEncoder extends NumberEncoder {
  
  public enum WorldName {
    OVERWORLD(NamespacedKey.minecraft("overworld"), "Overworld", NamedTextColor.GREEN),
    NETHER(NamespacedKey.minecraft("the_nether"), "Nether", NamedTextColor.DARK_PURPLE),
    END(NamespacedKey.minecraft("the_end"), "End", NamedTextColor.GOLD);
    
    private NamespacedKey namespacedKey;
    private String friendlyName;
    private NamedTextColor defaultColor;
    private WorldName(NamespacedKey namespacedKey, String friendlyName, NamedTextColor defaultColor) {
      this.namespacedKey = namespacedKey;
      this.friendlyName = friendlyName;
      this.defaultColor = defaultColor;
    }
    
    public NamedTextColor getDefaultColor() {
      return defaultColor;
    }
    
    public String getFriendlyName() {
      return friendlyName;
    }
    
    public NamespacedKey getNamespacedKey() {
      return namespacedKey;
    }
    
    public static WorldName lookup(NamespacedKey key) {
      for(WorldName wn : values()) {
        if(wn.namespacedKey.equals(key)) {
          return wn;
        }
      }
      
      return null;
    }
    
    public static WorldName lookup(String name) {
      return lookup(NamespacedKey.minecraft(name));
    }
  }
  
  private final static Pattern LOC_PARSER = Pattern.compile("([^/]*)/([^/]*)/([^/]+)/(.+)");

  /**
   * Parse the encoded string to extract the Location value
   * @param encoded
   * @return
   */
  public static Location decode(String encoded) {
    if(encoded == null) {
      return null;
    }
    
    Matcher m = LOC_PARSER.matcher(encoded);
    
    if(!m.matches()) {
      return null;
    }
    
    World world = Bukkit.getServer().getWorld(m.group(1));
    if(world == null) {
      throw new IllegalStateException("Failed to parse world component of " + encoded);
    }
    
    return new Location(world,
        decodeInt(m.group(2), encoded),
        decodeInt(m.group(3), encoded),
        decodeInt(m.group(4), encoded));
  }
  
  /**
   * Encode the block's location to a string.  Only the location is encoded; all
   * other block specific data is excluded.
   * @param block
   * @return
   */
  public static String encode(Block block) {
    return encode(block.getLocation());
  }
  
  /**
   * Encode the location into a string that can be stored in a namespaced key
   * @param loc
   * @return
   */
  public static String encode(Location loc) {
    return loc.getWorld().getName() + SEP + loc.getBlockX() + SEP + loc.getBlockY() + SEP + loc.getBlockZ();
  }
}
