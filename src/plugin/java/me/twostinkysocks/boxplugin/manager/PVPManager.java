package me.twostinkysocks.boxplugin.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class PVPManager {

    private HashMap<UUID, Integer> streaks;


    public PVPManager() {
        streaks = new HashMap<UUID, Integer>();
    }

    public void registerKill(Player cause, Player target) {
        // add to streak
        if(streaks.containsKey(cause.getUniqueId())) {
            streaks.put(cause.getUniqueId(), streaks.get(cause.getUniqueId()) + 1);
        } else {
            streaks.put(cause.getUniqueId(), 1);
        }
            if(getStreak(cause) >= 20) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
            }
            if(getStreak(cause) >= 40) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0, true, false));
            }
            if(getStreak(cause) >= 60) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0, true, false));
            }
            if(getStreak(cause) >= 80) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1, true, false));
            }
            if(getStreak(cause) >= 100) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 2, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1, true, false));
            }
            if(getStreak(cause) >= 120) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 2, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 2, true, false));
            }
            if(getStreak(cause) >= 140) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 2, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 3, true, false));
            }
            if(getStreak(cause) >= 160) {
                cause.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 2, true, false));
                cause.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 4, true, false));
            }
            if(getStreak(cause) % 10 == 0) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c&lBounty! &7" + cause.getName() + " is on a " + getStreak(cause) + " kill streak!"));
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.2f, 2f);
                }
            }
        // claim and reset streak
        resetStreak(target);
    }

    public int getStreak(Player p) {
        return streaks.containsKey(p.getUniqueId()) ? streaks.get(p.getUniqueId()) : 0;
    }

    public void resetStreak(Player p) {
        streaks.put(p.getUniqueId(), 0);
    }

    public boolean hasStreak(Player p) {
        return streaks.containsKey(p.getUniqueId()) && streaks.get(p.getUniqueId()) > 0;
    }

    // num of skulls
    public int getBounty(Player p) {
        return calculateBounty(getStreak(p));
    }

    public int calculateBounty(int streak) {
        return streak >= 10 ? streak : 1;
    }

}
