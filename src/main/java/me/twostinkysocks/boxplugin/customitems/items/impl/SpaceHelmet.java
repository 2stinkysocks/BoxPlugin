package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SpaceHelmet extends CustomItem {

    public HashMap<UUID, BukkitTask> tasks;

    private ArrayList<Material> colors;

    public SpaceHelmet(CustomItemsMain plugin) {
        super(
                "§cSpace Helmet",
                "SPACE_HELMET",
                Material.RED_STAINED_GLASS,
                plugin,
                "§7§oA rare space helmet forged",
                "§7§ofrom shards of moon glass",
                "",
                "§8Right click to equip",
                "",
                "§c§lSPECIAL HELMET"
        );
        tasks = new HashMap<>();
        ItemMeta meta = getItemStack().getItemMeta();
        meta.addEnchant(Enchantment.PROTECTION, 20, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        getItemStack().setItemMeta(meta);

        colors = new ArrayList<>();
        colors.add(Material.RED_STAINED_GLASS);
        colors.add(Material.ORANGE_STAINED_GLASS);
        colors.add(Material.YELLOW_STAINED_GLASS);
        colors.add(Material.GREEN_STAINED_GLASS);
        colors.add(Material.BLUE_STAINED_GLASS);
        colors.add(Material.PURPLE_STAINED_GLASS);

        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 2f);
                ItemStack helmet = e.getPlayer().getInventory().getHelmet();
                ItemStack hotbar = e.getItem();
                if(tasks.containsKey(p.getUniqueId())) {
                    tasks.get(p.getUniqueId()).cancel();
                    tasks.remove(p.getUniqueId());
                }
                e.getPlayer().getInventory().setHelmet(hotbar);
                e.getPlayer().getInventory().setItem(e.getHand(), helmet);
                BukkitTask old = tasks.put(p.getUniqueId(), Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
                    if(p.getInventory().getHelmet() != null && p.getInventory().getHelmet().hasItemMeta() && p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals(getItemId())) {
                        int index = colors.indexOf(p.getInventory().getHelmet().getType());
                        if(index >= colors.size()-1) index = -1;
                        p.getInventory().getHelmet().setType(colors.get(index+1));
                    } else {
                        tasks.get(p.getUniqueId()).cancel();
                        tasks.remove(p.getUniqueId());
                    }
                }, 5L, 5L));
                if(old != null) old.cancel();
            }
            if(a == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
            }
        });

        setLeave(p -> {
            if(tasks.containsKey(p.getUniqueId())) {
                tasks.get(p.getUniqueId()).cancel();
                tasks.remove(p.getUniqueId());
            }
        });

        setJoin(p -> {
            if(p.getInventory().getHelmet() != null && p.getInventory().getHelmet().hasItemMeta() && p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals(getItemId())) {
                if(tasks.containsKey(p.getUniqueId())) {
                    tasks.get(p.getUniqueId()).cancel();
                    tasks.remove(p.getUniqueId());
                }
                BukkitTask old = tasks.put(p.getUniqueId(), Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
                    if(p.getInventory().getHelmet() != null && p.getInventory().getHelmet().hasItemMeta() && p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals(getItemId())) {
                        int index = colors.indexOf(p.getInventory().getHelmet().getType());
                        if(index >= colors.size()-1) index = -1;
                        p.getInventory().getHelmet().setType(colors.get(index+1));
                    } else {
                        tasks.get(p.getUniqueId()).cancel();
                        tasks.remove(p.getUniqueId());
                    }
                }, 5L, 5L));
                if(old != null) old.cancel();
            }
        });
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack orig = super.getItemStack();
        ItemMeta meta = orig.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "UNIQUE_ID"), PersistentDataType.STRING, UUID.randomUUID().toString());
        orig.setItemMeta(meta);
        return orig;
    }
}
