package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AxeOfTheShredded extends CustomItem {

    private HashMap<UUID, Long> cooldown;
    private HashMap<Player, ArrayList<Axe>> spawnedAxes;

    private class Axe {
        private ArmorStand stand;
        private Vector vector;
        public Axe(ArmorStand stand, Vector vector) {
            this.vector = vector;
            this.stand = stand;
        }
        public void setArmorStand(ArmorStand stand) {
            this.stand = stand;
        }
        public void setVelocity(Vector vector) {
            this.vector = vector;
        }
        public ArmorStand getArmorStand() {
            return this.stand;
        }
        public Vector getVelocity() {
            return this.vector;
        }
    }

    public AxeOfTheShredded(CustomItemsMain plugin) {
        super(
                ChatColor.WHITE + "Throwing Knife",
                "THROWING_KNIFE",
                Material.IRON_SWORD,
                plugin,
                false,
                "",
                ChatColor.GRAY + "Right click to throw!"
        );
        cooldown = new HashMap<>();
        spawnedAxes = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(3000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                    spawnAxe(p);
                }
            }
        });

        setLeave(p -> {
            cooldown.remove(p.getUniqueId());
            spawnedAxes.remove(p);
        });

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(Player p : spawnedAxes.keySet()) {
                if(p.isValid() && p.isOnline()) {
                    for(Axe axe : new ArrayList<>(spawnedAxes.get(p))) {
                        if(!axe.getArmorStand().isDead()) {
                            if(!collidingWithBlock(axe.getArmorStand())) {
                                //axe.getArmorStand().setVelocity(axe.getVelocity());
                                axe.getArmorStand().setVelocity(new Vector(0,0,0));
                                Vector delta = axe.getVelocity();
                                Location newLoc = axe.getArmorStand().getLocation().add(delta);
                                axe.getArmorStand().teleport(newLoc);
                                axe.getArmorStand().setRightArmPose(new EulerAngle((axe.getArmorStand().getRightArmPose().getX()+Math.toRadians(30)) > Math.PI*2 ? (axe.getArmorStand().getRightArmPose().getX()+Math.toRadians(30))%(Math.PI*2) : (axe.getArmorStand().getRightArmPose().getX()+Math.toRadians(30)), 0, 0));
                                damage(p, axe);
                            } else {
                                axe.getArmorStand().remove();
                            }
                        } else {
                            spawnedAxes.get(p).remove(axe);
                        }
                    }
                }
            }
        }, 1, 1);
    }

    private void damage(Player p, Axe axe) {
        List<Entity> nearby = axe.getArmorStand().getNearbyEntities(0.5, 0.5, 0.5);
        for(Entity entity : nearby) {
            if(entity instanceof Damageable && !entity.equals(p) && !(entity.getType() == EntityType.ARMOR_STAND)) {
                Damageable damageable = (Damageable) entity;
//                (((CraftLivingEntity) damageable).getHandle()).a((new EntityDamageSource("thorns", ((CraftPlayer)p).getHandle())), 11); // same as railgun
                damageable.damage(11, p);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 0f);
                //axe.getArmorStand().remove();
            }
        }
    }

    private boolean collidingWithBlock(ArmorStand e) {
        Block head = e.getEyeLocation().getBlock();
        BoundingBox box = e.getBoundingBox();
        box.expand(0.1);
        BoundingBox xph = head.getRelative(1, 0 ,0).getBoundingBox();
        BoundingBox xmh = head.getRelative(-1, 0 ,0).getBoundingBox();
        BoundingBox yph = head.getRelative(0, 1 ,0).getBoundingBox();
        BoundingBox zph = head.getRelative(0, 0 ,1).getBoundingBox();
        BoundingBox zmh = head.getRelative(0, 0 ,-1).getBoundingBox();
        return box.overlaps(xph) || box.overlaps(xmh) || box.overlaps(yph) || box.overlaps(zph) || box.overlaps(zmh);
    }

    private boolean isNextToBlock(Entity e) {
        Block head = e.getLocation().add(0, 1, 0).getBlock();
        Block xph = head.getRelative(1, 0 ,0);
        Block xmh = head.getRelative(-1, 0 ,0);
        Block yph = head.getRelative(0, 1 ,0);
        Block ymh = head.getRelative(0, -1 ,0);
        Block zph = head.getRelative(0, 0 ,1);
        Block zmh = head.getRelative(0, 0 ,-1);
        if(xph.getType() != Material.AIR || xmh.getType() != Material.AIR || yph.getType() != Material.AIR || ymh.getType() != Material.AIR || zph.getType() != Material.AIR || zmh.getType() != Material.AIR) {
            return true;
        }
        return false;
    }

    private ArmorStand createStand(Player p) {
        ArmorStand armorStand = (ArmorStand) p.getWorld().spawnEntity(new Location(p.getWorld(), 0, 0, 0), EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setArms(true);
        armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        armorStand.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, getItemId()), PersistentDataType.INTEGER, 1);
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
            armorStand.remove();
        }, 4*20); // kill stand timer
        armorStand.setInvulnerable(true);
        Location armorStandLocation = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().rotateAroundY(Math.PI / 2).multiply(0.5));
        armorStand.teleport(armorStandLocation);
        armorStand.setVelocity(p.getLocation().getDirection().normalize().multiply(2));
        return armorStand;
    }

    private void spawnAxe(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 0.5f);
        ArmorStand armorStand = createStand(p);

        if(spawnedAxes.containsKey(p)) {
            if(!spawnedAxes.get(p).contains(p)) {
                spawnedAxes.get(p).add(new Axe(armorStand, p.getLocation().getDirection().normalize().multiply(0.7)));
            }
        } else {
            spawnedAxes.put(p, new ArrayList<>(List.of(new Axe(armorStand, p.getLocation().getDirection().normalize().multiply(0.7)))));
        }
    }
}
