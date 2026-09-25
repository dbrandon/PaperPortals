package com.tubba.mc.paper.portals;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import com.tubba.mc.paper.portals.util.WorldName;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PortalPowerPlugin extends JavaPlugin {
  private final static String GETTOOL_COMMAND = "gettool";
  
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
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(!(sender instanceof Player)) {
      sender.sendMessage("§cThis command must be run as a player.");
      return true;
    }
        
    getLogger().warning("Command: " + command.getName() + "; label=[" + label + "] #args=" + args.length);
    
    if(args.length == 0) {
      sender.sendMessage("No argument was given");
      return true;
    }
    
    switch(args[0].toLowerCase()) {
    case GETTOOL_COMMAND:
      if(args.length != 3) {
        sender.sendMessage("Missing arguments");
        return true;
      }
      
      giveToolToPlayer(sender, args[1], args[2]);
      return true;
      
    case "debugdump": 
      portalRegistry.dumpLoadedBlocks();
      return true;
    }
    
    return true;
  }
  
  private void giveToolToPlayer(CommandSender sender, String worldRef, String playerRef) {
    WorldName world = WorldName.lookup(worldRef);
    Player player = Bukkit.getPlayerExact(playerRef);
    
    if(world == null) {
      sender.sendMessage("Unknown world name");
      return;
    }
    if(player == null) {
      sender.sendMessage("Player was not found");
      return;
    }
    
    ItemStack linkerTool = igniterManager.createIgniter(world);//WorldName.OVERWORLD);
    if(!player.getInventory().addItem(linkerTool).isEmpty()) {
      player.getWorld().dropItemNaturally(player.getLocation(), linkerTool);
      player.sendMessage(Component.text("Someone tried to send you a portal igniter but your inventory was full and the item was dropped.", NamedTextColor.YELLOW));
    }
    else {
      player.sendMessage(Component.text("You have received a gunter portal igniter", NamedTextColor.GREEN));
    }
  }
  
  @Override
  public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    getLogger().warning("ARGS length=" + args.length);
    if(args.length == 1) {
      return List.of(GETTOOL_COMMAND, "debugdump");
    }
    if(args[0].equals(GETTOOL_COMMAND)) {
      if(args.length == 2) {
        return List.of(WorldName.OVERWORLD.getNamespacedKey().asString(),
            WorldName.NETHER.getNamespacedKey().asString(),
            WorldName.END.getNamespacedKey().asString());
      }
      if(args.length == 3) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
      }
    }
  
    return List.of();
  }
}
