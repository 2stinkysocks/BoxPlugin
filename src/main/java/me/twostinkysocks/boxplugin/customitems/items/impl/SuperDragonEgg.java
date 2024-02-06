package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.util.MythicMobsIntegration;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.persistence.PersistentDataType;

public class SuperDragonEgg extends CustomItem {

    public SuperDragonEgg(CustomItemsMain plugin) {
        super(
                ChatColor.DARK_PURPLE + "Super Dragon Egg",
                "SUPER_DRAGON_EGG",
                Material.DRAGON_EGG,
                plugin,
                ChatColor.LIGHT_PURPLE + "Place on the altar in the end to summon the dragon!"
        );
        setClick((e, a) -> {
            if(a == Action.RIGHT_CLICK_BLOCK) {
                if(e.getClickedBlock().getLocation().getWorld().getEnvironment() == World.Environment.THE_END && e.getClickedBlock().getLocation().getBlockX() == 0 && e.getClickedBlock().getLocation().getBlockY() == 111 && e.getClickedBlock().getLocation().getBlockZ() == 0 && e.getBlockFace() == BlockFace.UP) {
                    if(e.getPlayer().getWorld().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "dragon_alive"), PersistentDataType.INTEGER) && e.getPlayer().getWorld().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "dragon_alive"), PersistentDataType.INTEGER) == 1) {
                        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                        e.getPlayer().sendMessage(ChatColor.RED + "A dragon is still alive!");
                        return;
                    }
                    e.getPlayer().getWorld().getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "dragon_alive"), PersistentDataType.INTEGER, 1);
                    e.getPlayer().getInventory().getItem(e.getHand()).setAmount(e.getPlayer().getInventory().getItem(e.getHand()).getAmount()-1);
                    MythicMobsIntegration.spawnWithData("dragonSpawn", 0, new Location(e.getPlayer().getWorld(), 0.5, 109, 0.5), true);
                    Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                        MythicMobsIntegration.spawnWithData("SuperDragon", 6000, new Location(e.getPlayer().getWorld(), 0, 141, 0), true);
                    }, 160);
                }
            }
            e.setCancelled(true);
        });
    }
}
