package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.UUID;

public class WolfPack extends CustomItem {

    private HashMap<UUID, Long> cooldown;

    public WolfPack(CustomItemsMain plugin) {
        super(
                "§fWolf Pack",
                "WOLF_PACK",
                Material.BONE,
                plugin,
                false,
                "",
                "put in inventory to spawn wolf lol",
                "20s cooldown"
        );
        cooldown = new HashMap<>();
        setEntityDamageByEntity(e -> {
            Player p = (Player) e.getEntity();
            if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                spawnWolves(e);
            }
        });
    }

    private void spawnWolves(EntityDamageByEntityEvent e) {
        Player p = (Player) e.getEntity();
        cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(20000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1))); // 10 seconds
        for(int i = 0; i < 4; i++) {
            Wolf wolf = (Wolf) p.getWorld().spawnEntity(p.getLocation(), EntityType.WOLF);
            wolf.setTamed(true);
            wolf.setOwner(p);
            if(e.getDamager() instanceof LivingEntity) {
                wolf.setTarget((LivingEntity) e.getDamager());
            }
            wolf.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40);
            wolf.setHealth(40);
            wolf.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(8);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WOLF_HOWL, 0.5f, 1f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation(), 10, 0, 0, 0, 0.5);
    }
}
