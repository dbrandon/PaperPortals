package com.netgear.tubba.mc.portalpower.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.block.data.type.Switch;
import org.bukkit.inventory.InventoryHolder;

public class BlockInteractionTest {
  public static boolean opensMenu(Block block) {
    if(block == null) {
      return false;
    }
    
    BlockState state = block.getState();
    if(state instanceof InventoryHolder) {
      return true;
    }
    
    Material type = block.getType();
    return switch(type) {
    case ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL, CRAFTING_TABLE,
        ENCHANTING_TABLE, BEACON, LOOM, CARTOGRAPHY_TABLE,
        GRINDSTONE, SMITHING_TABLE -> true;
    default -> false;
    };
  }
  
  public static boolean isSwitchOrToggle(Block block) {
    if(block == null) {
      return false;
    }
    
    BlockData data = block.getBlockData();
    
    return data instanceof Powerable
        || data instanceof Openable
        || data instanceof Switch
        || data instanceof Repeater
        || data instanceof Comparator;
  }
}
