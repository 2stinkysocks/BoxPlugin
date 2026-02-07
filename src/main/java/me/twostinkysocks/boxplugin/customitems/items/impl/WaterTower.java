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

public class WaterTower extends CustomItem {
    private int damage = 15;

    private HashMap<UUID, Long> cooldown;
    private HashMap<UUID, Long> renderTimers;
    private HashMap<UUID, Long> fireTimers;

    public WaterTower(CustomItemsMain plugin) {
        super(
                "§9Water Tower",
                "WATER_TOWER",
                Material.BREEZE_ROD,
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
        Location origin = p.getLocation().clone().add(0,5,0);
        p.getWorld().playSound(origin, Sound.BLOCK_CONDUIT_AMBIENT, 5.0F, 0.8F);
        p.getWorld().playSound(origin, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 2.0F, 0.8F);
        p.getWorld().playSound(origin, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 2.0F, 0.8F);
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if(task.isCancelled()) return; // just in case

            Collection<Entity> nearby = p.getWorld().getNearbyEntities(origin, 18, 18, 18, e -> !p.getUniqueId().equals(e.getUniqueId()) && e instanceof LivingEntity && !e.isDead() && e.getLocation().distanceSquared(origin) <= 18*18);
            Util.debug(p, "Found " + nearby.size() + " living entities within 18 blocks");
            int i = 0; // max 4 entities
            for(Entity e : nearby) {
                if(i >= 4) break;


                // ray trace
                Vector travelDir = e.getLocation().toVector().add(new Vector(0, 1, 0)).subtract(origin.toVector());//dont get feet
                //check if blocks are in the way
                double distanceToTarget = travelDir.length();
                RayTraceResult blocksInWay = p.getWorld().rayTraceBlocks(origin, travelDir.clone().normalize(), distanceToTarget);
                if(blocksInWay == null){
                    // draw line
                    Util.debug(p, "Raytraced entity was FOUND");
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 140, 255), 1.5F);
                    RenderUtil.renderDustLine(origin, travelDir, dustOptions);
                    RenderUtil.renderParticleHelix(origin, travelDir, 0.3, 60, Particle.BUBBLE, 0.05);
                    RenderUtil.renderParticleHelix(origin, travelDir, 0.3, 60, Particle.DRIPPING_WATER, 0.05);
                    p.getWorld().playSound(e.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 0.9F, 1.8F);
                    p.getWorld().playSound(origin, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 0.8F, 1.8F);
                    LivingEntity dmg = (LivingEntity) e;
                    if(dmg instanceof Player target) {
                        if(target.isBlocking()){
                            dmg.damage(damage*0.2, p);
                        } else {
                            dmg.damage(damage, p);
                        }
                    } else {
                        dmg.damage(damage*2, p);
                    }
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

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 140, 255), 1.5F);
            RenderUtil.renderParticleOrb(origin, 360, 1, Particle.DOLPHIN, 0.2);
            RenderUtil.renderDustOrb(origin, 60, 0.3, dustOptions);


            //dustOptions = new Particle.DustOptions(Color.fromRGB(0, 100, 255), 1F);
            Vector travelDirection = origin.clone().add(0, -5.5, 0).toVector().subtract(origin.toVector());
            Location newOrigin = origin.clone().subtract(0, 0.8, 0);
            RenderUtil.renderParticleCYL(newOrigin, travelDirection,6, 0.2, 3, Particle.DOLPHIN, 0);
            RenderUtil.renderDustLine(newOrigin, travelDirection, dustOptions);

            if(renderTimers.containsKey(p.getUniqueId())) {
                renderTimers.put(p.getUniqueId(), renderTimers.get(p.getUniqueId())+1);
            } else {
                renderTimers.put(p.getUniqueId(), 0L);
            }
            if(renderTimers.get(p.getUniqueId()) > 30) {
                renderTimers.remove(p.getUniqueId());
                task.cancel();
            }
        }, 0, 3);
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

}
