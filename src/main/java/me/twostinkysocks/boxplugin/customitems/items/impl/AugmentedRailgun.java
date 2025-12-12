package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.util.Laser;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import me.twostinkysocks.boxplugin.util.RenderUtil;
import me.twostinkysocks.boxplugin.util.RayTrace;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

public class AugmentedRailgun extends CustomItem {

    private HashMap<UUID, Long> cooldown;
    private HashMap<UUID, Integer> particleTimers;
    private HashMap<UUID, Integer> ring1Timer;
    private HashMap<UUID, Integer> ring2Timer;
    private HashMap<UUID, Integer> ring3Timer;
    private HashMap<UUID, Integer> finalRingTimer;

    public AugmentedRailgun(CustomItemsMain plugin) {
        super(
                ChatColor.WHITE + "Augmented Railgun",
                "AUGMENTED_RAILGUN",
                Material.DIAMOND_HOE,
                plugin
        );
        cooldown = new HashMap<>();
        particleTimers = new HashMap<>();
        ring1Timer = new HashMap<>();
        ring2Timer = new HashMap<>();
        ring3Timer = new HashMap<>();
        finalRingTimer = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(1000*15 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                    try {
                        shoot(p);
                    } catch (ReflectiveOperationException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            }
        });
    }

    private void spawnEffects(Player p, UUID instanceUUID, Vector direction) throws ReflectiveOperationException {
        //old 1.19 version idk
//        Location startLoc = p.getLocation().clone().add(0, 1, 0);
//
//        Location endLoc = p.getTargetBlock(Set.of(Material.values()), 50).getLocation();
//
//        Location middle = new Location(startLoc.getWorld(), (startLoc.getX()+endLoc.getX())/2, (startLoc.getY()+endLoc.getY())/2, (startLoc.getZ()+endLoc.getZ())/2);

        //new clanker given 1.21 version
        Location startLoc = p.getLocation().clone().add(0, 1, 0);
        Vector dir = startLoc.getDirection().normalize();
        Location endLoc = startLoc.clone().add(dir.multiply(50));
        Location middle = new Location(
                startLoc.getWorld(),
                (startLoc.getX() + endLoc.getX()) / 2,
                (startLoc.getY() + endLoc.getY()) / 2,
                (startLoc.getZ() + endLoc.getZ()) / 2
        );

        p.getWorld().playSound(startLoc, Sound.BLOCK_CONDUIT_ACTIVATE, 0.5f, 2f);
        p.getWorld().playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2f);
        p.getWorld().playSound(endLoc, Sound.BLOCK_CONDUIT_ACTIVATE, 0.5f, 2f);
        p.getWorld().playSound(endLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2f);
        p.getWorld().playSound(middle, Sound.BLOCK_CONDUIT_ACTIVATE, 0.5f, 2f);
        p.getWorld().playSound(middle, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2f);

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case
            p.getWorld().spawnParticle(Particle.GLOW, startLoc.clone().add(0, -.1, 0), 10, 0.1, 0.1, 0.1, 0);
            p.getWorld().spawnParticle(Particle.SCULK_SOUL, startLoc, 10, 0.15, 0.15, 0.15, 0);
            if(particleTimers.containsKey(instanceUUID)) {
                particleTimers.put(instanceUUID, particleTimers.get(instanceUUID)+1);
            } else {
                particleTimers.put(instanceUUID, 0);
            }
            if(particleTimers.get(instanceUUID) > 15) {
                particleTimers.remove(instanceUUID);
                task.cancel();
            }
        }, 0, 1);
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            Vector translation = direction.clone().normalize(); // 1 block
            spawnCircle(p, startLoc.clone().add(translation), translation, 0.5, 50);

            if(ring1Timer.containsKey(instanceUUID)) {
                ring1Timer.put(instanceUUID, ring1Timer.get(instanceUUID)+1);
            } else {
                ring1Timer.put(instanceUUID, 0);
            }
            if(ring1Timer.get(instanceUUID) > 15) {
                ring1Timer.remove(instanceUUID);
                task.cancel();
            }
        }, 0, 1);
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            Vector translation = direction.clone().normalize().multiply(2); // 2 blocks
            spawnCircle(p, startLoc.clone().add(translation), translation, 0.5, 50);


            if(ring2Timer.containsKey(instanceUUID)) {
                ring2Timer.put(instanceUUID, ring2Timer.get(instanceUUID)+1);
            } else {
                ring2Timer.put(instanceUUID, 0);
            }
            if(ring2Timer.get(instanceUUID) > 13) {
                ring2Timer.remove(instanceUUID);
                task.cancel();
            }
        }, 2, 1);
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            Vector translation = direction.clone().normalize().multiply(3); // 3 blocks
            spawnCircle(p, startLoc.clone().add(translation), translation, 0.5, 50);

            if(ring3Timer.containsKey(instanceUUID)) {
                ring3Timer.put(instanceUUID, ring3Timer.get(instanceUUID)+1);
            } else {
                ring3Timer.put(instanceUUID, 0);
            }
            if(ring3Timer.get(instanceUUID) > 11) {
                ring3Timer.remove(instanceUUID);
                task.cancel();
            }
        }, 4, 1);


        Laser laser = new Laser.GuardianLaser(startLoc, endLoc, -1, -1);
        laser.start(BoxPlugin.instance);

        Location startLocClone = startLoc.clone();
        // fire
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            Vector translation = direction.clone().normalize().multiply(1);
            Location locationidk = startLocClone.clone().add(translation);

            spawnCircle(p, locationidk, translation, 0.5, 50, 0);
            translation = direction.clone().normalize().multiply(2);

            spawnCircle(p, locationidk, translation, 0.5, 50, 0);
            translation = direction.clone().normalize().multiply(3);
            spawnCircle(p, locationidk, translation, 0.5, 50, 0);

            p.getWorld().spawnParticle(Particle.LAVA, locationidk, 50, 0.3, 0.3, 0.3);

            startLocClone.add(direction.clone().normalize().multiply(3));
            if(finalRingTimer.containsKey(instanceUUID)) {
                finalRingTimer.put(instanceUUID, finalRingTimer.get(instanceUUID)+1);
            } else {
                finalRingTimer.put(instanceUUID, 0);
            }
            if(finalRingTimer.get(instanceUUID) < 8) {
                Vector direc2 = direction.clone().normalize().multiply(0.1);
                Vector direc = direc2.clone();
                if(laser.isStarted()) {
                    laser.stop();
                }
            }
            if(finalRingTimer.get(instanceUUID) > 16) {
                finalRingTimer.remove(instanceUUID);
                task.cancel();
            }
        }, 13, 1);
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
            p.getWorld().playSound(startLoc, Sound.BLOCK_CONDUIT_DEACTIVATE, 0.5f, 2f);
            p.getWorld().playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.7f);
            p.getWorld().playSound(startLoc, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.4f, 0.5f);
            p.getWorld().playSound(startLoc, Sound.ENTITY_WITHER_DEATH, 0.2f, 2f);
            p.getWorld().playSound(startLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.35f, 1.6f);
            p.getWorld().playSound(startLoc, Sound.ITEM_TRIDENT_THUNDER, 0.5f, 0.85f);
            p.getWorld().playSound(endLoc, Sound.BLOCK_CONDUIT_DEACTIVATE, 0.5f, 2f);
            p.getWorld().playSound(endLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.7f);
            p.getWorld().playSound(endLoc, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.4f, 0.5f);
            p.getWorld().playSound(endLoc, Sound.ENTITY_WITHER_DEATH, 0.2f, 2f);
            p.getWorld().playSound(endLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.35f, 1.6f);
            p.getWorld().playSound(endLoc, Sound.ITEM_TRIDENT_THUNDER, 0.5f, 0.85f);
            Location middle2 = new Location(startLoc.getWorld(), (startLoc.getX()+endLoc.getX())/2, (startLoc.getY()+endLoc.getY())/2, (startLoc.getZ()+endLoc.getZ())/2);
            p.getWorld().playSound(middle2, Sound.BLOCK_CONDUIT_DEACTIVATE, 0.5f, 2f);
            p.getWorld().playSound(middle2, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.7f);
            p.getWorld().playSound(middle2, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.4f, 0.5f);
            p.getWorld().playSound(middle2, Sound.ENTITY_WITHER_DEATH, 0.2f, 2f);
            p.getWorld().playSound(middle2, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.35f, 1.6f);
            p.getWorld().playSound(middle2, Sound.ITEM_TRIDENT_THUNDER, 0.5f, 0.85f);
            if(laser.isStarted()) {
                laser.stop();
            }
        }, 15);

    }

    private void shoot(Player p) throws ReflectiveOperationException {
        UUID instanceUUID = UUID.randomUUID();
        Location startLoc = p.getLocation().clone().add(0, 1, 0);

        spawnEffects(p, instanceUUID, p.getLocation().getDirection().normalize());

        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
            List<Entity> nearbyEntities = new ArrayList<>(startLoc.getWorld().getNearbyEntities(startLoc, 50, 50, 50));

            //DEBUG//
//            for(Entity ent : nearbyEntities) {
//                ent.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, ent.getLocation().clone().add(0,1,0), 1, 0, 0, 0, 0);
//            }
            //DEBUG//

            //List<Damageable> damageables = raycastEntities(lineOfSight, nearbyEntities);
            List<Damageable> damageables = raycastEntitiesAccurate(startLoc, nearbyEntities);

            //DEBUG//
//            for(Entity ent : damageables) {
//                ent.getWorld().spawnParticle(Particle.END_ROD, ent.getLocation().clone().add(0,1,0), 50, 0.2, 0.2, 0.2, 0);
//            }
            //DEBUG//

            for(Damageable d : damageables) {
                if(d instanceof ArmorStand) return;
                if(d instanceof ItemFrame) return;
                if(d instanceof LivingEntity) {
                    if(d.getUniqueId().equals(p.getUniqueId())) continue;
                    ((LivingEntity) d).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 2, true, false));
                }
                if(d instanceof Player && ((Player) d).isBlocking()) {
                    Util.hitThroughShield(p, (HumanEntity) d, 100, 30);
                } else {
                    d.damage(100, p);
                }
            }
        }, 15);
    }

    private List<Damageable> raycastEntities(List<Block> lineOfSight, List<Entity> nearbyEntities) {
        ArrayList<Damageable> entities = new ArrayList<>();
        for(Entity entity : nearbyEntities) {
            for(Block block : lineOfSight) {
                if(entity instanceof Damageable) {
                    if(entity.getLocation().distance(block.getLocation()) < 2) {
                        entities.add((Damageable) entity);
                    }
                }
            }
        }
        return entities;
    }

    private List<Damageable> raycastEntitiesAccurate(Location location, List<Entity> nearbyEntities) {
        RayTrace trace = new RayTrace(location.toVector(), location.getDirection());
        return trace.intersectsWithEntities(nearbyEntities, 50, 0.5, 0.75).stream().filter(entity -> entity instanceof Damageable).map(entity -> (Damageable) entity).collect(Collectors.toList());
    }

    private void spawnCircle(Player p, Location circleLocation, Vector direction, double radius, int points, double speed) {
        double interval = 2*Math.PI/points;
        for(int i = 0; i < points; i++) {
            double t = i*interval;
            double x = radius * Math.cos(t);
            double y = radius * Math.sin(t);
            double z = 0;
            Vector v = new Vector(x,y,z);
            v = RenderUtil.rotateFunction(v, new Location(p.getWorld(), 0,0,0).setDirection(direction));
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, new Location(p.getWorld(), circleLocation.getX() + v.getX(), circleLocation.getY() + v.getY(), circleLocation.getZ() + v.getZ()), 1, 0, 0, 0, speed);
        }


        //
//        // this works
//        int points = 50;
//        double radius = 0.5;
//        double interval = 2*Math.PI/points;
//        Location circleLocation = startLoc.clone();
//        for(int i = 0; i < points; i++) {
//            double t = i*interval;
//            double x = radius * Math.cos(t);
//            double y = radius * Math.sin(t);
//            double z = 0;
//            Vector v = new Vector(x,y,z);
//            v = RenderUtil.rotateFunction(v, new Location(p.getWorld(), 0,0,0).setDirection(direction));
//            p.getWorld().spawnParticle(Particle.DRIP_LAVA, new Location(p.getWorld(), circleLocation.getX() + v.getX(), circleLocation.getY() + v.getY(), circleLocation.getZ() + v.getZ()), 0, 0, 0, 0);
//        }
        //
    }

    private void spawnCircle(Player p, Location circleLocation, Vector direction, double radius, int points) {
        double interval = 2*Math.PI/points;
        for(int i = 0; i < points; i++) {
            double t = i*interval;
            double x = radius * Math.cos(t);
            double y = radius * Math.sin(t);
            double z = 0;
            Vector v = new Vector(x,y,z);
            v = RenderUtil.rotateFunction(v, new Location(p.getWorld(), 0,0,0).setDirection(direction));
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, new Location(p.getWorld(), circleLocation.getX() + v.getX(), circleLocation.getY() + v.getY(), circleLocation.getZ() + v.getZ()), 1, 0, 0, 0);
        }


        //
//        // this works
//        int points = 50;
//        double radius = 0.5;
//        double interval = 2*Math.PI/points;
//        Location circleLocation = startLoc.clone();
//        for(int i = 0; i < points; i++) {
//            double t = i*interval;
//            double x = radius * Math.cos(t);
//            double y = radius * Math.sin(t);
//            double z = 0;
//            Vector v = new Vector(x,y,z);
//            v = RenderUtil.rotateFunction(v, new Location(p.getWorld(), 0,0,0).setDirection(direction));
//            p.getWorld().spawnParticle(Particle.DRIP_LAVA, new Location(p.getWorld(), circleLocation.getX() + v.getX(), circleLocation.getY() + v.getY(), circleLocation.getZ() + v.getZ()), 0, 0, 0, 0);
//        }
        //
    }
}
