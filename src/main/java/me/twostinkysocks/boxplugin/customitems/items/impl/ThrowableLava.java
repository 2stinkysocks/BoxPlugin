package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.UUID;

public class ThrowableLava extends CustomItem {

    private HashMap<UUID, Long> cooldown;

    public ThrowableLava(CustomItemsMain plugin) {
        super(
                "§fThrowable Lava",
                "THROWABLE_LAVA",
                Material.LAVA_BUCKET,
                plugin,
                false,
                ""
        );
        cooldown = new HashMap<>();
        setClick((e, a) -> {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(10000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1))); // 10 seconds
                    throwLava(e);
                } else {
                    BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            }
        });
    }

    private void throwLava(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Location spawn = p.getEyeLocation().clone().add(p.getLocation().getDirection().normalize().multiply(2));
        FallingBlock magma = spawn.getWorld().spawnFallingBlock(spawn, Bukkit.createBlockData(Material.MAGMA_BLOCK));
        magma.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "LAVA"), PersistentDataType.STRING, p.getUniqueId().toString());
        magma.setHurtEntities(false);
        magma.setVelocity(spawn.getDirection().normalize().multiply(1.5));
    }
}
