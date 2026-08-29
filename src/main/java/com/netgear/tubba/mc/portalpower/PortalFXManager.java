package com.netgear.tubba.mc.portalpower;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;

import com.netgear.tubba.mc.portalpower.PortalRegistry.RegisteredBlockType;
import com.netgear.tubba.mc.portalpower.data.PortalBlockData;
import com.netgear.tubba.mc.portalpower.data.PortalColor;

public class PortalFXManager extends BukkitRunnable {
  private PortalPowerPlugin plugin;
  private Map<Color, Particle.DustOptions> dustOptionMap;
  private Map<Integer, Color> colorMap = new HashMap<Integer, Color>();
  
  public PortalFXManager(PortalPowerPlugin plugin) {
    this.plugin = plugin;
    this.dustOptionMap = new HashMap<Color, Particle.DustOptions>();
  }
  
  @Override
  public void run() {
    Set<Material> matSet = plugin.getPortalPluginConfig().getPortalMaterialSet();
    
    for(World world : plugin.getServer().getWorlds()) {
      for(Chunk chunk : world.getLoadedChunks()) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        
        for(NamespacedKey key : pdc.getKeys()) {
          if(!key.getNamespace().equals(plugin.getName().toLowerCase())) {
            continue;
          }
          
          Block block = plugin.getPortalRegistry().blockOf(key, chunk, RegisteredBlockType.PortalForward);
          if(block == null || !matSet.contains(block.getType())) {
            continue;
          }
          PortalBlockData blockData = plugin.getPortalRegistry().getPortalBlockData(block);
          if(blockData == null) {
            continue;
          }
          if(blockData.hasPortalActive() && !blockData.getPortalActive()) {
            continue;
          }
          
          spawnGridParticles(world, block, blockData);
        }
      }
    }
  }
  
  private void spawnGridParticles(World world, Block block, PortalBlockData data) {
    double offsetX = 0.3;
    double offsetZ = 0.3;
    
    if(block.getBlockData() instanceof Orientable orientable) {
      if(orientable.getAxis() == Axis.X) {
        offsetZ = 0.05;
      }
      else {
        offsetX = 0.05;
      }
    }
    
    world.spawnParticle(
        Particle.DUST,
        block.getLocation().add(0.5, 0.5, 0.5),
        2,  // count
        offsetX, 0.4, offsetZ,
        0.0,
        getDustOptions(data));
  }
  
  private Particle.DustOptions getDustOptions(PortalBlockData data) {
    if(data == null || !data.hasPortalColor() || data.getPortalColor().getRgbaCount() == 0) {
      return getOrCreateDustOption(Color.AQUA);
    }
    
    PortalColor pc = data.getPortalColor();
    Color color = null;
    int n = (int)Math.round(Math.random() * pc.getRgbaCount());
    if(n >= pc.getRgbaCount()) {
      n = pc.getRgbaCount() - 1;
    }
    int rgba = pc.getRgba(n);
    color = colorMap.get(rgba);
    if(color == null) {
      color = Color.fromARGB(rgba);
      colorMap.put(rgba, color);
    }
//    
//    if(pc.hasRgba()) {
//      color = colorMap.get(pc.getRgba());
//      if(color == null) {
//        color = Color.fromARGB(pc.getRgba());
//        colorMap.put(pc.getRgba(), color);
//      }
//    }
//    
//    if(pc.hasPrismatic() && pc.getPrismatic()) {
//      int n = (int)Math.round(Math.random() * PRISMATIC.length);
//      if(n >= PRISMATIC.length) {
//        n = PRISMATIC.length -1;
//      }
//      color = PRISMATIC[n];
//    }
    
    if(color == null) {
      color = Color.AQUA;
    }
    
    return getOrCreateDustOption(color);
  }
  
  private Particle.DustOptions getOrCreateDustOption(Color color) {
    Particle.DustOptions option = dustOptionMap.get(color);
    
    if(option == null) {
      option = new Particle.DustOptions(color, 1.1F);
      dustOptionMap.put(color, option);
    }
    
    return option;
  }
}
