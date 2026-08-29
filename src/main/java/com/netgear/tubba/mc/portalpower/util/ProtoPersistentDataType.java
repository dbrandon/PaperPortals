package com.netgear.tubba.mc.portalpower.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

/**
 * Glue class to make the protocol buffer data types easy to encode/decode with
 * paper persistent data containers.
 * @param <T>
 */
public class ProtoPersistentDataType<T extends MessageLite> implements PersistentDataType<byte[], T> {
  private Class<T> messageClass;
  private Parser<T> parser;
  
  public ProtoPersistentDataType(Class<T> messageClass, Parser<T> parser) {
    this.messageClass = messageClass;
    this.parser = parser;
  }
  
  @Override
  public @NotNull Class<byte[]> getPrimitiveType() {
    return byte[].class;
  }
  
  @Override
  public @NotNull Class<T> getComplexType() {
    return this.messageClass;
  }
  
  @Override
  public byte @NotNull [] toPrimitive(@NotNull T complex, @NotNull PersistentDataAdapterContext context) {
    return complex.toByteArray();
  }
  
  @Override
  public @NotNull T fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
    try {
      return this.parser.parseFrom(primitive);
    }
    catch(InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Failed to parse protocol buffer data from PDC byte array", e);
    }
  }
  
  public T decode(ItemStack item, NamespacedKey key) {
    if(!has(item, key)) {
      return null;
    }
    
    return item.getItemMeta().getPersistentDataContainer().get(key, this);
  }

  public boolean has(ItemStack item, NamespacedKey key) {
    if(item == null || item.getItemMeta() == null) {
      return false;
    }
    
    PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
    return pdc.has(key, this);
  }
}
