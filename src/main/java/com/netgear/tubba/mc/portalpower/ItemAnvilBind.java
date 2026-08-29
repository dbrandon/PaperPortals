package com.netgear.tubba.mc.portalpower;

import java.util.function.Consumer;

import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

public class ItemAnvilBind {
  private ItemStack output;
  private int levelCost;
  private Consumer<AnvilInventory> anvilApply;
  
  public ItemAnvilBind(ItemStack output, int levelCost, Consumer<AnvilInventory> anvilApply) {
    this.output = output;
    this.levelCost = levelCost;
    this.anvilApply = anvilApply;
  }
  
  public int getLevelCost() {
    return levelCost;
  }
  
  public ItemStack getOutput() {
    return output;
  }
  
  public void handleAnvilPrepare(AnvilView view) {
    view.setRepairCost(levelCost);
//    inventory.setRepairCost(levelCost);
  }
  
  public void handleAnvilApply(AnvilInventory inventory) {
    if(anvilApply != null) {
      anvilApply.accept(inventory);
    }
  }
}
