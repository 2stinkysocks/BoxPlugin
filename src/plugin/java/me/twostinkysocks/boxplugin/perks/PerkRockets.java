package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PerkRockets extends AbstractPerk {
    private HashSet<UUID> playersWithRocketPerk = new HashSet<>();
    private boolean runningTimer = false;
    public PerkRockets() {
        ItemStack guiItem = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Rockets");
        meta.setPower(2);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain 32 Rockets that regenerates",
                ChatColor.GRAY + "1 back every 20 seconds"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(5);

        setKey("perk_rockets");
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        removeOldRocketsFromInventory(p);
        addRocketsToInventory(p, 32);
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {
        for(ItemStack item : new ArrayList<>(e.getDrops())) {
            if(item.getType() == Material.FIREWORK_ROCKET && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                e.getDrops().remove(item);
            }
        }
    }

    @Override
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(hasRocketPerk(p)){
            playersWithRocketPerk.add(p.getUniqueId());
        }
    }

    @Override
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(hasRocketPerk(p)){
            playersWithRocketPerk.remove(p.getUniqueId());
        }
    }

    @Override
    public void onEquip(Player p) {
        removeOldRocketsFromInventory(p);
        playersWithRocketPerk.add(p.getUniqueId());
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "rocket_perk_item_count"), PersistentDataType.INTEGER)) {
            addRocketsToInventory(p, p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "rocket_perk_item_count"), PersistentDataType.INTEGER));
        } else {
            addRocketsToInventory(p, 32);
        }
    }

    @Override
    public void onUnequip(Player p) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "rocket_perk_item_count"), PersistentDataType.INTEGER, getRocketCountInInventory(p));
        removeOldRocketsFromInventory(p);
        playersWithRocketPerk.remove(p.getUniqueId());
    }

    public boolean hasRocketPerk(Player p){
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "rocket_perk_item_count"))){
            return true;
        }
        return false;
    }

    private void removeOldRocketsFromInventory(Player p) {
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.FIREWORK_ROCKET && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                p.getInventory().remove(item);
            }
        }
        ItemStack item = p.getInventory().getItemInOffHand();
        if(item != null && item.getType() == Material.FIREWORK_ROCKET && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
            p.getInventory().setItemInOffHand(null);
        }
    }

    private void updateRocketsInInventory(Player p, int amount) {
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.FIREWORK_ROCKET && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                item.setAmount(amount);
            }
        }
    }

    private int getRocketCountInInventory(Player p) {
        int count = 0;
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.getType() == Material.FIREWORK_ROCKET && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
                count += item.getAmount();
            }
        }
//        ItemStack item = p.getInventory().getItemInOffHand();
//        if(item != null && item.getType() == Material.FIREWORK_ROCKET && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1) {
//            count += item.getAmount();
//        }
        return count;
    }

    private void addRocketsToInventory(Player p, int count) {
        if(count < 1){
            count = 32;
        }
        ItemStack stack = new ItemStack(Material.FIREWORK_ROCKET, count);
        FireworkMeta meta = (FireworkMeta) stack.getItemMeta();
        meta.setPower(3);
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
            if(playersWithRocketPerk.isEmpty()){
                runningTimer = false;
                task.cancel();
            }
            for(UUID uuid : playersWithRocketPerk){
                Player fella = Bukkit.getPlayer(uuid);
                if(fella == null){
                    continue;
                }
                int rocketCount = getRocketCountInInventory(fella);
                rocketCount += 1;
                if(rocketCount <= 32){
                    updateRocketsInInventory(fella, rocketCount);
                }
                if(rocketCount < 1){
                    addRocketsToInventory(fella, 1);
                }
            }
        }, 5L, 400L);//every 20 seconds
    }
}
