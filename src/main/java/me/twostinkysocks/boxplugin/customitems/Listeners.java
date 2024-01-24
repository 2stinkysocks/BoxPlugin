package me.twostinkysocks.boxplugin.customitems;

import com.google.common.util.concurrent.AtomicDouble;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Listeners implements Listener {

    private ArrayList<CustomItem> items;

    public Listeners(ArrayList<CustomItem> items) {
        this.items = items;
    }


    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if(item != null && item.getItemMeta() != null) {
            if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING)) {
                String itemId = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING);
                if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                    for(CustomItem i : items) {
                        if(itemId.equals(i.getItemId())) {
                            i.getClick().accept(e, e.getAction());
                        }
                    }
                } else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if(e.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                        for(CustomItem i : items) {
                            if(itemId.equals(i.getItemId())) {
                                i.getClick().accept(e, e.getAction());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void entityInteract(EntityInteractEvent e) {
        for(CustomItem i : items) {
            i.getEntityInteract().accept(e);
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            for(ItemStack item : p.getInventory().getContents()) {
                // TODO: implement for all items
                if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("TALISMAN_OF_ENERGY")) {
                    for(CustomItem ci : items) {
                        if(ci.getItemId().equals("TALISMAN_OF_ENERGY")) {
                            ci.getEntityDamageByEntity().accept(e);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        if(e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();
            for(ItemStack item : damaged.getInventory().getContents()) {
                if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING)&& item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("WOLF_PACK")) {
                    for(CustomItem ci : items) {
                        if(ci.getItemId().equals("WOLF_PACK")) {
                            ci.getEntityDamageByEntity().accept(e);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void projectile(ProjectileLaunchEvent e) {
        if(e.getEntity().getShooter() instanceof Player) {
            Player p = (Player) e.getEntity().getShooter();
            if(p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("HEAL_SPEAR")) {
                e.getEntity().getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "HEAL_SPEAR"), PersistentDataType.INTEGER, 1);
            } else if(p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("MILK_POTION")) {
                e.getEntity().getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "MILK_POTION"), PersistentDataType.INTEGER, 1);
            }
        }
    }

    @EventHandler
    public void projectileLand(ProjectileHitEvent e) {
        if(e.getEntity() instanceof Trident && e.getEntity().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "HEAL_SPEAR"), PersistentDataType.INTEGER)) {
            Location location = e.getEntity().getLocation().clone();
            List<LivingEntity> near20 = e.getEntity().getNearbyEntities(15, 3, 15).stream().filter(entity -> entity instanceof Damageable && entity.getLocation().distanceSquared(location) <= 15 * 15).map(entity -> (LivingEntity) entity).collect(Collectors.toList());
            List<UUID> damaged = new ArrayList<>();
            AtomicDouble i = new AtomicDouble();
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2f, 0.5f);
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.7f);
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 2f);
            Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
                if(i.get() == 0.0) {
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 0.5f);
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_SCULK_BREAK, 0.6f, 0.5f);
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_WITHER_DEATH, 0.2f, 2f);
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 1.6f);
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 0.8f);
                }
                Util.spawnTallCircle(e.getEntity().getWorld(), location, new Vector(0, 1, 0), i.get(), 100, new Particle.DustOptions(Color.fromRGB(71, 145, 255), 1.5F));
                for(LivingEntity entity : near20) {
                    if(!(entity instanceof ArmorStand) && !(entity instanceof ItemFrame) && !(entity instanceof FallingBlock) && !damaged.contains(entity.getUniqueId()) && entity.getLocation().distance(location) <= i.get()) {
                        damaged.add(entity.getUniqueId());
                        double damage = entity.getHealth();
                        if(entity instanceof Player && ((Player) entity).isBlocking()) {
                            ((Player) entity).setCooldown(Material.SHIELD, 30);
                        }
                        if(entity instanceof Player && ((Player) entity).isBlocking()) {
                            Util.hitThroughShield((Entity) e.getEntity().getShooter(), (Player) entity, 50, 30);
                        } else {
                            entity.damage(50, (Entity) e.getEntity().getShooter());
                        }
                        if(entity.getUniqueId().equals(((Entity) e.getEntity().getShooter()).getUniqueId()) && e.getEntity().getShooter() instanceof Player && ((Player) e.getEntity().getShooter()).getHealth() < 1) {
                            ((Player)e.getEntity().getShooter()).setHealth(1);
                        }
                        damage = damage - entity.getHealth();
                        double finalDamage = damage;
                        Player p = (Player) e.getEntity().getShooter();
                        Util.debug(p, "Healed for " + Math.min(finalDamage*0.7, 0));
                        p.setHealth(Math.min(p.getHealth() + (finalDamage*0.7), p.getMaxHealth()));
                    }
                }
                i.getAndAdd(1);
                if(i.get() > 15) {
                    task.cancel();
                }
            }, 20L, 1L);
        }
    }

    @EventHandler
    public void throwPotion(PotionSplashEvent e) {
        if(e.getEntity().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "MILK_POTION"), PersistentDataType.INTEGER)) {
            for(LivingEntity entity : e.getAffectedEntities()) {
                if(entity.getActivePotionEffects().size() > 0) {
                    ArrayList<PotionEffect> effects = new ArrayList<>(entity.getActivePotionEffects());
                    for(PotionEffect effect : effects) entity.removePotionEffect(effect.getType());
                    Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                        for(PotionEffect effect : effects) entity.addPotionEffect(effect);
                    }, 20 * 30);
                }
            }
        }
    }

    @EventHandler
    public void entityExplode(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) e.getDamager();
            if(tnt.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "CLUSTER_GRENADE_ENTITY"), PersistentDataType.STRING)) {
                if(e.getEntity() instanceof Player) {
                    Player p = (Player) e.getEntity();
                    Util.debug(p, "Pre-calcualtion raw tnt damage: " + e.getDamage());
                }
                e.setDamage(e.getDamage()*3);
                if(e.getEntity() instanceof Player) {
                    Player p = (Player) e.getEntity();
                    Util.debug(p, "Post-calculation raw tnt damage: " + e.getDamage());
                    Util.debug(p, "Post-calculation final tnt damage: " + e.getFinalDamage());
                }
            }
        } else if(e.getDamager() instanceof Fireball) {
            Fireball tnt = (Fireball) e.getDamager();
            if(e.getEntity() instanceof ArmorStand || e.getEntity() instanceof ItemFrame) {
                e.setCancelled(true);
                return;
            }
            if(tnt.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "HYPERION_BOOM_MULTIPLIER"), PersistentDataType.DOUBLE)) {
                if(tnt.getShooter() instanceof Player && ((Player)tnt.getShooter()).getUniqueId().equals(e.getEntity().getUniqueId())) {
                    e.setCancelled(true);
                    return;
                }
                double multiplier = tnt.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "HYPERION_BOOM_MULTIPLIER"), PersistentDataType.DOUBLE);
                if(e.getEntity() instanceof Player) {
                    Util.debug((Player) e.getEntity(), "Explosion level: " + multiplier);
                }
                e.setDamage(e.getDamage() * multiplier);
            }
        }
    }

    @EventHandler
    public void armorStandInteract(PlayerArmorStandManipulateEvent e) {
        if(e.getRightClicked().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "THROWING_KNIFE"), PersistentDataType.INTEGER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if(e.getTarget() instanceof Player) {
            Player p = (Player) e.getTarget();
            ItemStack item = p.getInventory().getItemInMainHand();
            if(item != null && item.getItemMeta() != null) {
                if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING)) {
                    String itemId = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING);
                    for(CustomItem i : items) {
                        if(itemId.equals(i.getItemId())) {
                            i.getEntityTarget().accept(e);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBow(EntityShootBowEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if((p.getInventory().getItemInMainHand().getType() == Material.BOW && p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("PULSE_BOW")) || (p.getInventory().getItemInOffHand().getType() == Material.BOW && p.getInventory().getItemInOffHand().hasItemMeta() && p.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && p.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("PULSE_BOW"))) {
                e.getProjectile().getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "PULSE_ARROW"), PersistentDataType.INTEGER, 1);
                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 2f);
                e.getProjectile().setVelocity(e.getProjectile().getVelocity().multiply(2));
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        for(CustomItem i : items) {
            i.getLeave().accept(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for(CustomItem i : items) {
            i.getJoin().accept(e.getPlayer());
        }
    }
}
