package com.netgear.tubba.mc.portalpower;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

import com.netgear.tubba.mc.portalpower.data.ControlBlockData;
import com.netgear.tubba.mc.portalpower.data.PortalBlockData;
import com.netgear.tubba.mc.portalpower.util.ConvertUtil;
import com.netgear.tubba.mc.portalpower.util.ProtoPersistentDataType;

public class PortalRegistry {
  public enum RegisteredBlockType {
    PortalControl("pctl", null),
    PortalForward("pfwd", Set.of(Material.NETHER_PORTAL, Material.RESPAWN_ANCHOR));
    
    private String key;
    
    private RegisteredBlockType(String key, Set<Material> materialSet) {
      this.key = key;
    }
    
    public static RegisteredBlockType lookup(String key) {
      for(RegisteredBlockType type : values()) {
        if(type.key.equals(key)) {
          return type;
        }
      }
      
      return null;
    }
  }
  
  private PortalPowerPlugin plugin;
  private Map<String, ControlBlockData> controlMap = new HashMap<String, ControlBlockData>();
  private ProtoPersistentDataType<ControlBlockData> controlBlockHandler = new ProtoPersistentDataType<ControlBlockData>(ControlBlockData.class, ControlBlockData.parser());
  
  private Map<String, PortalBlockData> portalMap = new HashMap<String, PortalBlockData>();
  private ProtoPersistentDataType<PortalBlockData> portalBlockHandler = new ProtoPersistentDataType<PortalBlockData>(PortalBlockData.class, PortalBlockData.parser());
  
  public PortalRegistry(PortalPowerPlugin plugin) {
    this.plugin = plugin;
  }
  
  private NamespacedKey toNamespacedKey(Block block, RegisteredBlockType type) {
    return new NamespacedKey(plugin, type.key + LocationEncoder.SEP + LocationEncoder.encode(block));
  }
  
  public Block blockOf(NamespacedKey key, Chunk chunk, RegisteredBlockType requestType) {
    String[] parts = key.getKey().split(LocationEncoder.SEP);
    if(parts.length != 5) {
      plugin.getLogger().warning("Removing unsupported key - [" + key + "]");
      chunk.getPersistentDataContainer().remove(key);
      return null;
    }
    
    RegisteredBlockType type = RegisteredBlockType.lookup(parts[0]);
    if(type == null) {
      plugin.getLogger().warning("Removing key with unknown block type - [" + key + "]");
      chunk.getPersistentDataContainer().remove(key);
      return null;
    }
    if(type != requestType) {
      return null;
    }
    
    int x = Integer.parseInt(parts[2]);
    int y = Integer.parseInt(parts[3]);
    int z = Integer.parseInt(parts[4]);
    
    Block block = chunk.getBlock(x & 15,  y, z & 15);
    if(type == RegisteredBlockType.PortalControl) {
      if(!block.isSolid()) {
        plugin.getLogger().warning("Removing stale control key with incorrect block type: Key[" + key + "]; expected solid block");
        chunk.getPersistentDataContainer().remove(key);
        controlMap.remove(LocationEncoder.encode(block));
        return null;
      }
      return block;
    }
    Set<Material> matSet = plugin.getPortalPluginConfig().getPortalMaterialSet();
    if(!matSet.contains(block.getType())) {
      plugin.getLogger().warning("Removing stale key with incorrect block type: Key[" + key + "]; expected material=" + matSet + "; actual=" + block.getType());
      chunk.getPersistentDataContainer().remove(key);
      return null;
    }

    return block;
  }
  
  public Map<Block, ControlBlockData> getLoadedControlBlocks() {
    Map<Block, ControlBlockData> map = new HashMap<Block, ControlBlockData>();
    
    for(World world : plugin.getServer().getWorlds()) {
      for(Chunk chunk : world.getLoadedChunks()) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        
        for(NamespacedKey key : pdc.getKeys()) {
          if(!key.getNamespace().equals(plugin.getName().toLowerCase())) {
            continue;
          }
          
          Block block = blockOf(key, chunk, RegisteredBlockType.PortalControl);
          if(block == null) {
            continue;
          }
          ControlBlockData data = getControlBlockData(block);
          if(data != null) {
            map.put(block, data);
          }
        }
      }
    }
    
    return map;
  }
  
  public void dumpLoadedBlocks() {
    int count = 0;
    
    for(World world : plugin.getServer().getWorlds()) {
      for(Chunk chunk : world.getLoadedChunks()) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        
        for(NamespacedKey key : pdc.getKeys()) {
          if(!key.getNamespace().equals(plugin.getName().toLowerCase())) {
            continue;
          }
        
          Block block = blockOf(key, chunk, RegisteredBlockType.PortalForward);
          if(block == null || block.getType() != Material.NETHER_PORTAL) {
            continue;
          }
          
          plugin.getLogger().warning("Registered block " + block.getLocation());
          count++;
        }
      }
    }
    
    plugin.getLogger().warning("Total of " + count + " registered blocks found in loaded chunks.");
  }
  
  public void registerControlBlock(Block block, ControlBlockData data) {
    PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();

    Bukkit.getServer().getLogger().warning("register control block: " + block.getLocation());
    pdc.set(toNamespacedKey(block, RegisteredBlockType.PortalControl), controlBlockHandler, data);
    controlMap.put(LocationEncoder.encode(block), data);
  }
  
  public void unregisterControlBlock(Block block) {
    PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
    
    pdc.remove(toNamespacedKey(block, RegisteredBlockType.PortalControl));
    controlMap.remove(LocationEncoder.encode(block));
  }

  public void registerPortalBlock(Block block, PortalBlockData data) {
    PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
    
    if(data == null || !data.hasDestination()) {
      throw new IllegalStateException("Portal block data missing a destination");
    }
  
    pdc.set(toNamespacedKey(block, RegisteredBlockType.PortalForward), portalBlockHandler, data);
    portalMap.put(LocationEncoder.encode(block.getLocation()), data);
  }
  
  public void updatePortalBlock(Block block, PortalBlockData data) {
    PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
    
    pdc.set(toNamespacedKey(block, RegisteredBlockType.PortalForward), portalBlockHandler, data);
    portalMap.put(LocationEncoder.encode(block.getLocation()), data);
  }
  
  public void unregisterPortalBlock(Block block) {
    Chunk chunk = block.getChunk();
    PersistentDataContainer pdc = chunk.getPersistentDataContainer();
    
    pdc.remove(toNamespacedKey(block, RegisteredBlockType.PortalForward));
    portalMap.remove(LocationEncoder.encode(block.getLocation()));
  }
  
  public boolean hasDestination(Block block) {
    Chunk chunk = block.getChunk();
    PersistentDataContainer pdc = chunk.getPersistentDataContainer();

    return pdc.has(toNamespacedKey(block, RegisteredBlockType.PortalForward));
  }
  
  public Location getDestination(Block block) {
    PortalBlockData data = getPortalBlockData(block);
    if(data == null || !data.hasDestination()) {
      return null;
    }
    
    return ConvertUtil.convert(data.getDestination());
  }
  
  public ControlBlockData getControlBlockData(Block block) {
    String key = LocationEncoder.encode(block);
    ControlBlockData data = controlMap.get(key);
    
    if(data == null) {
      PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
      data = pdc.get(toNamespacedKey(block, RegisteredBlockType.PortalControl), controlBlockHandler);
      if(data != null) {
        controlMap.put(key, data);
      }
    }
    
    return data;
  }
  
  public PortalBlockData getPortalBlockData(Block block) {
    String key = LocationEncoder.encode(block);
    PortalBlockData data = portalMap.get(key);
    
    if(data == null) {
      data = lookupPortalBlockData(block);
      if(data != null) {
        portalMap.put(key, data);
      }
    }
    
    return data;
  }
  
  private PortalBlockData lookupPortalBlockData(Block block) {
    Chunk chunk = block.getChunk();
    PersistentDataContainer pdc = chunk.getPersistentDataContainer();
    
    return pdc.get(toNamespacedKey(block, RegisteredBlockType.PortalForward), portalBlockHandler);
  }
}
