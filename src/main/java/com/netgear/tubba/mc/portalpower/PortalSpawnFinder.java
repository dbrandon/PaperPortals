package com.netgear.tubba.mc.portalpower;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class PortalSpawnFinder {
  private List<Location> avoidList;
  
  public PortalSpawnFinder(List<Location> avoidList) {
    this.avoidList = avoidList;
  }
  
  public Location findSafeSpawnLocation(Location origin, int radius, int maxSearchRange) {
    World world = origin.getWorld();
    if(world == null) return null;
    
    int startX = origin.getBlockX();
    int startZ = origin.getBlockZ();
    
    // spiral steps
    int x = 0, z = 0;
    int dx = 0, dz = -1;
    int maxSteps = maxSearchRange * maxSearchRange;
    
    for(int i = 0; i < maxSteps; i++) {
      if((-maxSearchRange <= x && x <= maxSearchRange) && (-maxSearchRange <= z && z <= maxSearchRange)) {
        int currentX = startX + x;
        int currentZ = startZ + z;
        
        int highestY = world.getHighestBlockYAt(currentX, currentZ);
        Location candidate = new Location(world, currentX, highestY + 1, currentZ);
        
        
        if(isEnvironmentSafe(candidate) && isRadiusClear(candidate, radius)) {
          return candidate;
        }
      }
      
      if(x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
        int temp = dx;
        dx = -dz;
        dz = temp;
      }
      x += dx;
      z += dz;
    }
    
    return null;
  }

  /**
   * Confirm the location is a safe spot for a player to spawn
   * @param loc
   * @return
   */
  private boolean isEnvironmentSafe(Location loc) {
    Block feet = loc.getBlock();
    Block head = feet.getRelative(0, 1, 0);
    Block ground = feet.getRelative(0, -1, 0);
    
    return feet.getType().isAir() && head.getType().isAir() && ground.getType().isSolid();
  }
  
  private boolean isRadiusClear(Location center, int r) {
    World world = center.getWorld();
    int cx = center.getBlockX();
    int cy = center.getBlockY();
    int cz = center.getBlockZ();
    int r2 = r * r;
    
    for(int x = -r; x <= r; x++) {
      for(int z = -r; z <= r; z++) {
        for(int y = -r; y <= r; y++) {
          int cmp = x*x + y*y + z*z;
          if(cmp > r2) {
            continue;
          }
          
          Location testLoc = new Location(world, x + cx, y + cy, z + cz);
          if(avoidList.contains(testLoc)) {
            return false;
          }
        }
      }
    }
    
    return true;
  }
}
