package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
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
                false,
                ChatColor.GRAY + "Right click to reclaim your ghost items",
                ChatColor.RED + "Left click 3 times to permanently delete your ghost items"
        );
        leftClicks = new HashMap<>();

        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost_token_cooldown"), PersistentDataType.LONG) && p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ghost_token_cooldown"), PersistentDataType.LONG) > System.currentTimeMillis()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                    p.sendMessage(ChatColor.RED + "That's too fast!");
                    return;
                }
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
                    p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "ghost_token_cooldown"), PersistentDataType.LONG, System.currentTimeMillis()+1000);
                }
            } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                if(BoxPlugin.instance.getPerksManager().getSelectedPerks(p).size() > 1 || BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).size() > 0) {
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                    p.sendMessage(ChatColor.RED + "You can only have 1 equipped perk (and no equipped megaperks) while using ghost items!");
                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1f, 2f);
                    int numReclaimables = BoxPlugin.instance.getGhostTokenManager().getReclaimableCountFromPDC(p);
                    int items = (int) Arrays.stream(p.getInventory().getStorageContents()).filter(i -> i != null).count();
                    int openSlots = (4*9) - items;
                    if(numReclaimables > openSlots) {
                        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                        p.sendMessage(ChatColor.RED + "You don't have enough inventory space!");
                    } else {
                        BoxPlugin.instance.getGhostTokenManager().restoreReclaimables(p);
                        for(int i = 0; i < e.getPlayer().getInventory().getContents().length; i++) {
                            ItemStack item = e.getPlayer().getInventory().getContents()[i];
                            if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("GHOST_TOKEN")) {
                                e.getPlayer().getInventory().setItem(i, null);
                            }
                        }
                        leftClicks.remove(e.getPlayer().getUniqueId());
                    }
                }

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
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(itemMeta);
        return item;
    }
}
