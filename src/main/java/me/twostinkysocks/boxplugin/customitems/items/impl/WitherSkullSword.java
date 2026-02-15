package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.block.Action;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.UUID;

public class WitherSkullSword extends CustomItem {

    private HashMap<UUID, Long> cooldown;

    public WitherSkullSword(CustomItemsMain plugin) {
        super(
                "wither skull boner lol",
                "WITHER_SKULL_SWORD",
                Material.BONE,
                plugin,
                false,
                ""
        );
        cooldown = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(5000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1))); // 5 seconds
                    //
                    WitherSkull skull = (WitherSkull) p.getWorld().spawnEntity(p.getLocation().add(0, 1, 0), EntityType.WITHER_SKULL);
                    skull.setCharged(true);
                    skull.setShooter(p);
                    skull.setDirection(p.getLocation().getDirection());
                    skull.setVelocity(p.getLocation().getDirection().normalize().multiply(3));
                    //
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0F, 1.0F);
                } else {
                    BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            }
        });
    }
}
