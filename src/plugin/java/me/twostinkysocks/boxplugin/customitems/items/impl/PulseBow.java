package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

public class PulseBow extends CustomItem {

    public PulseBow(CustomItemsMain plugin) {
        super(
                "§3Pulse Bow",
                "PULSE_BOW",
                Material.BOW,
                plugin,
                false,
                ""
        );
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world : Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if(entity.getType() == EntityType.ARROW && entity.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "PULSE_ARROW"), PersistentDataType.INTEGER)) {
                        if(((Arrow) entity).isInBlock()) {
                            entity.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "PULSE_ARROW"));
                        }
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, entity.getLocation(), 10, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }
        }, 1L, 1L);
    }
}
