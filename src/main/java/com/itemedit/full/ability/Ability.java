package com.itemedit.full.ability;

import com.itemedit.full.ItemEditFull;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class Ability {
    private final String id;
    private final String name;
    private final String description;

    public Ability(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract boolean trigger(Player player, ItemStack item);

    // Helpers to retrieve parameters with per-item customization support

    public double getDoubleParam(ItemEditFull plugin, ItemStack item, String param, double defaultValue) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            
            // 1. Check PDC custom override
            NamespacedKey key = new NamespacedKey(plugin, "ability_param_" + id + "_" + param);
            if (pdc.has(key, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
                return pdc.get(key, org.bukkit.persistence.PersistentDataType.DOUBLE);
            }
            
            // 2. Check weapon.yml configuration
            NamespacedKey weaponKeyPdc = new NamespacedKey(plugin, "weapon_key");
            if (pdc.has(weaponKeyPdc, org.bukkit.persistence.PersistentDataType.STRING)) {
                String weaponKey = pdc.get(weaponKeyPdc, org.bukkit.persistence.PersistentDataType.STRING);
                if (weaponKey != null) {
                    org.bukkit.configuration.file.FileConfiguration weaponCfg = plugin.getWeaponConfigManager().getConfig();
                    String path = "weapons." + weaponKey + ".settings." + id + "." + param;
                    if (weaponCfg.contains(path)) {
                        return weaponCfg.getDouble(path);
                    }
                }
            }
        }
        // 3. Fallback to standard config.yml
        return plugin.getConfig().getDouble("abilities." + id + "." + param, defaultValue);
    }

    public int getIntParam(ItemEditFull plugin, ItemStack item, String param, int defaultValue) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            
            // 1. Check PDC custom override
            NamespacedKey key = new NamespacedKey(plugin, "ability_param_" + id + "_" + param);
            if (pdc.has(key, org.bukkit.persistence.PersistentDataType.INTEGER)) {
                return pdc.get(key, org.bukkit.persistence.PersistentDataType.INTEGER);
            }
            
            // 2. Check weapon.yml configuration
            NamespacedKey weaponKeyPdc = new NamespacedKey(plugin, "weapon_key");
            if (pdc.has(weaponKeyPdc, org.bukkit.persistence.PersistentDataType.STRING)) {
                String weaponKey = pdc.get(weaponKeyPdc, org.bukkit.persistence.PersistentDataType.STRING);
                if (weaponKey != null) {
                    org.bukkit.configuration.file.FileConfiguration weaponCfg = plugin.getWeaponConfigManager().getConfig();
                    String path = "weapons." + weaponKey + ".settings." + id + "." + param;
                    if (weaponCfg.contains(path)) {
                        return weaponCfg.getInt(path);
                    }
                }
            }
        }
        // 3. Fallback to standard config.yml
        return plugin.getConfig().getInt("abilities." + id + "." + param, defaultValue);
    }

    public void setCustomParam(ItemEditFull plugin, ItemStack item, String param, double value) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "ability_param_" + id + "_" + param);
        pdc.set(key, PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
    }

    public void setCustomParam(ItemEditFull plugin, ItemStack item, String param, int value) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "ability_param_" + id + "_" + param);
        pdc.set(key, PersistentDataType.INTEGER, value);
        item.setItemMeta(meta);
    }

    public void removeCustomParam(ItemEditFull plugin, ItemStack item, String param) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "ability_param_" + id + "_" + param);
        pdc.remove(key);
        item.setItemMeta(meta);
    }
}
