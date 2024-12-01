package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PerkObsidian extends AbstractPerk {
    public PerkObsidian() {
        ItemStack guiItem = new ItemStack(Material.OBSIDIAN);
        ItemMeta meta = guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "Obsidian");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain 64 obsidian on respawn"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(2);

        setKey("perk_obsidian");
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        removeOldObsidianFromInventory(p);
        addObsidianToInventory(p);
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {
        for(ItemStack item : new ArrayList<>(e.getDrops())) {
            if(item.getType() == Material.OBSIDIAN && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                e.getDrops().remove(item);
            }
        }
    }

    @Override
    public void onEquip(Player p) {
        removeOldObsidianFromInventory(p);
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "obsidian_perk_item_count"), PersistentDataType.INTEGER)) {
            addObsidianToInventory(p, p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "obsidian_perk_item_count"), PersistentDataType.INTEGER));
        } else {
            addObsidianToInventory(p);
        }
    }

    @Override
    public void onUnequip(Player p) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "obsidian_perk_item_count"), PersistentDataType.INTEGER, getObsidianCountInInventory(p));
        removeOldObsidianFromInventory(p);
    }

    private void removeOldObsidianFromInventory(Player p) {
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.OBSIDIAN && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                p.getInventory().remove(item);
            }
        }
        ItemStack item = p.getInventory().getItemInOffHand();
        if(item != null && item.getType() == Material.OBSIDIAN && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
            p.getInventory().setItemInOffHand(null);
        }
    }

    private int getObsidianCountInInventory(Player p) {
        int count = 0;
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.OBSIDIAN && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                count += item.getAmount();
            }
        }
//        ItemStack item = p.getInventory().getItemInOffHand();
//        if(item != null && item.getType() == Material.OBSIDIAN && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
//            count += item.getAmount();
//        }
        return count;
    }

    private void addObsidianToInventory(Player p, int amount) {
        ItemStack stack = new ItemStack(Material.OBSIDIAN, amount);
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Perk item"
        ));
        meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
        p.getInventory().addItem(stack);
    }
    private void addObsidianToInventory(Player p) {
        ItemStack stack = new ItemStack(Material.OBSIDIAN, 64);
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Perk item"
        ));
        meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
        p.getInventory().addItem(stack);
    }
}
