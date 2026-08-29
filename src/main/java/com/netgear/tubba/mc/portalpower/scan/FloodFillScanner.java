package com.netgear.tubba.mc.portalpower.scan;

import java.util.Set;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class FloodFillScanner {
  
  /**
   * Finds the set of blocks that can be converted to a nether portal.  The start block needs to
   * be air surrounded by solid blocks.
   * @param start
   * @param ref
   * @return
   */
  public static BlockScanSet findIgniterSet(Block start, Block ref) {
    return floodFill(start, ref, new BlockScanIgniterParams());
  }
  
  /**
   * Finds the set of blocks making up the current portal
   * @param start
   * @param ref
   * @return
   */
  public static BlockScanSet findPortalSet(Block start, Block ref, Set<Material> set) {
    return floodFill(start, ref, new BlockScanPortalParams(set));
  }
  
  public static BlockScanSet floodFill(Block start, Block ref, BlockScanParams params) {
    if(ref.getLocation().getBlockX() != start.getLocation().getBlockX()) {
      BlockScanSet.Builder builder = new BlockScanSet.Builder();
      if(fillX(start, builder, params)) {
        builder.setAxis(Axis.X);
        return builder.build();
      }
      
      builder = new BlockScanSet.Builder();
      if(fillY(start, builder, params)) {
        builder.setAxis(Axis.Y);
        return builder.build();
      }
    }
    
    if(ref.getLocation().getBlockY() != start.getLocation().getBlockY()) {
      BlockScanSet.Builder builder = new BlockScanSet.Builder();
      if(fillX(start, builder, params)) {
        builder.setAxis(Axis.X);
        return builder.build();
      }
      
      builder = new BlockScanSet.Builder();
      if(fillZ(start, builder, params)) {
        builder.setAxis(Axis.Z);
        return builder.build();
      }
    }
    
    if(ref.getLocation().getBlockZ() != start.getLocation().getBlockZ()) {
      BlockScanSet.Builder builder = new BlockScanSet.Builder();
      if(fillZ(start, builder, params)) {
        builder.setAxis(Axis.Z);
        return builder.build();
      }
      
      builder = new BlockScanSet.Builder();
      if(fillY(start, builder, params)) {
        builder.setAxis(Axis.Y);
        return builder.build();
      }
    }
    
    return null;
  }
  
  private static boolean fillX(Block current, BlockScanSet.Builder builder, BlockScanParams params) {
    if(builder.getBlockCount() > params.getMaxArea()) {
      return false;
    }
    if(builder.contains(current)) {
      return true;
    }
    
    if(params.isBounds(current)) {
      builder.addBound(current);
      return true;
    }
    if(!params.isValid(current)) {
      return false;
    }
    
    builder.addBlock(current);
    return fillX(current.getRelative(BlockFace.UP), builder, params) &&
        fillX(current.getRelative(BlockFace.DOWN), builder, params) &&
        fillX(current.getRelative(BlockFace.EAST), builder, params) &&
        fillX(current.getRelative(BlockFace.WEST), builder, params);
  }
  
  private static boolean fillY(Block current, BlockScanSet.Builder builder, BlockScanParams params) {
    if(builder.getBlockCount() > params.getMaxArea()) {
      return false;
    }
    if(builder.contains(current)) {
      return true;
    }
    
    if(params.isBounds(current)) {
      builder.addBound(current);
      return true;
    }
    if(!params.isValid(current)) {
      return false;
    }
    
    builder.addBlock(current);
    return fillY(current.getRelative(BlockFace.EAST), builder, params) &&
        fillY(current.getRelative(BlockFace.WEST), builder, params) &&
        fillY(current.getRelative(BlockFace.NORTH), builder, params) &&
        fillY(current.getRelative(BlockFace.SOUTH), builder, params);
  }
  
  private static boolean fillZ(Block current, BlockScanSet.Builder builder, BlockScanParams params) {
    if(builder.getBlockCount() > params.getMaxArea()) {
      return false;
    }
    if(builder.contains(current)) {
      return true;
    }
    
    if(params.isBounds(current)) {
      builder.addBound(current);
      return true;
    }
    if(!params.isValid(current)) {
      return false;
    }
    
    builder.addBlock(current);
    
    return fillZ(current.getRelative(BlockFace.UP), builder, params) &&
        fillZ(current.getRelative(BlockFace.DOWN), builder, params) &&
        fillZ(current.getRelative(BlockFace.NORTH), builder, params) &&
        fillZ(current.getRelative(BlockFace.SOUTH), builder, params);
  }
}
