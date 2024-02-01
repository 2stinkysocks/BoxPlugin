package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.UUID;

public class GhostToken extends CustomItem {

    private HashMap<UUID, Integer> leftClicks;

    public GhostToken(CustomItemsMain plugin) {
        super(
                "§6Ghost Token",
                "GHOST_TOKEN",
                Material.MUSIC_DISC_5,
                plugin,
                ""
        );
        leftClicks = new HashMap<>();

        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                if(leftClicks.containsKey(p.getUniqueId())) {
                    leftClicks.put(p.getUniqueId(), leftClicks.get(p.getUniqueId())+1);
                } else {
                    leftClicks.put(p.getUniqueId(), 1);
                }
                if(leftClicks.get(p.getUniqueId()) >= 3) {
                    removeItem(e);
                } else {
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1f, 0.75f);
                    p.sendMessage(ChatColor.RED + "Left Click " + ChatColor.BOLD + (3-leftClicks.get(p.getUniqueId())) + ChatColor.RED + " more times to permanently delete your ghost token.");
                }
            } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1f, 2f);
                BoxPlugin.instance.getGhostTokenManager().restoreReclaimables(p);
                for(int i = 0; i < e.getPlayer().getInventory().getContents().length; i++) {
                    ItemStack item = e.getPlayer().getInventory().getContents()[i];
                    if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("GHOST_TOKEN")) {
                        e.getPlayer().getInventory().setItem(i, null);
                    }
                }
                leftClicks.remove(e.getPlayer().getUniqueId());
            }
        });
    }

    private void removeItem(PlayerInteractEvent e) {
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 0.5f);
        BoxPlugin.instance.getGhostTokenManager().clearReclaimables(e.getPlayer());
        for(int i = 0; i < e.getPlayer().getInventory().getContents().length; i++) {
            ItemStack item = e.getPlayer().getInventory().getContents()[i];
            if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("GHOST_TOKEN")) {
                e.getPlayer().getInventory().setItem(i, null);
            }
        }
        leftClicks.remove(e.getPlayer().getUniqueId());
        e.getPlayer().sendMessage(ChatColor.RED + "Cleared your ghost token!");
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack item = super.getItemStack();
        ItemMeta itemMeta = item.getItemMeta(); // will never be null
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "SOULBOUND"), PersistentDataType.INTEGER, 1);
        itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(itemMeta);
        return item;
    }
}
