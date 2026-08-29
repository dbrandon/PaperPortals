package com.netgear.tubba.mc.portalpower.scan;

import org.bukkit.block.Block;

public interface BlockScanParams {
  /**
   * Returns true if the block represents a boundary edge for the scan
   * @param block
   * @return
   */
  public boolean isBounds(Block block);
  
  /**
   * Returns true if the block represents a valid block to include in the scan
   * @param block
   * @return
   */
  public boolean isValid(Block block);
  
  public int getMaxArea();
}
