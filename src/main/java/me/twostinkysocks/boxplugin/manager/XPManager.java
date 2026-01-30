package me.twostinkysocks.boxplugin.manager;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.event.PlayerBoxXpUpdateEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentcrates.key.CrateKey;

import java.io.File;
import java.util.Random;

public class XPManager {
    public int getRandNumFromLimit(int limit){
        Random random = new Random();
        int randNum = random.nextInt(limit) + 1;
        return randNum;
    }

    public XPManager() {
        CrateKey legendaryKey = BoxPlugin.instance.getKeyManager().getKeyById("legendary_crate");
        CrateKey epicKey = BoxPlugin.instance.getKeyManager().getKeyById("epic");
        CrateKey rareKey = BoxPlugin.instance.getKeyManager().getKeyById("rare");
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            Bukkit.getOnlinePlayers().forEach(p -> {
                int beforexp = BoxPlugin.instance.getXpManager().getXP(p);
                BoxPlugin.instance.getXpManager().addXP(p, BoxPlugin.instance.getConfig().getInt("xp-equation-constant") * 10);
                int afterxp = BoxPlugin.instance.getXpManager().getXP(p);
                int randomKeyChance = getRandNumFromLimit(600);
                CrateKey key = BoxPlugin.instance.getKeyManager().getKeyById("common");
                if(randomKeyChance == 1){//1 out of 600
                    key = legendaryKey;
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', p.getName() + " &dJust got their free key upgraded to " + ChatColor.GOLD + ChatColor.BOLD + "Legendary!"));
                } else if (randomKeyChance <= 4) {//3 out of 600
                    key = epicKey;
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', p.getName() + " &dJust got their free key upgraded to " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Epic!"));
                } else if (randomKeyChance <= 12) {//12 out of 600
                    key = rareKey;
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', p.getName() + " &dJust got their free key upgraded to " + ChatColor.GOLD + "Rare!"));
                }
                if(key != null){
                    BoxPlugin.instance.getKeyManager().giveKey(p, key, 1);
                } else {
                    System.out.println("key not found");
                }
                Bukkit.getPluginManager().callEvent(new PlayerBoxXpUpdateEvent(p, beforexp, afterxp));
            });
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a&lFREE REWARDS! &7You earned " + BoxPlugin.instance.getConfig().getInt("xp-equation-constant") * 10 + " xp and 1 common key!"));
        }, 36000, 36000);
    }

    public int getXP(Player p) {
        return p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER);
    }

    public void setXP(Player p, int xp) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER, xp);
    }

    public void addXP(Player p, int xp) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "xp"), PersistentDataType.INTEGER, getXP(p) + xp);
    }

    public int getLevel(Player p) {
        int xp = getXP(p);
        return convertXPToLevel(xp);
    }

    public int convertXPToLevel(int xp) {
        return (int) (Math.sqrt(Math.abs(((double)xp)/BoxPlugin.instance.getConfig().getInt("xp-equation-constant")))+1);
    }

    public int convertLevelToXP(int level) {
        return (int) (BoxPlugin.instance.getConfig().getInt("xp-equation-constant") * Math.pow(level - 1, 2));
    }

    public int getNeededXp(Player p) {
        int xp = getXP(p);
        int level = convertXPToLevel(xp);
        int xpDiff = convertLevelToXP(level+1) - convertLevelToXP(level);
        int xpRemainder = xp - convertLevelToXP(level);
        return xpDiff - xpRemainder;
    }

    public int getLevelUpReward(int level) {
        int coins = level/2;
        if(coins > 192) coins = 192;
        return coins;
    }

    public int getLevelUpRewardLevelToLevel(int beforeLevel, int afterLevel) {
        int total = 0;
        for(int i = beforeLevel; i <= afterLevel; i++) {
            if(i%5==0) {
                total+=getLevelUpReward(i);
            }
        }
        return total;
    }

    public int getCumulativeLevelUpReward(int level) {
        int total = 0;
        for(int i = 1; i <= level; i++) {
            if(i%5==0) {
                total += getLevelUpReward(i);
            }
        }
        return total;
    }

    public void handleGroupUpdate(Player p, int beforelevel, int afterlevel) {
        User user = BoxPlugin.instance.getLuckPerms().getUserManager().getUser(p.getUniqueId());
        // add xp
        if(beforelevel < 15 && afterlevel >= 15) {
            InheritanceNode node = InheritanceNode.builder("lvl20").value(true).build();
            user.data().add(node);
        }
        if(beforelevel < 35 && afterlevel >= 35) {
            InheritanceNode node = InheritanceNode.builder("lvl35").value(true).build();
            user.data().add(node);
        }
        if(beforelevel < 50 && afterlevel >= 50) {
            InheritanceNode node = InheritanceNode.builder("lvl50").value(true).build();
            user.data().add(node);
        }
        if(beforelevel < 70 && afterlevel >= 70) {
            InheritanceNode node = InheritanceNode.builder("lvl70").value(true).build();
            user.data().add(node);
            p.sendMessage(ChatColor.GREEN + "You've reached level 70! You can claim your free 3-day trial of MVP+ at any time with /redeemtrialrank!");
        }
        if(beforelevel < 100 && afterlevel >= 100) {
            InheritanceNode node = InheritanceNode.builder("lvl100").value(true).build();
            user.data().add(node);
        }

        // remove xp
        if(afterlevel < 100 && beforelevel >= 100) {
            InheritanceNode node = InheritanceNode.builder("lvl100").value(false).build();
            user.data().add(node);
        }
        if(afterlevel < 70 && beforelevel >= 70) {
            InheritanceNode node = InheritanceNode.builder("lvl70").value(false).build();
            user.data().add(node);
        }
        if(afterlevel < 50 && beforelevel >= 50) {
            InheritanceNode node = InheritanceNode.builder("lvl50").value(false).build();
            user.data().add(node);
        }
        if(afterlevel < 35 && beforelevel >= 35) {
            InheritanceNode node = InheritanceNode.builder("lvl35").value(false).build();
            user.data().add(node);
        }
        if(afterlevel < 15 && beforelevel >= 15) {
            InheritanceNode node = InheritanceNode.builder("lvl20").value(false).build();
            user.data().add(node);
        }
        BoxPlugin.instance.getLuckPerms().getUserManager().saveUser(user);
    }

    public void updateXPBar(Player p) {
        int level = this.getLevel(p);
        float xpInLevel = this.convertLevelToXP(level + 1) - this.convertLevelToXP(level);
        float xpSoFar = xpInLevel - this.getNeededXp(p);
        p.setLevel(level);
        p.setExp(xpSoFar/xpInLevel);
    }

}
