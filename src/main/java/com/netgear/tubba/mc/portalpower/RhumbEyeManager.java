package com.netgear.tubba.mc.portalpower;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.netgear.tubba.mc.portalpower.LocationEncoder.WorldName;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RhumbEyeManager {
  public static String RHUMB_EYE_NAME = "Eye of Rhumb";
    
  private NamespacedKey rhumbEyeOverworldRecipeKey;
  private NamespacedKey rhumbEyeNetherRecipeKey;
  private NamespacedKey rhumbEyeEndRecipeKey;
  
  private NamespacedKey rhumbEyeKey;
  
  public RhumbEyeManager(JavaPlugin plugin) {
    this.rhumbEyeOverworldRecipeKey = new NamespacedKey(plugin, "rhumb_eye_overworld_recipe");
    this.rhumbEyeNetherRecipeKey = new NamespacedKey(plugin, "rhumb_eye_nether_recipe");
    this.rhumbEyeEndRecipeKey = new NamespacedKey(plugin, "rhumb_eye_end_recipe");
    
    this.rhumbEyeKey = new NamespacedKey(plugin, "rhumb_eye_world");
  }
  
  public void registerRecipes() {   
    registerRecipe(rhumbEyeOverworldRecipeKey, Material.GRASS_BLOCK, createRhumbEye(WorldName.OVERWORLD));
    registerRecipe(rhumbEyeNetherRecipeKey, Material.NETHERRACK, createRhumbEye(WorldName.NETHER));
    registerRecipe(rhumbEyeEndRecipeKey, Material.END_STONE, createRhumbEye(WorldName.END));
  }
  
  /**
   * Returns true if this is a valid rhumb eye
   * @param item
   * @return
   */
  public boolean isRhumbEye(ItemStack item) {
    return getRhumbEyeWorld(item) != null;
  }
  
  /**
   * Returns the world to which the eye is attuned or null if not a valid eye or
   * not a valid world
   * @param item
   * @return
   */
  public WorldName getRhumbEyeWorld(ItemStack item) {
    if(item == null || item.getItemMeta() == null) {
      return null;
    }
    
    PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
    String attunedName = pdc.get(rhumbEyeKey, PersistentDataType.STRING);
    
    if(attunedName == null) {
      return null;
    }

    // Build the key and sure it is valid
    WorldName worldName = WorldName.lookup(attunedName);
    if(worldName == null) {
      throw new IllegalStateException("Failed to determine rhumb eye world from name='" + attunedName + "'");
    }
    
    return worldName;
  }
  
  /**
   * Create a new rhumb eye attuned to the identifed world
   * @param attunedWorld
   * @return
   */
  
  public ItemStack createRhumbEye(WorldName worldName) {
    ItemStack rhumbEye = new ItemStack(Material.ENDER_EYE);
    ItemMeta meta = rhumbEye.getItemMeta();
    
    if(meta == null) {
      throw new IllegalStateException("No item meta available");
    }
    
    String displayName = null;
    switch(worldName) {
    case END :
      displayName = "Eye of Rhumb's End";
      break;
    case NETHER :
      displayName = "Nether Eye of Rhumb";
      break;
    case OVERWORLD :
      displayName = "Overworld Eye of Rhumb";
      break;
    }
    if(displayName == null) {
      Bukkit.getServer().getLogger().warning("Unknown world type when binding ender's eye: " + worldName);
      displayName = "Eye of Rhumb";
    }
    
    meta.displayName(Component.text(displayName, worldName.getDefaultColor()));
//    meta.lore(List.of(Component.text("Eye that can focus and see the true location within the " + worldName, NamedTextColor.GOLD)));
    meta.setEnchantmentGlintOverride(true);
    meta.getPersistentDataContainer().set(rhumbEyeKey, PersistentDataType.STRING, worldName.getNamespacedKey().asMinimalString());
    rhumbEye.setItemMeta(meta);
    
    return rhumbEye;
  }
  
  private void registerRecipe(NamespacedKey key, Material centralMat, ItemStack item) {
    ShapedRecipe recipe = new ShapedRecipe(key, item);
    
    recipe.shape("EEE", "EXE", "EEE");
    recipe.setIngredient('E', Material.ENDER_EYE);
    recipe.setIngredient('X', centralMat);
    
    Bukkit.getServer().addRecipe(recipe);
  }
}
