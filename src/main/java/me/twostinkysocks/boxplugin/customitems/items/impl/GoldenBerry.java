package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.UUID;

public class GoldenBerry extends CustomItem{
    private HashMap<UUID, Long> cooldown;

    public GoldenBerry(CustomItemsMain plugin) {
        super(
                ChatColor.GOLD + "Golden Berry",
                "GOLDEN_BERRY",
                Material.GLOW_BERRIES,
                plugin,
                true
        );
        cooldown = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(2000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                    consume(p);
                    ItemStack toRemove = e.getItem().clone();
                    toRemove.setAmount(1);
                    p.getInventory().removeItem(toRemove);
                } else {
                    BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.8F, 0.5F);
                }
            }
        });
    }

    public void consume(Player p){
        double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHP = p.getHealth();
        p.setHealth(Math.min(maxHealth, (currentHP + 16)));

        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 80, 1, true, false));

        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0F, 1.5F);
    }
}
