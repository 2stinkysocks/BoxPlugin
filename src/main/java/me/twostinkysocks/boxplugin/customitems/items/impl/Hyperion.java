package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import me.twostinkysocks.boxplugin.util.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import javax.naming.Name;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Hyperion extends CustomItem {

    private HashMap<UUID, Long> tpcooldown;
    private HashMap<UUID, Long> slamcooldown;
    private HashMap<UUID, Long> resistancecooldown;

    private HashMap<UUID, Integer> inAirTime;

    public Hyperion(CustomItemsMain plugin) {
        super(
                "Hyperion",
                "HYPERION",
                Material.NETHERITE_SWORD,
                plugin,
                false
        );
        tpcooldown = new HashMap<>();
        slamcooldown = new HashMap<>();
        resistancecooldown = new HashMap<>();
        inAirTime = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(BoxPlugin.instance.getCurseManager().hasCurse(p)){
                p.sendMessage(ChatColor.RED + "You cannot use magic items without a soul!");
                return;
            }
            if((a == Action.RIGHT_CLICK_BLOCK && !p.isSneaking()) || a == Action.RIGHT_CLICK_AIR) {
                if(p.hasPermission("customitems.cooldownbypass") || !tpcooldown.containsKey(p.getUniqueId()) || tpcooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    tpcooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(10000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                    tp(p);
                } else {
                    e.setCancelled(true);
                    BigDecimal bd = new BigDecimal(((double)(tpcooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            } else if(a == Action.RIGHT_CLICK_BLOCK && p.getLocation().getPitch() > 45) {
                if(p.hasPermission("customitems.cooldownbypass") || !resistancecooldown.containsKey(p.getUniqueId()) || resistancecooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    resistancecooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(25000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                    resistance(p);
                } else {
                    e.setCancelled(true);
                    BigDecimal bd = new BigDecimal(((double)(resistancecooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            }
        });
        setSneak((e) -> {
            Player p = e.getPlayer();
            if(!p.isSneaking()){
                return;
            }
            Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
                if(task.isCancelled()) return; // just in case
                if(p.isSneaking() && !p.isFlying() && p.getVelocity().getY() < -0.1 && !p.isInWater() && !p.isInsideVehicle() && !p.isClimbing() && !p.isGliding() && p.getPotionEffect(PotionEffectType.SLOW_FALLING) == null) {
                    if(p.hasPermission("customitems.cooldownbypass") || !slamcooldown.containsKey(p.getUniqueId()) || slamcooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                        if(p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("HYPERION")) {
                            if(!inAirTime.containsKey(p.getUniqueId())) {
                                inAirTime.put(p.getUniqueId(), 0);
                            }
                            if(inAirTime.get(p.getUniqueId()) % 3 == 0) {
                                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
                            }
                            p.setVelocity(p.getVelocity().add(new Vector(0, -0.1, 0)));
                            p.getWorld().spawnParticle(Particle.EXPLOSION, p.getLocation(), 5, 0, 0, 0, 0.5);
                            inAirTime.put(p.getUniqueId(), inAirTime.get(p.getUniqueId())+1);
                            Util.debug(p, "Ticks in air: " + inAirTime.get(p.getUniqueId()));
                            Util.debug(p, "Velocity: " + p.getVelocity().getY());
                        } else {
                            inAirTime.put(p.getUniqueId(), 0);
                        }
                    } else {
                        inAirTime.put(p.getUniqueId(), 0);
                    }
                } else if(p.getVelocity().getY() >= -0.1 && p.getVelocity().getY() <= 0.1 && p.isSneaking() && !p.isFlying() && !p.isInWater() && !p.isInsideVehicle() && !p.isClimbing() && !p.isGliding() && p.getPotionEffect(PotionEffectType.SLOW_FALLING) == null) {
                    if(p.hasPermission("customitems.cooldownbypass") || !slamcooldown.containsKey(p.getUniqueId()) || slamcooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                        if (p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("HYPERION")) {
                            int ticks = inAirTime.get(p.getUniqueId());
                            float rad = 4;
                            double multiplier = 1.0;
                            if (ticks < 5) return;
                            else if (ticks <= 7) {
                                rad = 3;
                                multiplier = 0.5;
                            } else if (ticks <= 10) {
                                rad = 4;
                                multiplier = 0.9;
                            }
                            else if (ticks <= 14) {
                                rad = 5;
                                multiplier = 1.3;
                            }
                            else if (ticks <= 18) {
                                rad = 6;
                                multiplier = 1.8;
                            }
                            else if (ticks <= 22) {
                                rad = 7;
                                multiplier = 2.2;
                            } else {
                                rad = 8;
                                multiplier = 2.6;
                            }
                            Util.debug(p, "Explosion level: " + multiplier);
                            Util.debug(p, "Explosion yield: " + rad);
                            TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getLocation(), EntityType.TNT);
                            tnt.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "HYPERION_BOOM_MULTIPLIER"), PersistentDataType.DOUBLE, multiplier);
                            tnt.setFuseTicks(0);
                            tnt.setSource(p);
                            PrimedTnt nmsTNT = ((CraftTNTPrimed) tnt).getHandle();
                            nmsTNT.explosionPower = rad;
//                          LargeFireball fireball = p.getWorld().spawn(p.getLocation(), LargeFireball.class);
//                          fireball.setShooter(p);
//                          fireball.setYield(rad);
//                          fireball.setVelocity(new Vector(0, -1, 0));
//                          tnt.setFuseTicks(0);
//                          fireball.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "HYPERION_BOOM_MULTIPLIER"), PersistentDataType.DOUBLE, multiplier);
                            slamcooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long) (45000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                            task.cancel();
                        } else {
                            inAirTime.remove(p.getUniqueId());
                            task.cancel();
                        }
                    } else {
                        inAirTime.remove(p.getUniqueId());
                        task.cancel();
                    }
                } else {
                    inAirTime.remove(p.getUniqueId());
                    task.cancel();
                }
            }, 0L, 1L);
        });

    }

    private void tp(Player p) {
        List<Block> lineOfSight = new ArrayList<>(p.getLineOfSight(Set.of(Material.AIR, Material.CAVE_AIR, Material.WATER, Material.LAVA, Material.LIGHT), 25));
        Location tpLocation = null;
        boolean valid = false;
        while(!valid && lineOfSight.size() > 0) {
            Block finalBlock = lineOfSight.get(lineOfSight.size()-1);
            Block oneAbove = finalBlock.getRelative(0, 1, 0);
            Block twoAbove = finalBlock.getRelative(0, 2, 0);
            if(!finalBlock.getLocation().isWorldLoaded() || !oneAbove.getLocation().isWorldLoaded() || !twoAbove.getLocation().isWorldLoaded()) {
                lineOfSight.remove(lineOfSight.size()-1);
                continue;
            }

            Set<Material> validBlocks = Set.of(Material.AIR, Material.CAVE_AIR, Material.WATER, Material.LAVA, Material.LIGHT);
            if(validBlocks.contains(oneAbove.getType()) && validBlocks.contains(oneAbove.getType())) {
                valid = true;
                tpLocation = finalBlock.getLocation().add(0, 1, 0).add(0.5, 0.0, 0.5);
            } else {
                lineOfSight.remove(lineOfSight.size()-1);
            }
        }
        // no valid location
        if(!valid) {
            tpLocation = p.getLocation().clone();
        }
        float pitch = p.getEyeLocation().getPitch();
        float yaw = p.getEyeLocation().getYaw();
        tpLocation.setYaw(yaw);
        tpLocation.setPitch(pitch);
        p.teleport(tpLocation);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    private void resistance(Player p) {
        Location loc = p.getLocation().clone().add(0, 0.3, 0);
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 1f, 0.6f);
        AtomicInteger i = new AtomicInteger();
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(i.get() % 5 == 0) {
                Util.spawnCircle(loc.getWorld(), loc, new Vector(0, 1, 0), 2.5, 100);
            }
            if(i.get() % 10 == 0) {
                List<LivingEntity> nearby = loc.getWorld().getNearbyEntities(loc, 2.5, 2.5, 2.5).stream().filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).collect(Collectors.toList());
                for(LivingEntity entity : nearby) {
                    if(entity.getLocation().distance(loc) <= 2.5) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 21, 2));
                    }
                }
            }
            i.getAndIncrement();
            if(i.get() > 120) {
                task.cancel();
            }
        }, 0L, 1L);
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = super.getItemStack();
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        return stack;
    }
}
