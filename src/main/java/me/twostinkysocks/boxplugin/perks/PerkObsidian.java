package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PerkObsidian extends AbstractPerk {
    private HashSet<UUID> playersWithObiPerk = new HashSet<>();
    private boolean runningTimer = false;
    public PerkObsidian() {
        ItemStack guiItem = new ItemStack(Material.OBSIDIAN);
        ItemMeta meta = guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "Obsidian");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain 64 Obsidian that regenerates",
                ChatColor.GRAY + "1 back every 10 seconds"
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
        addObsidianToInventory(p, 64);
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
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(hasObiPerk(p)){
            playersWithObiPerk.add(p.getUniqueId());
        }
    }

    @Override
    public void onEquip(Player p) {
        removeOldObsidianFromInventory(p);
        playersWithObiPerk.add(p.getUniqueId());
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "obsidian_perk_item_count"), PersistentDataType.INTEGER)) {
            addObsidianToInventory(p, p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "obsidian_perk_item_count"), PersistentDataType.INTEGER));
        } else {
            addObsidianToInventory(p, 64);
        }
    }

    @Override
    public void onUnequip(Player p) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "obsidian_perk_item_count"), PersistentDataType.INTEGER, getObsidianCountInInventory(p));
        removeOldObsidianFromInventory(p);
        playersWithObiPerk.remove(p.getUniqueId());
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

    private void updateObsidianInInventory(Player p, int amount) {
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.OBSIDIAN && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                item.setAmount(amount);
            }
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

    public boolean hasObiPerk(Player p){
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "obsidian_perk_item_count"), PersistentDataType.INTEGER)){
            return true;
        }
        return false;
    }

    private void addObsidianToInventory(Player p, int amount) {
        if(amount <= 0){
            amount = 1;
        }
        ItemStack stack = new ItemStack(Material.OBSIDIAN, amount);
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Perk item"
        ));
        meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
        p.getInventory().addItem(stack);

        if(runningTimer){
            return;
        }
        runningTimer = true;

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(playersWithObiPerk.isEmpty()){
                runningTimer = false;
                task.cancel();
            }
            for(UUID uuid : playersWithObiPerk){
                Player fella = Bukkit.getPlayer(uuid);
                int obiCount = getObsidianCountInInventory(fella);
                obiCount += 1;
                if(obiCount <= 64){
                    updateObsidianInInventory(p, obiCount);
                }
                if(obiCount < 1){
                    addObsidianToInventory(p, 1);
                }

            }
        }, 5L, 200L);//every 10 seconds
    }
}
