package me.twostinkysocks.boxplugin.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractPerk {

    private ItemStack guiItem;

    private int cost;

    private String key;

    public AbstractPerk() {
        this.guiItem = null;
        this.cost = 0;
        this.key = null;
    }

    public ItemStack getGuiItem(Player p) {
        return guiItem;
    }

    public int getCost() {
        return cost;
    }

    public String getKey() {
        return key;
    }

    public void setGuiItem(ItemStack guiItem) {
        this.guiItem = guiItem;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void onRespawn(PlayerRespawnEvent e) {}

    public void onDeath(PlayerDeathEvent e) {}

    public void onJoin(PlayerJoinEvent e){}
    public void onQuit(PlayerQuitEvent e){}

    public void onEquip(Player p) {}

    public void onUnequip(Player p) {}
}
