package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PerkHaste extends AbstractPerk implements Upgradable {
    public PerkHaste() {
        setCost(4);

        setKey("perk_haste");
    }

    @Override
    public ItemStack getGuiItem(Player p) {
        ItemStack guiItem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) guiItem.getItemMeta();
        meta.setColor(Color.YELLOW);
        meta.setDisplayName(ChatColor.YELLOW + "Haste");
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain permanent Haste " + getLevel(p)
        ));
        guiItem.setItemMeta(meta);
        return guiItem;
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if(getLevel(p) > 0) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, getLevel(p)-1, true, false));
        }
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {
    }

    @Override
    public void onEquip(Player p) {
        if(getLevel(p) > 0) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, getLevel(p)-1, true, false));
        }
    }

    @Override
    public void onUnequip(Player p) {
        p.removePotionEffect(PotionEffectType.HASTE);
    }

    @Override
    public boolean upgrade(Player p) {
        if(getLevel(p) < getMaxLevel()) {
            boolean success = buyUpgrade(p);
            if(success) {
                setLevel(p, getLevel(p)+1);
                onEquip(p);
            }
            return success;
        } else {
            return false;
        }
    }

    private boolean buyUpgrade(Player p) {
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

    @Override
    public int getNextCost(int currentLevel) {
        return (int) Math.pow(4, currentLevel);
    }

    @Override
    public int getNextTeraCost(int currentLevel) {
        return (int) (getNextCost(currentLevel)/64) - getNextHexidiumCost(currentLevel)*64;
    }

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
        return 8;
    }

    @Override
    public int getLevel(Player p) {
        return p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, getKey() + "_level"), PersistentDataType.INTEGER) ? p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, getKey() + "_level"), PersistentDataType.INTEGER) : 1;
    }

    @Override
    public void setLevel(Player p, int level) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, getKey() + "_level"), PersistentDataType.INTEGER, level);
    }
}
