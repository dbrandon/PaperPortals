package com.netgear.tubba.mc.portalpower.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.netgear.tubba.mc.portalpower.data.MinecraftLocation;


public class ConvertUtil {
  public static Location convert(MinecraftLocation location) {
    return new Location(
        Bukkit.getWorld(location.getWorld()),
        location.getX(),
        location.getY(),
        location.getZ());
  }
  
  public static MinecraftLocation convert(Location location) {
    return MinecraftLocation.newBuilder()
        .setAzimuth(0)
        .setWorld(location.getWorld().getName())
        .setX(location.getBlockX())
        .setY(location.getBlockY())
        .setZ(location.getBlockZ())
        .build();
  }
}
