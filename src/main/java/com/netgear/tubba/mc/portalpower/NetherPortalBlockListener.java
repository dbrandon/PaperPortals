package com.netgear.tubba.mc.portalpower;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

import io.papermc.paper.entity.poi.PoiSearchResult;
import io.papermc.paper.entity.poi.PoiTypes;

/**
 * Block portal events from using custom portals
 */
public class NetherPortalBlockListener implements Listener {
  private PortalPowerPlugin plugin;
  
  public NetherPortalBlockListener(PortalPowerPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Minecraft doesn't link portals between the worlds.  Instead, when a player enters a portal the
   * server calculates the target in the destination world and searches for an existing nether portal block.
   * The server keeps a "point of interest" cache that it uses to quick find a portal block and any found
   * are treated as valid.  The custom portals also use nether portal blocks so we need to exclude those from
   * being used as travel destinations.
   * 
   * To exclude the custom portals, a fast POI search is used to locate an existing vanilla portal.  If found,
   * that is used.  If none are found then a search is initiated to find a location that within a safe range
   * of any existing custom portals to avoid breaking the portals.
   * 
   * @param event
   */
  @EventHandler
  public void onIncomingPortalLink(PlayerPortalEvent event) {
    plugin.getLogger().warning("incoming portal event - " + event.getPlayer().getName());
    
    Location dest = event.getTo();
    if(dest == null || dest.getWorld() == null) {
      return;
    }
    
    // Cancel the default event if the player is departing from a custom portal
    Location from = event.getFrom();
    if(from != null && from.getWorld() != null && isFromCustomPortal(from)) {
      plugin.getLogger().warning("Source is custom portal; will cancel event!");
      event.setTo(event.getFrom());
      event.setCancelled(true);
      return;
    }
    
    // Find all nether portal blocks in range of the portal event
    List<PoiSearchResult> poiList = dest.getWorld().locateAllPoiInRange(dest, poi -> poi == PoiTypes.NETHER_PORTAL, 127);
    List<Location> customPortalList = new ArrayList<Location>();
    Location redirectTo = null;

    for(PoiSearchResult result : poiList) {
      Location l = result.location();
      Location customOutput = plugin.getPortalRegistry().getDestination(l.getBlock());

      // no custom output means it's a vanilla portal and thus a safe destination
      if(customOutput == null) {
        redirectTo = l;
        break;
      }
      
      customPortalList.add(l);
    }
    
    // No vanilla portal was found so find a location a safe distance away from any custom portals
    if(redirectTo == null) {
      PortalSpawnFinder finder = new PortalSpawnFinder(customPortalList);
      redirectTo = finder.findSafeSpawnLocation(dest, 6, 127);
    }
    
    if(redirectTo != null) {
      event.setTo(redirectTo);
      event.setSearchRadius(0);
      event.setCanCreatePortal(true);
    }
  }
  
  private boolean isFromCustomPortal(Location from) {
    int startx = from.getBlockX() - 1;
    int starty = from.getBlockY();
    int startz = from.getBlockZ() - 1;
    
    for(int x = startx; x <= startx+2; x++) {
      for(int y = starty; y <= starty+2; y++) {
        for(int z = startz; z <= startz+2; z++) {
          Block block = from.getWorld().getBlockAt(x, y, z);
          
          if(block.getType() != Material.NETHER_PORTAL) {
            continue;
          }
          Location dest = plugin.getPortalRegistry().getDestination(block);
          if(dest != null) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
