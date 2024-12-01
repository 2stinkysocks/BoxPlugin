package me.twostinkysocks.boxplugin.customitems.items.impl;

import com.google.common.base.Preconditions;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import me.twostinkysocks.boxplugin.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TalismanOfEnergy extends CustomItem {


    private HashMap<UUID, Long> cooldown;
    // HashMap<PlayerUUID, HashMap<EntityUUID, Time>
    private HashMap<UUID, HashMap<UUID, Long>> newCooldown;
    private HashMap<UUID, Integer> boltTimers;
    // uuid: map of hits (Damage, Timestamp)
    private HashMap<UUID, ArrayList<Hit>> hits;

    class Hit {
        private Double damage;
        private Long timestamp;

        public Hit(Double damage, Long timestamp) {
            this.damage = damage;
            this.timestamp = timestamp;
        }

        public Double getDamage() {return damage;}

        public Long getTimestamp() {return timestamp;}
    }

    public TalismanOfEnergy(CustomItemsMain plugin) {
        super(
                "§bArtifact of Energy",
                "TALISMAN_OF_ENERGY",
                Material.NETHER_STAR,
                plugin,
                "",
                "§6Item Ability: Electrocute",
                "§7Landing 3 hits within 3 seconds makes your last hit strike twice,",
                "§7dealing 1/2 the average damage per hit",
                "§7over the duration",
                "§8Cooldown: §a10s"
        );
        cooldown = new HashMap<>();
        newCooldown = new HashMap<>();
        hits = new HashMap<>();
        boltTimers = new HashMap<>();
        setEntityDamageByEntity(e -> {
            Player p = (Player) e.getDamager();
            if(p.hasPermission("customitems.cooldownbypass") || !newCooldown.containsKey(p.getUniqueId()) || newCooldown.get(p.getUniqueId()) == null || !newCooldown.get(p.getUniqueId()).containsKey(e.getEntity().getUniqueId()) || newCooldown.get(p.getUniqueId()).get(e.getEntity().getUniqueId()) < System.currentTimeMillis()) {
                // only trigger logic if off cooldown
                logic(e, p);
            }
//            if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
//                // only trigger logic if off cooldown
//                logic(e, p);
//            }
        });
    }

    private void electrocute(long avg, EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof CraftLivingEntity) {
            LivingEntity nmsEntity = ((CraftLivingEntity) e.getEntity()).getHandle();
            Preconditions.checkState(!nmsEntity.generation, "Cannot damage entity during world generation");
            DamageSource reason = nmsEntity.damageSources().playerAttack(((CraftHumanEntity)e.getDamager()).getHandle()); // DamageSource.playerAttack()
            nmsEntity.invulnerableTime = 0; // set no damage ticks to 0
//            System.out.println(avg);
            if(e.getEntity() instanceof Player && ((Player) e.getEntity()).isBlocking()) {
                ((Player) e.getEntity()).setCooldown(Material.SHIELD, 30);
            }
            Util.debug((Player) e.getDamager(), "Dealt " + avg + " damage with smite");
            nmsEntity.hurt(reason, (float)avg);
            playEffects(e);
        }
    }

    private void playEffects(EntityDamageByEntityEvent e) {
        Entity damaged = e.getEntity();
        UUID instanceUUID = UUID.randomUUID();
        //particle minecraft:dust 1.0 0.1 0.2 1.5 ~ ~2 ~ 0 1 0 0 5 force
        damaged.getWorld().playSound(damaged.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1f, 0.5f);
        damaged.getWorld().playSound(damaged.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.2f, 1.49f);
        damaged.getWorld().playSound(damaged.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, .32f, 1.14f);
        damaged.getWorld().playSound(damaged.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, .36f, 1.08f);
        damaged.getWorld().playSound(damaged.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, .46f, 1f);
        damaged.getWorld().playSound(damaged.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 0.62f, 2f);
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 25, 51), 1.5F);
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case
            damaged.getWorld().spawnParticle(Particle.DUST, damaged.getLocation().clone().add(0, 2, 0), 5, 0, 3, 0, dustOptions);
            if(boltTimers.containsKey(instanceUUID)) {
                boltTimers.put(instanceUUID, boltTimers.get(instanceUUID)+1);
            } else {
                boltTimers.put(instanceUUID, 0);
            }
            if(boltTimers.get(instanceUUID) > 3) {
                boltTimers.remove(instanceUUID);
                task.cancel();
                damaged.getWorld().spawnParticle(Particle.DUST, damaged.getLocation().clone().add(0, 0.5, 0), 15, 0.5, 0.5, 0.5, dustOptions);
                damaged.getWorld().spawnParticle(Particle.DUST, damaged.getLocation(), 15, 0.8, 0.8, 0.8, dustOptions);
            }
        }, 0, 1);
    }

    private void logic(EntityDamageByEntityEvent e, Player p) {
        // if player has not hit anything yet
        if(!hits.containsKey(p.getUniqueId())) {
            // make sure inner hit array is there
            ArrayList<Hit> list = new ArrayList<>();
            hits.put(p.getUniqueId(), list);
        }
        // get the hits array
        ArrayList<Hit> pHits = hits.get(p.getUniqueId());
        // hits +1
        pHits.add(new Hit(e.getDamage(), System.currentTimeMillis()));
        // if there are 3+ hits (this time is the third hit)
        if(pHits.size() >= 3) {
            // min starts as very big and max is very small
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            long avg = 0;
            for(Hit hit : pHits) {
//                System.out.println(hit.getTimestamp());
                // if the current hit happened before
                if(hit.getTimestamp() < min) min = hit.getTimestamp();
                if(hit.getTimestamp() > max) max = hit.getTimestamp();
                avg += hit.getDamage();
            }
            avg /= ((double) pHits.size());
            long diff = max - min;
//            System.out.println(diff);
            if(diff < 3000) { // should electrocute
                long finalAvg = avg;
                if(!newCooldown.containsKey(p.getUniqueId()) || !newCooldown.get(p.getUniqueId()).containsKey(e.getEntity().getUniqueId())) {
                    HashMap<UUID, Long> cd = new HashMap<>();
                    cd.put(e.getEntity().getUniqueId(), System.currentTimeMillis() + (long)(10000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                    newCooldown.put(p.getUniqueId(), cd);
                } else {
                    newCooldown.get(p.getUniqueId()).put(e.getEntity().getUniqueId(), System.currentTimeMillis() + (long)(10000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                }
                //cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(10000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1))); // 10 seconds
                Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                    electrocute(finalAvg, e);
                    hits.remove(p.getUniqueId());
                }, 10L);
            } else {
                //              first element
                pHits.remove(0);
            }
        }

    }
}
