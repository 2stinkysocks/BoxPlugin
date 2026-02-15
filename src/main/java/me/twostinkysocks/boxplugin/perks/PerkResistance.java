package me.twostinkysocks.boxplugin.perks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class PerkResistance extends AbstractPerk{
    public PerkResistance() {
        ItemStack guiItem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) guiItem.getItemMeta();
        meta.setBasePotionType(PotionType.TURTLE_MASTER);
        meta.setDisplayName(ChatColor.GRAY + "Resistance");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain permanent Resistance I"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(6);

        setKey("perk_resistance");
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {

    }

    @Override
    public void onEquip(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onUnequip(Player p) {
        p.removePotionEffect(PotionEffectType.RESISTANCE);
    }
}
