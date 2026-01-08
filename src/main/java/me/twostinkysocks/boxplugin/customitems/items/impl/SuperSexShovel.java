package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public class SuperSexShovel extends CustomItem {

    private HashMap<UUID, Long> cooldown;

    public SuperSexShovel(CustomItemsMain plugin) {
        super(
                "§1Super Sex Shovel",
                "SUPER_SEX_SHOVEL",
                Material.DIAMOND_SHOVEL,
                plugin,
                "",
                "§6Item Ability: Super Warp §e§lRIGHT CLICK",
                "§7Teleport forward 20 blocks on cast",
                "§8Cooldown: §a10s",
                "",
                "§8Can teleport through some ceilings"
        );
        cooldown = new HashMap<>();
        setClick((e, a) -> {
            Player p = e.getPlayer();
            if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(10000 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1))); // 10 seconds
                    List<Block> lineOfSight = new ArrayList<>(p.getLineOfSight(Set.of(Material.AIR, Material.CAVE_AIR, Material.WATER, Material.LAVA, Material.LIGHT), 20));
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
                        tpLocation = p.getLocation();
                    }
                    float pitch = p.getEyeLocation().getPitch();
                    float yaw = p.getEyeLocation().getYaw();
                    tpLocation.setYaw(yaw);
                    tpLocation.setPitch(pitch);
                    p.teleport(tpLocation);
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                } else {
                    BigDecimal bd = new BigDecimal(((double)(cooldown.get(p.getUniqueId()) - System.currentTimeMillis()))/1000.0);
                    bd = bd.round(new MathContext(2));
                    p.sendMessage(ChatColor.RED + "That's too fast! Wait " + bd.doubleValue() + " more seconds!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                }
            }
        });
    }
}
