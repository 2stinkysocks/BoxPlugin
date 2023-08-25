package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.UUID;

public class HealSpear extends CustomItem {

    private HashMap<UUID, Long> cooldown;

    public HealSpear(CustomItemsMain plugin) {
        super(
                "Heal Spear",
                "HEAL_SPEAR",
                Material.TRIDENT,
                plugin
        );
        cooldown = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(30000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                } else {
                    e.setCancelled(true);
                    BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            }
        });
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(World world : Bukkit.getWorlds()) {
                for(Entity entity : world.getEntities()) {
                    if(entity.getType() == EntityType.TRIDENT && entity.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "HEAL_SPEAR"), PersistentDataType.INTEGER)) {
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, entity.getLocation(), 10, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }
        }, 1L, 1L);
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
