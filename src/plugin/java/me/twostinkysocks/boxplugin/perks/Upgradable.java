package me.twostinkysocks.boxplugin.perks;

import org.bukkit.entity.Player;

public interface Upgradable {
    int getNextCost(int currentLevel);

    int getNextHexidiumCost(int currentLevel);

    int getNextTeraCost(int currentLevel);

    int getNextRemainderGigaCost(int currentLevel);

    int getLevel(Player p);

    int getMaxLevel();

    void setLevel(Player p, int level);

    /**
     *
     * @param p Player
     * @return if the purchase was successful
     */
    boolean upgrade(Player p);
}
