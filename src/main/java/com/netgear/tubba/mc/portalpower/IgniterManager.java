package com.netgear.tubba.mc.portalpower;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.java.JavaPlugin;

import com.netgear.tubba.mc.portalpower.LocationEncoder.WorldName;
import com.netgear.tubba.mc.portalpower.data.IgniterData;
import com.netgear.tubba.mc.portalpower.data.MinecraftLocation;
import com.netgear.tubba.mc.portalpower.data.PortalColor;
import com.netgear.tubba.mc.portalpower.util.ConvertUtil;
import com.netgear.tubba.mc.portalpower.util.ProtoPersistentDataType;
import com.netgear.tubba.mc.portalpower.util.StackConsumer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class IgniterManager {
  private final static int BIND_WORLD_COST = 1;
  
  private JavaPlugin plugin;
  
  private NamespacedKey gunterKey;
  
  private RhumbEyeManager rhumbEyeManager;
  
  private ProtoPersistentDataType<IgniterData> igniterDataHandler = new ProtoPersistentDataType<IgniterData>(IgniterData.class, IgniterData.parser());
  
  public IgniterManager(JavaPlugin plugin, RhumbEyeManager rhumbEyeManager) {
    this.plugin = plugin;
    this.rhumbEyeManager = rhumbEyeManager;
    this.gunterKey = new NamespacedKey(plugin, "gunter_item");
  }

  /**
   * Basic test of an item to see if it already is an igniter or is the right material to become one
   * @param item
   * @return
   */
  public boolean canBeIgniter(ItemStack item) {
    return item != null && item.getItemMeta() != null && item.getType() == Material.FLINT_AND_STEEL;
  }
  
  /**
   * Tests to see if this item is a blessed item that is an igniter.  It may be bound to a world type
   * or bound to a location.
   * @param item
   * @return
   */
  public boolean isIgniter(ItemStack item) {
    return canBeIgniter(item) && igniterDataHandler.has(item, gunterKey);
  }
  
  /**
   * Retrieves the data describing the state of this igniter.
   * @param item
   * @return
   */
  public IgniterData getIgniterData(ItemStack item) {
    if(!isIgniter(item)) {
      return null;
    }
    
    return igniterDataHandler.decode(item, gunterKey);
  }
  
  /**
   * Applies the igniter data to the item
   * @param item
   * @param data
   * @param displayName
   */
  private void setIgniterData(ItemStack item, IgniterData data, Component displayName) {
    if(!canBeIgniter(item)) {
      Bukkit.getServer().getLogger().warning("Cannot set igniter data on non-igniter item!");
      return;
    }
    
    ItemMeta meta = item.getItemMeta();
    meta.lore(buildLoreList(data));
    if(displayName != null) {
      meta.displayName(displayName);
    }
    meta.setEnchantmentGlintOverride(true);
    meta.getPersistentDataContainer().set(gunterKey, igniterDataHandler, data);
    item.setItemMeta(meta);
  }
  
  public ItemAnvilBind bindToIgniter(AnvilView view) {
    ItemStack item = view.getItem(1);
    ItemStack igniter = view.getItem(0);
    ItemAnvilBind bind = null;
        
    if((bind = bindToWorld(item, igniter, view)) != null) {
      return bind;
    }
    
    if((bind = bindToBonus(item, igniter, view)) != null) {
      return bind;
    }
    
    if((bind = bindDye(item, igniter)) != null) {
      return bind;
    }
    
    if((bind = bindToRedstone(item, igniter, view)) != null) {
      return bind;
    }
    
    return null;
  }
  
  private ItemAnvilBind bindDye(ItemStack dye, ItemStack igniter) {
//    PortalColor color = getPortalColor(dye);
//    if(color == null) {
//      return null;
//    }
//    
    // Make sure the igniter item is valid and has only been world attuned but not location attuned
    IgniterData data = getIgniterData(igniter);
    if(data == null || !data.hasAttunedWorld()) {
      return null;
    }
    
    PortalColor color = addPortalColor(dye, data.hasAttunedColor() ? data.getAttunedColor() : null);
    if(color == null) {
      return null;
    }

//    if(data.hasAttunedColor() && data.getAttunedColor().hasRgbHistoryMask())
//      color = checkForPrismatic(color, dye.getType(), data.getAttunedColor().getRgbHistoryMask());
    
    igniter = igniter.clone();
    data = IgniterData.newBuilder(data)
        .setAttunedColor(color)
        .build();
    setIgniterData(igniter, data, null);
    
    return new ItemAnvilBind(igniter, BIND_WORLD_COST, StackConsumer.create(plugin, igniter));
  }
  
//  private PortalColor checkForPrismatic(PortalColor color, Material dye, int mask) {
//    return PortalColor.newBuilder(color)
//        .setDescription("prismatic")
//        .setPrismatic(mask == 0x07)
//        .setRgbHistoryMask(mask)
//        .build();
//  }
//    
  /**
   * Creates the igniter tool binding it to one of the minecraft worlds
   * @param item
   * @param igniter
   * @return
   */
  private ItemAnvilBind bindToWorld(ItemStack item, ItemStack igniter, AnvilView view) {
    WorldName eyeWorld = rhumbEyeManager.getRhumbEyeWorld(item);
       
    if(eyeWorld == null || !canBeIgniter(igniter) || igniterDataHandler.has(igniter, gunterKey)) {
      return null;
    }
    
    igniter = createIgniter(igniter.clone(), eyeWorld, view.getRenameText());
    
    return new ItemAnvilBind(igniter, BIND_WORLD_COST, StackConsumer.create(plugin, item));
  }
  
  private ItemAnvilBind bindToBonus(ItemStack item, ItemStack igniter, AnvilView view) {
    IgniterData data = getIgniterData(igniter);
    
    if(item == null || data == null || !data.hasAttunedWorld() || data.hasAttunedLocation()) {
      return null;
    }
    
    int mask = 0;
    if(item.getType() == Material.DIAMOND) {
      mask = IgniterData.BonusMaskBits.DIAMOND_VALUE;
    }
    else if(item.getType() == Material.NETHERITE_INGOT) {
      mask = IgniterData.BonusMaskBits.NETHERITE_VALUE;
    }
    else {
      return null;
    }
    
    int curMask = data.hasBonusMask() ? data.getBonusMask() : 0;
    if((mask & curMask) == IgniterData.BonusMaskBits.DIAMOND_VALUE || (mask & curMask) == IgniterData.BonusMaskBits.NETHERITE_VALUE) {
      return null;
    }
    if((mask & IgniterData.BonusMaskBits.DIAMOND_VALUE) != 0 && (curMask & IgniterData.BonusMaskBits.NETHERITE_VALUE) != 0) {
      return null;
    }
    
    mask = mask | curMask;
    int remaining = 1;
    if((mask & IgniterData.BonusMaskBits.NETHERITE_VALUE) != 0) {
      remaining = 50;
    }
    else if((mask & IgniterData.BonusMaskBits.DIAMOND_VALUE) != 0) {
      remaining = 10;
    }

    igniter = igniter.clone();
    TextComponent component = null;
    data = IgniterData.newBuilder(data)
        .setBonusMask(curMask | mask)
        .setUsesRemaining(remaining)
        .build();
    
    if(view.getRenameText() != null && !view.getRenameText().equals("")) {
      WorldName worldName = WorldName.lookup(data.getAttunedWorld());
      component = Component.text(view.getRenameText(), worldName.getDefaultColor());
    }
    
    setIgniterData(igniter, data, component);
    return new ItemAnvilBind(igniter, BIND_WORLD_COST, StackConsumer.create(plugin, item));    
  }
  
  private ItemAnvilBind bindToRedstone(ItemStack item, ItemStack igniter, AnvilView view) {
    IgniterData data = getIgniterData(igniter);
    
    if(item == null || data == null || !data.hasAttunedWorld() || data.hasAttunedLocation()) {
      return null;
    }
    if(item.getType() != Material.REDSTONE_BLOCK) {
      return null;
    }
    
    if(data.hasRedstoneActivated() || data.getRedstoneActivated()) {
      return null;
    }
    
    igniter = igniter.clone();
    data = IgniterData.newBuilder(data).setRedstoneActivated(true).build();
    TextComponent component = null;
    if(view.getRenameText() != null && !view.getRenameText().equals("")) {
      WorldName worldName = WorldName.lookup(data.getAttunedWorld());
      component = Component.text(view.getRenameText(), worldName.getDefaultColor());
    }
    
    setIgniterData(igniter, data, component);
    return new ItemAnvilBind(igniter, BIND_WORLD_COST, StackConsumer.create(plugin, item));
  }
  
  public ItemStack createIgniter(WorldName worldName) {
    return createIgniter(new ItemStack(Material.FLINT_AND_STEEL), worldName, null);
  }
  
  private ItemStack createIgniter(ItemStack igniter, WorldName worldName, String customName) {
    NamedTextColor color = worldName.getDefaultColor();
    IgniterData data = IgniterData.newBuilder()
        .setAttunedWorld(worldName.getNamespacedKey().asMinimalString())
        .setAttunedColor(addPortalColor(color, null))
        .setUsesRemaining(1)
        .build();
    
    String name = worldName.getFriendlyName() + " Gunter's Igniter";
    if(customName != null && !customName.equals("")) {
      name = customName;
    }
    Bukkit.getServer().getLogger().warning("create igniter, name=" + name + "; customname=[" + customName + "]");
    setIgniterData(igniter, data, Component.text(name, color));
    
    return igniter;
  }
  
  private List<? extends Component> buildLoreList(IgniterData data) {
    List<TextComponent> list = new ArrayList<>();
    
    if(data.hasAttunedWorld()) {
//      list.add(Component.text("Enchanted igniter that can be attuned to a unique location within " + data.getAttunedWorld(), NamedTextColor.GRAY));
      list.add(Component.text("Unbound", NamedTextColor.RED));
    }
    else if(data.hasAttunedLocation()) {
      Location location = ConvertUtil.convert(data.getAttunedLocation());
//      list.add(Component.text("Enchanted igniter that contains an attuned location", NamedTextColor.GRAY));
      list.add(Component.text("Igniter is attuned to ", NamedTextColor.GRAY)
              .append(Component.text("[" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]", NamedTextColor.RED))
              );
    }
    
    if(data.hasAttunedColor() && data.getAttunedColor().hasDescription()) {
      list.add(Component.text("This igniter appears to have a faint " + data.getAttunedColor().getDescription() + " color"));
    }
    
    if(data.hasUsesRemaining()) {
      int remaining = data.getUsesRemaining();
      list.add(Component.text("" + remaining + (remaining > 1 ? " ignites" : " ignite") + " remaining"));
    }
    
    if(data.hasRedstoneActivated() && data.getRedstoneActivated()) {
      list.add(Component.text("Redstone Powered", NamedTextColor.RED));
    }
    
    return list;
  }
  
  public boolean bindToBlockLocation(ItemStack igniter, Block block, Player player) {
    if(block.getType() != Material.CARTOGRAPHY_TABLE) {
      return false;
    }
    
    IgniterData data = getIgniterData(igniter);
    
    if(data == null || !data.hasAttunedWorld()) {
      if(data.getAttunedLocation() != null) {
        player.sendMessage(Component.text("Gunter has already been attuned and cannot be changed.", NamedTextColor.RED));
      }
      return false;
    }
    
    NamespacedKey worldKey = NamespacedKey.minecraft(data.getAttunedWorld());
    WorldName worldName = WorldName.lookup(worldKey);
    World attunedWorld = Bukkit.getWorld(worldKey);
    if(worldName == null || attunedWorld == null) {
      Bukkit.getServer().getLogger().warning("Igniter has an unknown attuned world '" + data.getAttunedWorld() + "'");
      player.sendMessage(Component.text("Gunter is defective and must be destroyed.", NamedTextColor.RED));
      return false;
    }
    
    Location location = block.getLocation();
    if(location.getWorld() != attunedWorld) {
      player.sendMessage(Component.text("Gunter is not attuned to this world!", NamedTextColor.RED));
      return false;
    }
    
    data = IgniterData.newBuilder(data)
        .clearAttunedWorld()
        .setAttunedLocation(ConvertUtil.convert(location))
        .build();
    
    NamedTextColor color = worldName.getDefaultColor();
    String name = worldName.getFriendlyName() + "-attuned Gunter's Igniter";
    Component nameComponent = igniter.getItemMeta().displayName();
    if(nameComponent != null) {
      String plain = PlainTextComponentSerializer.plainText().serialize(nameComponent);
      if(plain.contains(worldName.getFriendlyName())) {
        plain.replace(worldName.getFriendlyName(), worldName.getFriendlyName() + "-attuned");
      }
      else {
        name = worldName.getFriendlyName() + "-attuned " + plain;
      }
    }
    
    setIgniterData(igniter, data, Component.text(name, color));
    
    return true;
  }
  
  private PortalColor addPortalColor(ItemStack dye, PortalColor base) {
    if(dye == null) {
      return null;
    }
    
    switch(dye.getType()) {
    case BLACK_DYE : return addPortalColor(Color.black, "black", base);
    case BLUE_DYE : return addPortalColor(Color.blue, "blue", base);
    case BROWN_DYE : return addPortalColor(new Color(150, 75, 0), "brown", base);
    case CYAN_DYE : return addPortalColor(Color.cyan, "cyan", base);
    case GRAY_DYE : return addPortalColor(Color.gray, "gray", base);
    case GREEN_DYE : return addPortalColor(Color.green, "green", base);
    case LIGHT_BLUE_DYE : return addPortalColor(new Color(173, 216, 230), "light blue", base);
    case LIGHT_GRAY_DYE : return addPortalColor(Color.lightGray, "light gray", base);
    case LIME_DYE : return addPortalColor(new Color(0x32, 0xCD, 0x32), "lime", base);
    case MAGENTA_DYE : return addPortalColor(Color.magenta, "magenta", base);
    case ORANGE_DYE : return addPortalColor(Color.orange, "orange", base);
    case PINK_DYE : return addPortalColor(Color.pink, "pink", base);
    case PURPLE_DYE : return addPortalColor(new Color(0xB2, 0x00, 0xED), "purple", base);
    case RED_DYE : return addPortalColor(Color.red, "red", base);
    case WHITE_DYE : return addPortalColor(Color.white, "white", base);
    case YELLOW_DYE : return addPortalColor(Color.yellow, "yellow", base);
    }
    
    return null;
  }
  
  private PortalColor addPortalColor(NamedTextColor color, PortalColor base) {
    return addPortalColor(new Color(color.value()), color.name().toLowerCase().replace('_', ' '), base);
  }
  
  private PortalColor addPortalColor(Color color, String description, PortalColor base) {
    PortalColor.Builder builder = base == null ? PortalColor.newBuilder() : PortalColor.newBuilder(base);
    
    builder.addRgba(color.getRGB());
    builder.setDescription(builder.getRgbaCount() > 1 ? "multi-hued" : description);
    return builder.build();
  }
  
  public boolean itemUsed(ItemStack igniter) {
    IgniterData data = getIgniterData(igniter);
    
    if(data == null) {
      return false;
    }
    
    if(!data.hasUsesRemaining()) {
      return true;
    }
    
    if(data.getUsesRemaining() <= 1) {
      igniter.setAmount(0);
    }
    
    data = IgniterData.newBuilder(data)
        .setUsesRemaining(data.getUsesRemaining() - 1)
        .build();
    setIgniterData(igniter, data, null);
    
    return true;
  }
}
