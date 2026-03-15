package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PerkSteak extends AbstractPerk {
    private HashSet<UUID> playersWithSteakPerk = new HashSet<>();
    private boolean runningTimer = false;
    public PerkSteak() {
        ItemStack guiItem = new ItemStack(Material.COOKED_BEEF);
        ItemMeta meta = guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Juicy Steaks");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain 64 Steak that regenerates",
                ChatColor.GRAY + "1 back every 25 seconds"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(2);

        setKey("perk_steak");
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        removeOldSteakFromInventory(p);
        addSteakToInventory(p, 64);
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {
        for(ItemStack item : new ArrayList<>(e.getDrops())) {
            if(item.getType() == Material.COOKED_BEEF && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                e.getDrops().remove(item);
            }
        }
    }
    @Override
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(hasSteakPerk(p)){
            playersWithSteakPerk.add(p.getUniqueId());
        }
    }

    @Override
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(hasSteakPerk(p)){
            playersWithSteakPerk.remove(p.getUniqueId());
        }
    }

    @Override
    public void onEquip(Player p) {
        removeOldSteakFromInventory(p);
        playersWithSteakPerk.add(p.getUniqueId());
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "steak_perk_item_count"), PersistentDataType.INTEGER)) {
            addSteakToInventory(p, p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "steak_perk_item_count"), PersistentDataType.INTEGER));
        } else {
            addSteakToInventory(p, 64);
        }
    }

    @Override
    public void onUnequip(Player p) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "steak_perk_item_count"), PersistentDataType.INTEGER, getSteakCountInInventory(p));
        removeOldSteakFromInventory(p);
        playersWithSteakPerk.remove(p.getUniqueId());
    }

    private void removeOldSteakFromInventory(Player p) {
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.COOKED_BEEF && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                p.getInventory().remove(item);
            }
        }
        ItemStack item = p.getInventory().getItemInOffHand();
        if(item != null && item.getType() == Material.COOKED_BEEF && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
            p.getInventory().setItemInOffHand(null);
        }
    }

    private void updateSteakInInventory(Player p, int amount) {
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.COOKED_BEEF && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                item.setAmount(amount);
            }
        }
    }

    private int getSteakCountInInventory(Player p) {
        int count = 0;
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.COOKED_BEEF && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                count += item.getAmount();
            }
        }
//        ItemStack item = p.getInventory().getItemInOffHand();
//        if(item != null && item.getType() == Material.OBSIDIAN && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
//            count += item.getAmount();
//        }
        return count;
    }

    public boolean hasSteakPerk(Player p){
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "steak_perk_item_count"), PersistentDataType.INTEGER)){
            return true;
        }
        return false;
    }

    private void addSteakToInventory(Player p, int amount) {
        if(amount <= 0){
            amount = 1;
        }
        ItemStack stack = new ItemStack(Material.COOKED_BEEF, amount);
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
            if(playersWithSteakPerk.isEmpty()){
                runningTimer = false;
                task.cancel();
            }
            for(UUID uuid : playersWithSteakPerk){
                Player fella = Bukkit.getPlayer(uuid);
                if(fella == null){
                    continue;
                }
                int steakCount = getSteakCountInInventory(fella);
                steakCount += 1;
                if(steakCount <= 64){
                    updateSteakInInventory(fella, steakCount);
                }
                if(steakCount < 1){
                    addSteakToInventory(fella, 1);
                }

            }
        }, 5L, 500L);//every 25 seconds
    }
}
