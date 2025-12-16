package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class MegaPerkResistance extends AbstractPerk {
    public MegaPerkResistance() {
        ItemStack guiItem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) guiItem.getItemMeta();
        meta.setBasePotionType(PotionType.INVISIBILITY);
        meta.setDisplayName(ChatColor.GRAY + "Mega Resistance");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain permanent Resistance II",
                ChatColor.GRAY + "and Regeneration I",
                "",
                ChatColor.AQUA + "Becomes Resistance III and",
                ChatColor.AQUA + "Regeneration III if combined",
                ChatColor.AQUA + "with Regeneration"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(1);

        setKey("mega_perk_resistance");
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if(hasCombo(p)){
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2, true, false));
        } else {
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
        }
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {

    }

    @Override
    public void onEquip(Player p) {
        if(hasCombo(p)){
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2, true, false));
        } else {
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, true, false));
        }
    }

    @Override
    public void onUnequip(Player p) {
        p.removePotionEffect(PotionEffectType.RESISTANCE);
        p.removePotionEffect(PotionEffectType.REGENERATION);
        if(hasCombo(p)){
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE  , Integer.MAX_VALUE, 0, true, false));
        }
    }

    public boolean hasCombo(Player p){
        ArrayList<PerksManager.MegaPerk> selectedPerks = BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p);
        PerksManager.MegaPerk perk1 = null;
        PerksManager.MegaPerk perk2 = null;
        if(selectedPerks.size() == 2){
            perk1 = selectedPerks.get(0);
            perk2 = selectedPerks.get(1);
        }
        if(perk1 == null || perk2 == null){
            return false;
        }
        if((perk1.equals(PerksManager.MegaPerk.MEGA_RESISTANCE) && perk2.equals(PerksManager.MegaPerk.MEGA_REGENERATION))
                || (perk2.equals(PerksManager.MegaPerk.MEGA_RESISTANCE) && perk1.equals(PerksManager.MegaPerk.MEGA_REGENERATION))){
            return true;
        }
        return false;
    }
}
