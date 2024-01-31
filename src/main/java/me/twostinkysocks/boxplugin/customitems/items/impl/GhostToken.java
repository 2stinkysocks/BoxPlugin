package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

import java.util.HashMap;
import java.util.UUID;

public class GhostToken extends CustomItem {

    private HashMap<UUID, Integer> leftClicks;

    public GhostToken(CustomItemsMain plugin) {
        super(
                "§6Ghost Token",
                "GHOST_TOKEN",
                Material.MUSIC_DISC_5,
                plugin,
                ""
        );
        leftClicks = new HashMap<>();

        setClick((e, a) -> {
            if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {

            }
        });
    }
}
