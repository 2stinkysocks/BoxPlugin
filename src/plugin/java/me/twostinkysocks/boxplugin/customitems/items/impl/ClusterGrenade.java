package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ClusterGrenade extends CustomItem {

    private HashMap<UUID, Long> cooldown;


    public ClusterGrenade(CustomItemsMain plugin) {
        super(
                ChatColor.WHITE + "Cluster Grenade",
                "CLUSTER_GRENADE",
                Material.TNT,
                plugin,
                true
        );
        cooldown = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(500 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                    shoot(p);
                    ItemStack toRemove = e.getItem().clone();
                    toRemove.setAmount(1);
                    p.getInventory().removeItem(toRemove);
                } else {
                    BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            }
        });
    }

    private void shoot(Player p) {
        UUID instanceUUID = UUID.randomUUID();
        Location startLoc = p.getEyeLocation().clone();
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT,1f, 1.6f);
        spawnTNT(p, startLoc, startLoc.getDirection().normalize(), 40, instanceUUID);
    }

    private void spawnTNT(Player p, Location startLoc, Vector velocity, int fuse, UUID instanceUUID) {
        TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(startLoc, EntityType.TNT);
        tnt.setFuseTicks(fuse);
        tnt.setVelocity(velocity.multiply(2));
        tnt.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "CLUSTER_GRENADE_ENTITY"), PersistentDataType.STRING, p.getUniqueId().toString());
        tnt.setSource(p);
        AtomicInteger trailTimer = new AtomicInteger();
        AtomicReference<Location> firstFinalLoc = new AtomicReference<>();
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tnt.getLocation(), 5, 0, 0, 0);

            trailTimer.getAndIncrement();
            if(trailTimer.get()%2==0) {
                p.getWorld().playSound(tnt.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.7f);
            }
            if(trailTimer.get() >= fuse-1) {
                firstFinalLoc.set(tnt.getLocation());
                task.cancel();
            }
        }, 0, 1);
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, task -> {
            Location origin = firstFinalLoc.get().clone();
            int newFuse = 20;
            origin.setDirection(new Vector(0.5, 1, 0));
            for(int i = 0; i < 6; i++) {
                TNTPrimed tnt2 = (TNTPrimed) p.getWorld().spawnEntity(firstFinalLoc.get(), EntityType.TNT);
                tnt2.setFuseTicks(newFuse+(new Random().nextInt(6)));
                tnt2.setVelocity(origin.getDirection().normalize().multiply(0.5));
                origin.setDirection(origin.getDirection().rotateAroundY(Math.toRadians(60)));
                tnt2.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "CLUSTER_GRENADE_ENTITY"), PersistentDataType.STRING, p.getUniqueId().toString());
                tnt2.setSource(p);
                AtomicInteger timer = new AtomicInteger();
                Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, t -> {
                    if(t.isCancelled()) return; // just in case

                    p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tnt2.getLocation(), 5, 0, 0, 0);

                    timer.getAndIncrement();
                    if(timer.get() >= newFuse-1) {
                        t.cancel();
                    }
                }, 0, 1);
            }
            AtomicInteger timer = new AtomicInteger();
            Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, t -> {
                if(t.isCancelled()) return;

                if(timer.get()%2==0) {
                    p.getWorld().playSound(firstFinalLoc.get(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
                }

                timer.getAndIncrement();
                if(timer.get() >= newFuse-1) {
                    t.cancel();
                }
            }, 0, 1);
        }, 42);
    }
}
