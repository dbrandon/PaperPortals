package com.netgear.tubba.mc.portalpower;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.format.NamedTextColor;

public class PortalPowerPlugin extends JavaPlugin {
  private PortalListener portalListener;
  private PortalPluginConfig portalPluginConfig;
  private PortalRegistry portalRegistry;
  
  private RhumbEyeManager rhumbEyeManager;
  private IgniterManager igniterManager;
  
  public PortalPowerPlugin() {
  }
  
  public PortalPluginConfig getPortalPluginConfig() {
    return portalPluginConfig;
  }
  
  public PortalRegistry getPortalRegistry() {
    return portalRegistry;
  }
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
    
    getLogger().info("Portal Power plugin enabled!!");
    
    portalPluginConfig = new PortalPluginConfig(this);
    portalRegistry = new PortalRegistry(this);
    
    rhumbEyeManager = new RhumbEyeManager(this);
    rhumbEyeManager.registerRecipes();
    
    
    igniterManager = new IgniterManager(this, rhumbEyeManager);
    portalListener = new PortalListener(this, igniterManager);
    getServer().getPluginManager().registerEvents(portalListener, this);
//    getServer().addRecipe(linkerManager.createConcentratedEyeRecipe());
//    
//    GunterPortalIgniter igniter = new GunterPortalIgniter(this, igniterManager);
//    getServer().getPluginManager().registerEvents(igniter, this);
    
    PortalPhysicsListener physicsListener = new PortalPhysicsListener(this);
    getServer().getPluginManager().registerEvents(physicsListener, this);
    
    NetherPortalBlockListener blockListener = new NetherPortalBlockListener(this);
    getServer().getPluginManager().registerEvents(blockListener, this);
    
    new PortalFXManager(this).runTaskTimer(this, 0L, 6L);
  }
  
  @Override
  public void onDisable() {
    // cleanup boss bar upon unload
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(!(sender instanceof Player)) {
      sender.sendMessage("§cThis command must be run as a player.");
      return true;
    }
    
    Player player = (Player)sender;
    
    getLogger().warning("Command: " + command.getName() + "; label=[" + label + "] #args=" + args.length);
    
    if(args.length == 0) {
      sender.sendMessage("No argument was given");
      return true;
    }
    
    switch(args[0].toLowerCase()) {
    case "gettool":
      giveToolToPlayer(player);
      return true;
      
    case "debugdump": 
      portalRegistry.dumpLoadedBlocks();
      return true;
    }
    
    
    if(true) {
      return true;
    }
    
    if(args.length == 0) {
//      sendHelp(player);
      return true;
    }
    
    switch(args[0].toLowerCase()) {
    case "fakeadd": {
      if(args.length < 3) {
        player.sendMessage("§cUsage: /radar fakeadd <name> <distance>");
        return true;
      }
      
      String name = args[1];
      Location loc = getLocationFromDistance(player, args[2]);
      if(loc != null) {
//        radarTask.setFakePlayer(name, loc);
        player.sendMessage("§aAdded fake player §f" + name + " §aat distance §f" + args[2]);
      }
      break;
    }
      
      
    case "fakemove": {
      if(args.length < 3) {
        player.sendMessage("§cUsage: /radar fakeremove <name> <distance>");
        return true;
      }
      
      String name = args[1];
      Location loc = getLocationFromDistance(player, args[2]);
      if(loc != null) {
//        radarTask.setFakePlayer(name, loc);
        player.sendMessage("§aMoved fake player §f" + name + " §ato distance §f" + args[2]);
      }
      break;
    }
      
    case "fakelist": {
//      List<String> nameList = radarTask.getFakePlayerNames();
//      if(nameList.isEmpty()) {
//        player.sendMessage("§aNo fake players created");
//        return true;
//      }
//      
//      for(String name : nameList) {
//        player.sendMessage("§aFake player named §f" + name);
//      }
      break;
    }
      
    case "fakeclear":
      player.sendMessage("§aAll fake players cleared.");
//      radarTask.clearFakePlayers();
      break;
      
//    default: sendHelp(player);
    }
    
    return true;
  }
  
  private void giveToolToPlayer(Player player) {
    ItemStack linkerTool = igniterManager.createIgniter(LocationEncoder.WorldName.OVERWORLD);
    if(!player.getInventory().addItem(linkerTool).isEmpty()) {
      player.getWorld().dropItemNaturally(player.getLocation(), linkerTool);
      player.sendMessage(NamedTextColor.YELLOW + "Your inventory was full!  Linker was dropped.");
    }
    else {
      player.sendMessage(NamedTextColor.GREEN + "Your have been given a thingy!");
    }
  }
  
  @Override
  public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if(args.length == 1) {
      return List.of("gettool", "debugdump");
    }
    return List.of();
//    if(args.length == 1) {
//      return List.of("fakeadd", "fakemove", "fakeremove", "fakelist", "fakeclear")
//          .stream()
//          .filter(s -> s.startsWith(args[0].toLowerCase()))
//          .collect(Collectors.toList());
//    }
//    if(args.length == 2 && List.of("fakemove", "fakeremove").contains(args[0].toLowerCase())) {
//      return radarTask.getFakePlayerNames()
//          .stream()
//          .filter(s -> s.startsWith(args[1]))
//          .collect(Collectors.toList());
//    }
//    return List.of();
  }
  
//  private void sendHelp(Player player) {
//    player.sendMessage("§e--- Radar Debug commands ---");
//    player.sendMessage("§f/radar fakeadd <name> <distance>");
//    player.sendMessage("§f/radar fakemove <name> <distance.");
//    player.sendMessage("§f/radar fakeremove <name>");
//    player.sendMessage("§f/radar fakelist");
//    player.sendMessage("§f/radar fakeclear");
//  }
//  
  private Location getLocationFromDistance(Player player, String distanceStr) {
    double distance;
    
    try {
      distance = Double.parseDouble(distanceStr);
    }
    catch(Exception ex) {
      player.sendMessage("§cDistance must be a number");
      return null;
    }
    
    return player.getLocation().add(player.getLocation().getDirection().multiply(distance));
  }
}
