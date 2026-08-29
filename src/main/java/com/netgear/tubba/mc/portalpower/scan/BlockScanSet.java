package com.netgear.tubba.mc.portalpower.scan;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Axis;
import org.bukkit.block.Block;

public class BlockScanSet {
  private Axis axis;
  private Set<Block> blockSet;
  private Set<Block> boundSet;
  
  public static class Builder {
    private BlockScanSet set = new BlockScanSet();
    
    public Builder() {}
    
    public Builder addBlock(Block block) {
      set.blockSet.add(block);
      return this;
    }
    
    public Builder addBound(Block block) {
      set.boundSet.add(block);
      return this;
    }
    
    public Builder setAxis(Axis axis) {
      set.axis = axis;
      return this;
    }
    
    /**
     * Tests to see if the block is contained in the block set or boundary set
     * @param block
     * @return
     */
    public boolean contains(Block block) {
      return set.blockSet.contains(block) || set.boundSet.contains(block);
    }
    
    public int getBlockCount() {
      return set.blockSet.size();
    }
    
    public BlockScanSet build() {
      return set;
    }
  }
  
  private BlockScanSet() {
    blockSet = new HashSet<Block>();
    boundSet = new HashSet<Block>();
  }
  
  public Axis getAxis() {
    return axis;
  }
  
  public Set<Block> getBlockSet() {
    return blockSet;
  }
  
  public Set<Block> getBoundSet() {
    return boundSet;
  }
  
  public boolean isEmpty() {
    return blockSet.isEmpty();
  }
}
