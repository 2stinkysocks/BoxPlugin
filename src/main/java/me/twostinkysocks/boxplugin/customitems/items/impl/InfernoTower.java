package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import me.twostinkysocks.boxplugin.util.RenderUtil;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class InfernoTower extends CustomItem {

    private HashMap<UUID, Long> cooldown;
    private HashMap<UUID, Long> renderTimers;
    private HashMap<UUID, Long> fireTimers;

    public InfernoTower(CustomItemsMain plugin) {
        super(
                "§6Inferno Tower",
                "INFERNO_TOWER",
                Material.LANTERN,
                plugin
        );

        cooldown = new HashMap<>();
        renderTimers = new HashMap<>();
        fireTimers = new HashMap<>();

        setClick((e, a) -> {
            e.setCancelled(true);
            if(a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
            Player p = e.getPlayer();
            if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(1000*20 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                go(p);
            } else {
                BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                bd = bd.round(new MathContext(2));
                p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            }
        });
    }

    private void go(Player p) {
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            Location origin = p.getLocation().clone().add(0,3,0);

            Collection<Entity> nearby = p.getWorld().getNearbyEntities(origin, 15, 15, 15, e -> !p.getUniqueId().equals(e.getUniqueId()) && e instanceof LivingEntity && e.getLocation().distanceSquared(origin) <= 15*15);
            Util.debug(p, "Found " + nearby.size() + " living entities within 10 blocks");
            int i = 0; // max 4 entities
            for(Entity e : nearby) {
                if(i >= 4) break;


                // ray trace
                Vector travelDir = e.getLocation().toVector().subtract(origin.toVector());
                // draw line
                Util.debug(p, "Raytraced entity was FOUND");
                renderLine(origin, travelDir);
                LivingEntity dmg = (LivingEntity) e;
                if(dmg instanceof Player) {
                    dmg.damage(10, p);
                } else {
                    dmg.damage(20, p);
                }

                i++;
            }

            if(fireTimers.containsKey(p.getUniqueId())) {
                fireTimers.put(p.getUniqueId(), fireTimers.get(p.getUniqueId())+1);
            } else {
                fireTimers.put(p.getUniqueId(), 0L);
            }
            if(fireTimers.get(p.getUniqueId()) > 3) {
                fireTimers.remove(p.getUniqueId());
                task.cancel();
            }
        }, 0, 20);
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            renderSphere(
                    p.getLocation().clone().add(0,3,0), 30, 0.5);

            if(renderTimers.containsKey(p.getUniqueId())) {
                renderTimers.put(p.getUniqueId(), renderTimers.get(p.getUniqueId())+1);
            } else {
                renderTimers.put(p.getUniqueId(), 0L);
            }
            if(renderTimers.get(p.getUniqueId()) > 48) {
                renderTimers.remove(p.getUniqueId());
                task.cancel();
            }
        }, 0, 2);
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack item = super.getItemStack();
        ItemMeta itemMeta = item.getItemMeta(); // will never be null
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, false);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
        return item;
    }

    private void renderSphere(Location loc, int amount, double radius){
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.5F);
        double phi = (Math.sqrt(5) + 1) / 2;
        double goldenAngle = 2 * Math.PI * (phi - 1);

        for (int i = 0; i < amount; i++) {
            double z = 1 - (2.0 * i) / (amount - 1);
            double r = Math.sqrt(1 - z * z);
            double theta = i * goldenAngle;

            double x = r * Math.cos(theta);
            double y = r * Math.sin(theta);

            Vector point = new Vector(x, y, z).multiply(radius);

            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(point), 1, 0, 0, 0, dustOptions);
        }
    }

    private void renderLine(Location origin, Vector direction) {
        Location originClone = origin.clone();
        int points = (int) direction.length() * 8;
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.5F);

        Vector directionShort = direction.normalize().divide(new Vector(8,8,8));

        for(int i = 0; i < points; i++) {
            origin.getWorld().spawnParticle(Particle.DUST, originClone, 1, 0, 0, 0, dustOptions);
            originClone.add(directionShort);
        }


    }
}
