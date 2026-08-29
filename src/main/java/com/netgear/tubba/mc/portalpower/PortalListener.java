package com.netgear.tubba.mc.portalpower;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.netgear.tubba.mc.portalpower.data.ControlBlockData;
import com.netgear.tubba.mc.portalpower.data.IgniterData;
import com.netgear.tubba.mc.portalpower.data.PortalBlockData;
import com.netgear.tubba.mc.portalpower.scan.BlockScanSet;
import com.netgear.tubba.mc.portalpower.scan.FloodFillScanner;
import com.netgear.tubba.mc.portalpower.util.BlockInteractionTest;
import com.netgear.tubba.mc.portalpower.util.ConvertUtil;
import com.netgear.tubba.mc.portalpower.util.StackConsumer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PortalListener implements Listener {
  private final static int DEFAULT_PORTAL_TICKS = 50;
  
  private PortalPowerPlugin plugin;
  private IgniterManager igniterManager;
  
  private Map<UUID, BukkitRunnable> pendingTeleports = new HashMap<UUID, BukkitRunnable>();
  
  public PortalListener(PortalPowerPlugin plugin, IgniterManager igniterManager) {
    this.plugin = plugin;
    this.igniterManager = igniterManager;
  }
  
  // These don't seem to work
//  private void startPortalAnimation(Player player) {
//    Bukkit.getServer().getLogger().warning("Sending player start portal animation");
//    sendPacket(player, (byte)46);
//  }
//  
//  private void stopPortalAnimation(Player player) {
//    Bukkit.getServer().getLogger().warning("Stopping player portal animation");
//    sendPacket(player, (byte)47);
//  }
//  
//  private void sendPacket(Player player, byte value) {
//    ServerPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
//    ClientboundEntityEventPacket packet = new ClientboundEntityEventPacket(nmsPlayer, value);
//    
//    nmsPlayer.connection.send(packet);
//  }
  
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Location from = event.getFrom();
    Location to = event.getTo();
    
    if(from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
      return;
    }
    
    Player player = event.getPlayer();
    Block block = event.getTo().getBlock();
    
    Set<Material> matSet = plugin.getPortalPluginConfig().getPortalMaterialSet();
    boolean matMatch = matSet.contains(block.getType());
    if(!matMatch) {
      for(Material mat : matSet) {
        if(mat.isSolid() && block.getRelative(BlockFace.DOWN).getType() == mat) {
          matMatch = true;
          block = block.getRelative(BlockFace.DOWN);
          break;
        }
      }
    }
    if(!matMatch) {
      return;
    }
    
    PortalBlockData data = plugin.getPortalRegistry().getPortalBlockData(block);
    if(data == null || !data.hasDestination()) {
      return;
    }
    if(data.hasPortalActive() && !data.getPortalActive()) {
      return;
    }
    
    int ticks = data.hasTicks() ? data.getTicks() : DEFAULT_PORTAL_TICKS;
    
    startTeleport(player, ConvertUtil.convert(data.getDestination()), ticks);
  }
  
  /**
   * Begin the teleportation
   * @param player      player to teleport
   * @param destination where to send the player
   * @param ticks       number of ticks to wait before teleporting
   */
  private void startTeleport(Player player, Location destination, int ticks) {
    if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || ticks == 0) {
      teleportPlayerNow(player, destination);
    }
    
    if(pendingTeleports.containsKey(player.getUniqueId())) {
      return;
    }
    
//    player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 1.0f);
//    Bukkit.getScheduler().runTaskLater(plugin, () -> startPortalAnimation(player), 2L);
//    startPortalAnimation(player);
    
//    Location startingLocation = player.getLocation().clone();
    BukkitRunnable teleportTask = new BukkitRunnable() {
      int ticksElapsed = 0;
      Location referenceLocation = player.getLocation().clone();
      
      {
        player.sendBlockChange(referenceLocation, Material.NETHER_PORTAL.createBlockData());
      }
      
      @Override
      public void run() {
        if(!player.isOnline()) {
          cancelPortal(player, referenceLocation);
          return;
        }
        
        if(player.getLocation().distanceSquared(referenceLocation) > 1.5) {
          Location currentDestination = plugin.getPortalRegistry().getDestination(player.getLocation().getBlock());
          if(currentDestination != null && currentDestination.equals(destination)) {
            restoreCustomPortalBlock(player, referenceLocation);
            referenceLocation = player.getLocation().clone();
            player.sendBlockChange(referenceLocation, Material.NETHER_PORTAL.createBlockData());
          }
          else {
            cancelPortal(player, referenceLocation);
            return;
          }
        }
        
        ticksElapsed += 10;
        
        if(ticksElapsed >= ticks) {
          cancelPortal(player, referenceLocation);
          teleportPlayerNow(player, destination);
        }
      }
    };
    
    teleportTask.runTaskTimer(plugin, 0L, 10L);
    pendingTeleports.put(player.getUniqueId(), teleportTask);
  }
  
  private void cancelPortal(Player player, Location playerLocation) {
    UUID uuid = player.getUniqueId();
    
    restoreCustomPortalBlock(player, playerLocation);
    if(pendingTeleports.containsKey(uuid)) {
      pendingTeleports.get(uuid).cancel();
      pendingTeleports.remove(uuid);
      
//      stopPortalAnimation(player);
    }
  }
  
  private void restoreCustomPortalBlock(Player player, Location location) {
    player.sendBlockChange(location, location.getBlock().getBlockData());
  }
  
  private void teleportPlayerNow(Player player, Location destination) {
    player.teleportAsync(destination);
    destination.getWorld().playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
//    Player player = event.getPlayer();
//    
//    getLogger().info("Welcome player " + player.getName());
  }
  
  @EventHandler
  public void onIgniterInteract(PlayerInteractEvent event) {
    if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    
    ItemStack heldItem = event.getItem();
    if(!igniterManager.isIgniter(heldItem)) {
      return;
    }
    
    Block clickedBlock = event.getClickedBlock();
    if(clickedBlock == null) {
      return;
    }

    if(clickedBlock.getType() == Material.CARTOGRAPHY_TABLE || !isInteractable(clickedBlock)) {
      event.setCancelled(true);
    }
    
    if(clickedBlock.getType() == Material.CARTOGRAPHY_TABLE) {
      handleIgniterBind(heldItem, clickedBlock, event.getPlayer());
      return;
    }
    
    handlePortalIgnite(heldItem, clickedBlock, event.getBlockFace(), event.getPlayer());
  }
  
  private boolean isInteractable(Block block) {
    return BlockInteractionTest.isSwitchOrToggle(block) || BlockInteractionTest.opensMenu(block);
  }
  
  private void handleIgniterBind(ItemStack igniter, Block clickedBlock, Player player) {
    if(!igniterManager.bindToBlockLocation(igniter, clickedBlock, player)) {
      return;
    }
    Location location = clickedBlock.getLocation();
    clickedBlock.setType(Material.AIR);
    location.getWorld().strikeLightningEffect(location);
    
    new BukkitRunnable() {
      private int secondsElapsed = 0;
      private int ticksElapsed = 0;
      
      @Override
      public void run() {
        if(secondsElapsed >= 3) {
          this.cancel();
          return;
        }
        
        location.getWorld().spawnParticle(Particle.COPPER_FIRE_FLAME, location, 15, 0, 1, 0, 0.1);
        if(ticksElapsed % 20 == 0) {
          secondsElapsed++;
        }
        ticksElapsed += 5;
      }
    }.runTaskTimer(plugin, 0L, 5L);
    //location.getWorld().createExplosion(location, 0.0F);
  }
  
  /**
   * Checks to see if the ignite attempt was successful but checking the clicked block and block face
   * and ensuring that the igniter was already bound to a location.
   * @param igniter
   * @param clickedBlock
   * @param clickedBlockFace
   * @param player
   * @return
   */
  private boolean handlePortalIgnite(ItemStack igniter, Block clickedBlock, BlockFace clickedBlockFace, Player player) {
    IgniterData data = igniterManager.getIgniterData(igniter);
    if(data == null || !data.hasAttunedLocation()) {
      return false;
    }
//    Location targetLocation = ConvertUtil.convert(data.getAttunedLocation());
//    Location targetLocation = igniterManager.getIgniterBoundLocation(igniter);
//    if(targetLocation == null) {
//      return false;
//    }
    
    Block targetAir = clickedBlock.getRelative(clickedBlockFace);
    if(targetAir.getType() != Material.AIR && targetAir.getType() != Material.CAVE_AIR) {
      return false;
    }

    BlockScanSet set = FloodFillScanner.findIgniterSet(targetAir, clickedBlock);
    if(set == null || set.isEmpty()) {
      return false;
    }
    
    // Create the portal blocks
    boolean isY = set.getAxis() == Axis.Y;
    Material portalMaterial = isY ? plugin.getPortalPluginConfig().getPortalHorizontalBlockType() :
      plugin.getPortalPluginConfig().getPortalVerticalBlockType();
    
    PortalBlockData.Builder blockDataBuilder = PortalBlockData.newBuilder()
        .setPortalActive(true)
        .setDestination(data.getAttunedLocation())
        .setTicks(isY ? 0 : DEFAULT_PORTAL_TICKS);
    if(data.hasAttunedColor()) {
      blockDataBuilder.setPortalColor(data.getAttunedColor());
    }
    PortalBlockData blockData = blockDataBuilder.build();
    
    ControlBlockData.Builder controlBlockBuilder = null;
    if(data.hasRedstoneActivated() && data.getRedstoneActivated()) {
      controlBlockBuilder = ControlBlockData.newBuilder();
    }
    
    for(Block b : set.getBlockSet()) {
      b.setType(portalMaterial);
      if(!isY && b.getBlockData() instanceof Orientable orientable) {
        orientable.setAxis(set.getAxis());
        b.setBlockData(orientable, false);
      }
      plugin.getPortalRegistry().registerPortalBlock(b, blockData);
      if(controlBlockBuilder != null) {
        controlBlockBuilder.addPortalBlock(ConvertUtil.convert(b.getLocation()));
      }
    }
    
    if(controlBlockBuilder != null) {
      plugin.getLogger().warning("Registering control block!");
      plugin.getPortalRegistry().registerControlBlock(clickedBlock, controlBlockBuilder.build());
    }
    
    if(player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      igniterManager.itemUsed(igniter);
    }

    player.sendMessage(Component.text("New gateway established!", NamedTextColor.GREEN));
    return true;
  }
  
  @EventHandler
  public void onPrepareAnvil(PrepareAnvilEvent event) {
    ItemAnvilBind bind = igniterManager.bindToIgniter(event.getView());
    if(bind == null || bind.getOutput() == null) {
      return;
    }
    
    event.setResult(bind.getOutput());
    plugin.getServer().getScheduler().runTask(plugin, () -> bind.handleAnvilPrepare(event.getView()));
  }
  
  /**
   * Handle the case when a stack of rhumb eyes is placed on the anvil.  By default,
   * the entire stack gets consumed when the item is crafted so instead intercept and
   * replace the stack with a new stack containing one fewer item.
   * @param event
   */
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if(event.getView().getTopInventory().getType() != InventoryType.ANVIL) {
      return;
    }
    
    AnvilInventory inven = (AnvilInventory)event.getView().getTopInventory();
    
    // Index 2 is the anvil's final output item slot
    if(event.getRawSlot() != 2 || inven.getResult() == null) {
      return;
    }

    if(!igniterManager.canBeIgniter(inven.getFirstItem())) {
      return;
    }
    
    StackConsumer.create(plugin, inven.getSecondItem()).accept(inven);
  }
}
