package com.netgear.tubba.mc.portalpower.util;

import java.util.function.Consumer;

import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class StackConsumer {
  /**
   * Creates a simple anvil item consumer that decrements the remaining count
   * in the stack by 1 upon crafting.
   * @param plugin
   * @param item
   * @return
   */
  public static Consumer<AnvilInventory> create(JavaPlugin plugin, ItemStack item) {
    return inventory -> {
      if(item == null) {
        return;
      }
      ItemStack remaining = item.clone();
      remaining.setAmount(remaining.getAmount() - 1);
      plugin.getServer().getScheduler().runTask(plugin, () -> inventory.setItem(1, remaining));
    };
  }
}
