package com.tubba.mc.paper.portals.util;

import org.bukkit.NamespacedKey;

import net.kyori.adventure.text.format.NamedTextColor;

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
    if(name.startsWith("minecraft:") && name.length() > 10) {
      name = name.substring(10);
    }
    return lookup(NamespacedKey.minecraft(name));
  }
}
