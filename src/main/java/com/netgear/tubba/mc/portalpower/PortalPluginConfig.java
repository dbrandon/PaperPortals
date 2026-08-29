package com.netgear.tubba.mc.portalpower;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;

public class PortalPluginConfig {
  private final static Material DEFAULT_VERT_MATERIAL = Material.NETHER_PORTAL;
  private final static Material DEFAULT_HORI_MATERIAL = Material.RESPAWN_ANCHOR;
  
  private PortalPowerPlugin plugin;
  private Set<Material> portalMaterialSet;
  
  private Material portalHorizontalBlockType;
  private Material portalVerticalBlockType;
  
  public PortalPluginConfig(PortalPowerPlugin plugin) {
    this.plugin = plugin;
  }
  
  public Set<Material> getPortalMaterialSet() {
    if(portalMaterialSet == null) {
      List<String> list = plugin.getConfig().getStringList("portal-block-type-list");
      portalMaterialSet = new HashSet<Material>();
    
      if(list != null) {
        for(String name : list) {
          Material mat = Material.matchMaterial(name);
        
          if(mat == null) {
            plugin.getLogger().warning("Unknown portal block material type: " + name);
          }
          else {
            portalMaterialSet.add(mat);
          }
        }
      }
    }
    
    return portalMaterialSet;
  }
  
  public Material getPortalHorizontalBlockType() {
    if(portalHorizontalBlockType == null) {
      String matName = plugin.getConfig().getString("portal-horizontal-block-type");
      
      if(matName == null) {
        portalHorizontalBlockType = DEFAULT_HORI_MATERIAL;
      }
      else {
        portalHorizontalBlockType = Material.matchMaterial(matName);
        if(portalHorizontalBlockType == null) {
          plugin.getLogger().warning("Unknown horizontal block type: " + matName);
          portalHorizontalBlockType = DEFAULT_HORI_MATERIAL;
        }
      }
    }
    
    return portalHorizontalBlockType;
  }

  public Material getPortalVerticalBlockType() {
    if(portalVerticalBlockType == null) {
      String matName = plugin.getConfig().getString("portal-vertical-block-type");
      
      if(matName == null) {
        portalVerticalBlockType = DEFAULT_VERT_MATERIAL;
      }
      else {
        portalVerticalBlockType = Material.matchMaterial(matName);
        if(portalVerticalBlockType == null) {
          plugin.getLogger().warning("Unknown vertical block type: " + matName);
          portalVerticalBlockType = DEFAULT_VERT_MATERIAL;
        }
      }
    }
    
    return portalVerticalBlockType;
  }
}
