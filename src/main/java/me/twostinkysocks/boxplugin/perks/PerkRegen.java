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

public class PerkRegen extends AbstractPerk{
    public PerkRegen() {
        ItemStack guiItem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) guiItem.getItemMeta();
        meta.setBasePotionType(PotionType.REGENERATION);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Regeneration");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain permanent Regeneration I"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(4);

        setKey("perk_regen");
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {

    }

    @Override
    public void onEquip(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
    }

    @Override
    public void onUnequip(Player p) {
        p.removePotionEffect(PotionEffectType.REGENERATION);
    }
}
