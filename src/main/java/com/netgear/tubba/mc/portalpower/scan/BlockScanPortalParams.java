package com.netgear.tubba.mc.portalpower.scan;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockScanPortalParams implements BlockScanParams {
  private Set<Material> materialSet;
  
  public BlockScanPortalParams(Set<Material> materialSet) {
    this.materialSet = materialSet;
  }
  
  @Override
  public int getMaxArea() {
    return BlockScanIgniterParams.MAX_PORTAL_AREA;
  }
  
  @Override
  public boolean isBounds(Block block) {
    return block.isSolid() && !materialSet.contains(block.getType());
  }
  
  @Override
  public boolean isValid(Block block) {
    return materialSet.contains(block.getType());
//    return block.getType() == Material.NETHER_PORTAL || block.getType() == Material.RESPAWN_ANCHOR;
  }
}
