package me.twostinkysocks.boxplugin.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBoxXpUpdateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private int beforeXP, afterXP;
    private Player player;
    private boolean bypassMultiplier;

    public PlayerBoxXpUpdateEvent(Player player, int beforeXP, int afterXP) {
        this.beforeXP = beforeXP;
        this.afterXP = afterXP;
        this.player = player;
        this.bypassMultiplier = false;
    }

    public PlayerBoxXpUpdateEvent(Player player, int beforeXP, int afterXP, boolean bypassMultiplier) {
        this.beforeXP = beforeXP;
        this.afterXP = afterXP;
        this.player = player;
        this.bypassMultiplier = bypassMultiplier;
    }

    public int getBeforeXP() {
        return beforeXP;
    }

    public Player getPlayer() {
        return player;
    }

    public int getAfterXP() {
        return afterXP;
    }

    public boolean isMultiplierBypassed() {
        return bypassMultiplier;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
