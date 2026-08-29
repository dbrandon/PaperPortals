package com.netgear.tubba.mc.portalpower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RadarTask implements Runnable {
  
  private PortalPowerPlugin plugin;
  private Map<UUID, BossBar> playerBars = new HashMap<UUID, BossBar>();
  
  private Map<String, Location> fakeMap = new HashMap<String, Location>();
  private Map<String, ArmorStand> markerMap = new HashMap<String, ArmorStand>();
  
  public RadarTask(PortalPowerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    for(Player player : Bukkit.getOnlinePlayers()) {
      Component header = Component.text("");
      
      List<String> nearby = player.getWorld()
          .getPlayers()
          .stream()
          .filter(p -> !p.equals(player) && p.getLocation().distance(player.getLocation()) < 50)
          .map(Player::getName)
          .collect(Collectors.toList());
      
      StringBuilder sb = new StringBuilder();
      for(String name : fakeMap.keySet()) {
        nearby.add("{F/" + name + "}");
        sb.append("[F/" + name + "]");
        
        header = header.append(Component.text("■ ", NamedTextColor.RED)).append(Component.text(name, NamedTextColor.RED));
      }
      for(String n : nearby) {
        sb.append("[" + n + "]");
        header = header.append(Component.text("■ ", NamedTextColor.BLUE)).append(Component.text("F/" + n, NamedTextColor.BLACK));
      }
      
//      String title = nearby.isEmpty() ? "§7No players nearby" : "§eNearby: §f" + String.join(" §7| §7f", nearby);
//      BossBar bar = playerBars.computeIfAbsent(player.getUniqueId(), id -> {
//        BossBar b = Bukkit.createBossBar(title,  BarColor.YELLOW, BarStyle.SOLID);
//        b.addPlayer(player);
//        return b;
//      });
//      
//      bar.setTitle(title);
      
      if(!fakeMap.isEmpty() || !nearby.isEmpty()) {
        player.sendPlayerListHeader(header);
//        player.sendActionBar(
//            Component.text("Nearby: ").append(Component.text(sb.toString(), NamedTextColor.RED)));
      }
    }
  }
  
  public void cleanup() {
    for(BossBar bar : playerBars.values()) {
      bar.removeAll();  // remove from all players
    }
    playerBars.clear();
  }
  
  protected void setFakePlayer(String name, Location location) {
    fakeMap.put(name, location);
    spawnMarker(name, location);
  }
  
  protected boolean removeFakePlayer(String name) {
    Location location = fakeMap.remove(name);
    ArmorStand old = markerMap.remove(name);
    
    if(old != null) {
      old.remove();
    }
    
    return location != null;
  }
  
  protected List<String> getFakePlayerNames() {
    List<String> list = new ArrayList<String>(fakeMap.keySet());
    
    list.sort((a,b) -> a.compareTo(b));
    return list;
  }
  
  protected void clearFakePlayers() {
    this.fakeMap.clear();
    markerMap.values().forEach(ArmorStand::remove);
    markerMap.clear();
  }
  
  private void spawnMarker(String name, Location loc) {
    ArmorStand old = markerMap.remove(name);
    if(old != null) {
      old.remove();
    }
    
    ArmorStand stand = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
    stand.setCustomName("§e" + name + "§7(fake)");
    stand.setCustomNameVisible(true);
    stand.setInvisible(true);
    stand.setGravity(false);
    stand.setInvulnerable(true);
    stand.setGlowing(true);
    stand.setSmall(true);
    
    AttributeInstance transmitRange = stand.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE);
    if(transmitRange != null) {
      transmitRange.setBaseValue(500.0);
    }
    
    stand.setWaypointStyle(null);
    
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "waypoint modify " + stand.getUniqueId() + " color red");
    
    markerMap.put(name, stand);
  }
}
