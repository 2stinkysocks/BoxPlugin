package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

public class RainbowBoots extends CustomItem {
    float hue = 0.18f;
    NamespacedKey key = new NamespacedKey(BoxPlugin.instance, "RAINBOW_BOOTS");
    NamespacedKey itemIDkey = new NamespacedKey(BoxPlugin.instance, "ITEM_ID");

    public RainbowBoots(CustomItemsMain plugin) {
        super(
                "§cRainbow Boots",
                "RAINBOW_BOOTS",
                Material.LEATHER_BOOTS,
                plugin,
                true,
                "§7§oIts not gay",
                "§c§lSPECIAL BOOTS"
        );

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world : Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity fella = (LivingEntity) entity;
                        ItemStack boots = fella.getEquipment().getBoots();
                        if (boots != null && boots.getItemMeta() != null && boots.getType() == Material.LEATHER_BOOTS &&
                                boots.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                boots.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_BOOTS")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) boots.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            Color bukkitColor = Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            boots.setItemMeta(armortMeta);
                            fella.getEquipment().setBoots(boots);
                        }
                    } else if (entity.getType() == EntityType.ARMOR_STAND) {

                        ArmorStand stand = (ArmorStand) entity;
                        ItemStack boots = stand.getEquipment().getBoots();

                        if (boots != null && boots.getItemMeta() != null && boots.getType() == Material.LEATHER_BOOTS &&
                                boots.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                boots.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_BOOTS")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) boots.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            Color bukkitColor = Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            boots.setItemMeta(armortMeta);
                            stand.getEquipment().setBoots(boots);
                        }
                    }
                }
            }
            hue += 0.02f; // speed
            if (hue >= 1f) hue = 0f;
        }, 2L, 2L);

    }
}
