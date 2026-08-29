package com.netgear.tubba.mc.portalpower.scan;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockScanIgniterParams implements BlockScanParams {
  public final static int MAX_PORTAL_LENGTH = 21;
  public final static int MAX_PORTAL_AREA = MAX_PORTAL_LENGTH * MAX_PORTAL_LENGTH;

  @Override
  public int getMaxArea() {
    return MAX_PORTAL_AREA; 
  }
  
  @Override
  public boolean isBounds(Block block) {
    return block.isSolid();
  }
  
  @Override
  public boolean isValid(Block block) {
    return block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR;
  }
}
