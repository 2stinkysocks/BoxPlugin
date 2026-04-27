package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class RainbowHelmet extends CustomItem {
    float hue = 0f;
    NamespacedKey key = new NamespacedKey(BoxPlugin.instance, "RAINBOW_HELM");
    NamespacedKey itemIDkey = new NamespacedKey(BoxPlugin.instance, "ITEM_ID");

    public RainbowHelmet(CustomItemsMain plugin) {
        super(
                "§cRainbow Helmet",
                "RAINBOW_HELM",
                Material.LEATHER_HELMET,
                plugin,
                true,
                "§7§oIts not gay",
                "§c§lSPECIAL HELMET"
        );

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world : Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity fella = (LivingEntity) entity;
                        ItemStack helm = fella.getEquipment().getHelmet();
                        if (helm != null && helm.getItemMeta() != null && helm.getType() == Material.LEATHER_HELMET &&
                                helm.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                helm.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_HELM")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) helm.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            org.bukkit.Color bukkitColor = org.bukkit.Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            helm.setItemMeta(armortMeta);
                            fella.getEquipment().setHelmet(helm);
                        }
                    } else if (entity.getType() == EntityType.ARMOR_STAND) {

                        ArmorStand stand = (ArmorStand) entity;
                        ItemStack helm = stand.getEquipment().getHelmet();

                        if (helm != null && helm.getItemMeta() != null && helm.getType() == Material.LEATHER_HELMET &&
                                helm.getItemMeta().getPersistentDataContainer().has(itemIDkey, PersistentDataType.STRING) &&
                                helm.getItemMeta().getPersistentDataContainer().get(itemIDkey, PersistentDataType.STRING).equals("RAINBOW_HELM")) {

                            LeatherArmorMeta armortMeta = (LeatherArmorMeta) helm.getItemMeta();

                            java.awt.Color awt = java.awt.Color.getHSBColor(hue, 1f, 1f);
                            org.bukkit.Color bukkitColor = org.bukkit.Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

                            armortMeta.setColor(bukkitColor);
                            helm.setItemMeta(armortMeta);
                            stand.getEquipment().setHelmet(helm);
                        }
                    }
                }
            }
            hue += 0.02f; // speed
            if (hue >= 1f) hue = 0f;
        }, 2L, 2L);

    }
}
