package com.netgear.tubba.mc.portalpower;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class FloodFillBounds {
  private final static int MAX_PORTAL_LENGTH = 21;
  private final static int MAX_PORTAL_AREA = MAX_PORTAL_LENGTH * MAX_PORTAL_LENGTH;

  private int minX;
  private int maxX;
  private int minY;
  private int maxY;
  private int minZ;
  private int maxZ;
  
  private Set<Block> set;
  
  public static FloodFillBounds fillBounds(Block start, Set<Material> boundSet) {
    FloodFillBounds bounds = new FloodFillBounds(start);
    
    if(bounds.fillZ(start, getMatSet(start), boundSet) && bounds.isRect()) {
      return bounds;
    }
    
    bounds = new FloodFillBounds(start);
    if(bounds.fillX(start, getMatSet(start), boundSet) && bounds.isRect()) {
      return bounds;
    }

    return null;
  }
  
  private FloodFillBounds(Block start) {
    initBounds(start.getLocation());
  }
  
  public Axis getAxis() {
    if(minX == maxX) {
      return Axis.Z;
    }
    if(minZ == maxZ) {
      return Axis.X;
    }
    if(minY == maxY) {
      return Axis.Y;
    }
    throw new IllegalStateException("Unknown axis for region");
  }
  
  public Set<Block> getSet() {
    return set;
  }
  
  private void initBounds(Location location) {
    minX = maxX = location.getBlockX();
    minY = maxY = location.getBlockY();
    minZ = maxZ = location.getBlockZ();
    set = new HashSet<Block>();
  }
  
  private void adjustBounds(Location location) {
    int x = location.getBlockX();
    if(minX > x) minX = x;
    if(maxX < x) maxX = x;
    int y = location.getBlockY();
    if(minY > y) minY = y;
    if(maxY < y) maxY = y;
    int z = location.getBlockZ();
    if(minZ > z) minZ = z;
    if(maxZ < z) maxZ = z;
  }
  
  private boolean isRect() {
    if(minX == maxX) {
      int min = 0;
      int max = 0;
      
      for(Block b : set) {
        int z = b.getLocation().getBlockZ();
        if(z == minZ) {
          min++;
        }
        else if(z == maxZ) {
          max++;
        }
      }
      
      return min == max && min == (maxY - minY + 1);
    }
    
    if(minZ == maxZ) {
      int min = 0;
      int max = 0;
      
      for(Block b : set) {
        int x = b.getLocation().getBlockX();
        
        if(x == minX) {
          min++;
        }
        else if(x == maxX) {
          max++;
        }
      }
      
      return min == max && min == (maxY - minY + 1);
    }
    
    return false;
  }
  
  private boolean fillX(Block current, Set<Material> matSet, Set<Material> boundSet) {
    if(set.size() > MAX_PORTAL_AREA) {
      return false;
    }
    if(set.contains(current)) {
      return true;
    }
    
    if(boundSet.contains(current.getType())) {
      return true;
    }
    if(!matSet.contains(current.getType())) {
      return false;
    }
    
    set.add(current);
    adjustBounds(current.getLocation());
    
    return fillX(current.getRelative(BlockFace.UP), matSet, boundSet) &&
        fillX(current.getRelative(BlockFace.DOWN), matSet, boundSet) &&
        fillX(current.getRelative(BlockFace.EAST), matSet, boundSet) &&
        fillX(current.getRelative(BlockFace.WEST), matSet, boundSet);
  }
  
  private boolean fillZ(Block current, Set<Material> matSet, Set<Material> boundSet) {
    if(set.size() > MAX_PORTAL_AREA) {
      return false;
    }
    if(set.contains(current)) {
      return true;
    }
    
    if(boundSet.contains(current.getType())) {
      return true;
    }
    
    if(!matSet.contains(current.getType()) ) {
      return false;
    }

    set.add(current);
    adjustBounds(current.getLocation());
    
    return fillZ(current.getRelative(BlockFace.UP), matSet, boundSet) &&
        fillZ(current.getRelative(BlockFace.DOWN), matSet, boundSet) &&
        fillZ(current.getRelative(BlockFace.NORTH), matSet, boundSet) &&
        fillZ(current.getRelative(BlockFace.SOUTH), matSet, boundSet);
  }
  
  private static Set<Material> getMatSet(Block block) {
    Set<Material> matSet = new HashSet<Material>();
    
    if(block.getType() == Material.NETHER_PORTAL) {
      matSet.add(block.getType());
    }
    else {
      matSet.add(Material.AIR);
      matSet.add(Material.CAVE_AIR);
    }

    return matSet;
  }
}
