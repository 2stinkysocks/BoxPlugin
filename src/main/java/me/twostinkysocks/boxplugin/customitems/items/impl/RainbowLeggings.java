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

public class RainbowLeggings extends CustomItem {
    float hue = 0.12f;
    NamespacedKey key = new NamespacedKey(BoxPlugin.instance, "RAINBOW_LEGGINGS");
    NamespacedKey itemIDkey = new NamespacedKey(BoxPlugin.instance, "ITEM_ID");

    public RainbowLeggings(CustomItemsMain plugin) {
        super(
                "§cRainbow Leggings",
                "RAINBOW_LEGGINGS",
                Material.LEATHER_LEGGINGS,
                plugin,
                true,
                "§7§oIts not gay",
                "§c§lSPECIAL LEGGINGS"
        );

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world : Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity fella = (LivingEntity) entity;
                        ItemStack leggings = fella.getEquipment().getLeggings();
                        if (leggings != null && leggings.getItemMeta() != null && leggings.getType() == Material.LEATHER_LEGGINGS &&
                                leggings.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                leggings.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_LEGGINGS")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) leggings.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            Color bukkitColor = Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            leggings.setItemMeta(armortMeta);
                            fella.getEquipment().setLeggings(leggings);
                        }
                    } else if (entity.getType() == EntityType.ARMOR_STAND) {

                        ArmorStand stand = (ArmorStand) entity;
                        ItemStack leggings = stand.getEquipment().getLeggings();

                        if (leggings != null && leggings.getItemMeta() != null && leggings.getType() == Material.LEATHER_LEGGINGS &&
                                leggings.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                leggings.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_LEGGINGS")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) leggings.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            Color bukkitColor = Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            leggings.setItemMeta(armortMeta);
                            stand.getEquipment().setLeggings(leggings);
                        }
                    }
                }
            }
            hue += 0.02f; // speed
            if (hue >= 1f) hue = 0f;
        }, 2L, 2L);

    }
}
