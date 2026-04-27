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

public class RainbowChestplate extends CustomItem {
    float hue = 0.06f;
    NamespacedKey key = new NamespacedKey(BoxPlugin.instance, "RAINBOW_CHESTPLATE");
    NamespacedKey itemIDkey = new NamespacedKey(BoxPlugin.instance, "ITEM_ID");

    public RainbowChestplate(CustomItemsMain plugin) {
        super(
                "§cRainbow Chestplate",
                "RAINBOW_CHESTPLATE",
                Material.LEATHER_CHESTPLATE,
                plugin,
                true,
                "§7§oIts not gay",
                "§c§lSPECIAL CHESTPLATE"
        );

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world : Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity fella = (LivingEntity) entity;
                        ItemStack chestplate = fella.getEquipment().getChestplate();
                        if (chestplate != null && chestplate.getItemMeta() != null && chestplate.getType() == Material.LEATHER_CHESTPLATE &&
                                chestplate.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                chestplate.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_CHESTPLATE")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) chestplate.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            Color bukkitColor = Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            chestplate.setItemMeta(armortMeta);
                            fella.getEquipment().setChestplate(chestplate);
                        }
                    } else if (entity.getType() == EntityType.ARMOR_STAND) {

                        ArmorStand stand = (ArmorStand) entity;
                        ItemStack chestplate = stand.getEquipment().getHelmet();

                        if (chestplate != null && chestplate.getItemMeta() != null && chestplate.getType() == Material.LEATHER_CHESTPLATE &&
                                chestplate.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                chestplate.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_CHESTPLATE")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) chestplate.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            Color bukkitColor = Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            chestplate.setItemMeta(armortMeta);
                            stand.getEquipment().setChestplate(chestplate);
                        }
                    }
                }
            }
            hue += 0.02f; // speed
            if (hue >= 1f) hue = 0f;
        }, 2L, 2L);

    }
}
