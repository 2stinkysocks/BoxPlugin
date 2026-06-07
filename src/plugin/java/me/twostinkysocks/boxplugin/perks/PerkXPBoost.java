package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.List;

public class PerkXPBoost extends AbstractPerk implements Upgradable {
    public PerkXPBoost() {
        setKey("perk_xp_boost");

        setCost(5);
    }

    @Override
    public ItemStack getGuiItem(Player p) {
        ItemStack guiItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "XP Boost");
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain " + calculateXPMultiplier(p) + "x XP"
        ));
        guiItem.setItemMeta(meta);
        return guiItem;
    }

    // bigdecimal prevents float inaccuracy
    public BigDecimal calculateXPMultiplier(Player p) {
        BigDecimal one = new BigDecimal("1.0");
        BigDecimal proportion = new BigDecimal("0.12");
        BigDecimal level = new BigDecimal(getLevel(p));
        return one.add(proportion.multiply(level));
    }

    @Override
    public boolean upgrade(Player p) {
        if(getLevel(p) < getMaxLevel()) {
            boolean success = buyXP(p);
            if(success) {
                setLevel(p, getLevel(p)+1);
                onEquip(p);
            }
            return success;
        } else {
            return false;
        }
    }

    // 2
    @Override
    public int getNextCost(int currentLevel) {
        return (int) Math.pow(2, currentLevel);
    }

    // 0
    @Override
    public int getNextTeraCost(int currentLevel) {
        return (int) (getNextCost(currentLevel)/64) - getNextHexidiumCost(currentLevel)*64;
    }

    // 0
    @Override
    public int getNextHexidiumCost(int currentLevel) {
        return (int) ((int) getNextCost(currentLevel)/64)/64;
    }

    @Override
    public int getNextRemainderGigaCost(int currentLevel) {
        // this should always be an exact int
        return (int) (((getNextCost(currentLevel)) - (getNextHexidiumCost(currentLevel)*64*64) - getNextTeraCost(currentLevel)*64));
    }

    @Override
    public int getMaxLevel() {
        return 20;
    }

    @Override
    public int getLevel(Player p) {
        return p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, getKey() + "_level"), PersistentDataType.INTEGER) ? p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, getKey() + "_level"), PersistentDataType.INTEGER) : 1;
    }

    @Override
    public void setLevel(Player p, int level) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, getKey() + "_level"), PersistentDataType.INTEGER, level);
    }

    private boolean buyXP(Player p) {
        int gigaCoinsHeld = 0;
        int teraCubesHeld = 0;
        int hexidiumHeld = 0;
        int gigaCost = this.getNextRemainderGigaCost(this.getLevel(p));
        int teraCost = this.getNextTeraCost(this.getLevel(p));
        int hexidiumCost = this.getNextHexidiumCost(this.getLevel(p));
        for(ItemStack item: p.getInventory().getContents()) {
            if(Util.isGigaCoin(item)) {
                gigaCoinsHeld += item.getAmount();
            }
            if(Util.isTeraCube(item)) {
                teraCubesHeld += item.getAmount();
            }
            if(Util.isHexidium(item)) {
                hexidiumHeld += item.getAmount();
            }
        }
        if(gigaCoinsHeld >= gigaCost && teraCubesHeld >= teraCost && hexidiumHeld >= hexidiumCost) {
            for(ItemStack item : p.getInventory().getContents()) {
                if(teraCost == 0) break;
                if(Util.isTeraCube(item)) {
                    int amount = item.getAmount();
                    for(int i = 0; i < amount; i++) {
                        teraCost--;
                        item.setAmount(item.getAmount() - 1);
                        if(teraCost == 0) break;
                    }
                }
            }
            for(ItemStack item : p.getInventory().getContents()) {
                if(gigaCost == 0) break;
                if(Util.isGigaCoin(item)) {
                    int amount = item.getAmount();
                    for(int i = 0; i < amount; i++) {
                        gigaCost--;
                        item.setAmount(item.getAmount() - 1);
                        if(gigaCost == 0) break;
                    }
                }
            }
            for(ItemStack item : p.getInventory().getContents()) {
                if(hexidiumCost == 0) break;
                if(Util.isHexidium(item)) {
                    int amount = item.getAmount();
                    for(int i = 0; i < amount; i++) {
                        hexidiumCost--;
                        item.setAmount(item.getAmount() - 1);
                        if(hexidiumCost == 0) break;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
