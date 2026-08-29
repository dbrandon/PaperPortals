package com.netgear.tubba.mc.portalpower;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.netgear.tubba.mc.portalpower.data.ControlBlockData;
import com.netgear.tubba.mc.portalpower.scan.BlockScanSet;
import com.netgear.tubba.mc.portalpower.scan.FloodFillScanner;

public class PortalPhysicsListener implements Listener {
  private PortalPowerPlugin plugin;
  
  private Set<Location> poweredBlockCache = new HashSet<Location>();
  
  public PortalPhysicsListener(PortalPowerPlugin plugin) {
    this.plugin = plugin;
  }
  
  @EventHandler
  public void onPortalLeftClickMute(PlayerInteractEvent event) {
    if(event.getAction() != Action.LEFT_CLICK_BLOCK || event.getClickedBlock() == null) {
      return;
    }

    // no easy way to resolve the broken glass sound in creative
    if(event.getClickedBlock().getType() == Material.NETHER_PORTAL) {
      event.getPlayer().stopSound(Sound.BLOCK_GLASS_BREAK);
    }
  }
  
  @EventHandler
  public void onPortalPhysics(BlockPhysicsEvent event) {
    Block block = event.getBlock();
    
    if(block.getType() == Material.NETHER_PORTAL) {
      event.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onRedstonePower(BlockRedstoneEvent event) {
    int old = event.getOldCurrent();
    int cur = event.getNewCurrent();

    // Only care if the value changed
    if(old == cur) {
      return;
    }
    
    Map<Block, ControlBlockData> map = plugin.getPortalRegistry().getLoadedControlBlocks();
    for(Block ctrlBlock : map.keySet()) {
      boolean powered = ctrlBlock.isBlockPowered();
      boolean indir = ctrlBlock.isBlockIndirectlyPowered();
      Location location = ctrlBlock.getLocation();
      plugin.getLogger().warning("Block " + (powered ? " ON" : "OFF") + (indir ? " ION" : "IOFF") + " at " + location);
      
      if(powered && !poweredBlockCache.contains(location)) {
        plugin.getLogger().warning("Block powered on");
        poweredBlockCache.add(location);
      }
      else if(!powered && poweredBlockCache.contains(location)) {
        plugin.getLogger().warning("Block powered off");
        poweredBlockCache.remove(location);
      }
    }
  }
  
  @EventHandler
  public void onFrameBreak(BlockBreakEvent event) {
    Set<Material> portalMatSet = plugin.getPortalPluginConfig().getPortalMaterialSet();
    Block broken = event.getBlock();
    
    plugin.getPortalRegistry().unregisterControlBlock(broken);

    if(portalMatSet.contains(broken.getType()) && plugin.getPortalRegistry().hasDestination(broken)) {
      event.setCancelled(true);
      // This is a nice thought but it doesn't appear to actually do anything
//      if(broken.getType() == Material.NETHER_PORTAL) {
//        event.getPlayer().stopSound(Sound.BLOCK_GLASS_BREAK);
//      }
      return;
    }
    
    BlockFace[] directions = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
    
    for(BlockFace face : directions) {
      Block adjacent = broken.getRelative(face);
      
      if(!portalMatSet.contains(adjacent.getType())) {
        continue;
      }

      BlockScanSet set = FloodFillScanner.findPortalSet(adjacent, broken, portalMatSet);
      if(set == null || set.isEmpty()) {
        continue;
      }

      for(Block block : set.getBlockSet()) {
        plugin.getPortalRegistry().unregisterPortalBlock(block);
        block.setType(Material.AIR);
      }
    }
  }
}
