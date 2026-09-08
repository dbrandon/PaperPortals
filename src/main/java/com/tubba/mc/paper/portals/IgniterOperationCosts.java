package com.tubba.mc.paper.portals;

import org.bukkit.configuration.file.FileConfiguration;

public class IgniterOperationCosts {
  private int bindToEye;
  private int bindToDye;
  private int bindToDiamond;
  private int bindToNetherite;
  private int bindToRedstone;
  
  private static String bindOp(String name) {
    return "igniter-operation-costs." + name;
  }
  
  public static IgniterOperationCosts load(FileConfiguration config) {
    IgniterOperationCosts costs = new IgniterOperationCosts();
    
    costs.bindToDiamond = config.getInt(bindOp("bind-to-diamond"), 5);
    costs.bindToDye = config.getInt(bindOp("bind-to-dye"), 2);
    costs.bindToEye = config.getInt(bindOp("bind-to-eye"), 2);
    costs.bindToNetherite = config.getInt(bindOp("bind-to-netherite"), 15);
    costs.bindToRedstone = config.getInt(bindOp("bind-to-redstone"), 10);
    
    return costs;
  }
  
  private IgniterOperationCosts() {
    
  }
  
  public int getBindToDiamond() {
    return bindToDiamond;
  }
  
  public int getBindToDye() {
    return bindToDye;
  }
  
  public int getBindToEye() {
    return bindToEye;
  }
  
  public int getBindToNetherite() {
    return bindToNetherite;
  }
  
  public int getBindToRedstone() {
    return bindToRedstone;
  }
}
