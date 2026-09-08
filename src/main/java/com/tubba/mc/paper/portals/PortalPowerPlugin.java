package com.tubba.mc.paper.portals;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import com.tubba.mc.paper.portals.util.WorldName;

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
    
    
    if(args.length == 0) {
//      sendHelp(player);
    }
    
    return true;
  }
  
  private void giveToolToPlayer(Player player) {
    ItemStack linkerTool = igniterManager.createIgniter(WorldName.OVERWORLD);
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
}
