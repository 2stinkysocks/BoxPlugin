package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class XanatosMenuManager {
    public void openGui(Player p) {
        ChestGui gui = new ChestGui(3, "Xanatos Menu");
        StaticPane pane = new StaticPane(9, 3);

        ItemStack ghostItems = new ItemStack(Material.ECHO_SHARD);
        ItemMeta ghostItemsMeta = ghostItems.getItemMeta();
        ghostItemsMeta.setDisplayName(ChatColor.DARK_AQUA + "Reclaim Ghost Items");
        ghostItems.setItemMeta(ghostItemsMeta);

        ItemStack reforge = new ItemStack(Material.ANVIL);
        ItemMeta reforgeMeta = reforge.getItemMeta();
        reforgeMeta.setDisplayName(ChatColor.GOLD + "Item Reforges");
        reforge.setItemMeta(reforgeMeta);

        ItemStack lives = new ItemStack(Material.RED_DYE);
        ItemMeta livesMeta = lives.getItemMeta();
        livesMeta.setDisplayName(ChatColor.RED + "Item Lives");
        lives.setItemMeta(livesMeta);

        gui.setOnGlobalClick(e -> {
            e.setCancelled(true);
        });

        GuiItem ghostGui = new GuiItem(ghostItems, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            BoxPlugin.instance.getGhostTokenManager().openGui(p);
        });

        GuiItem reforgeGui = new GuiItem(reforge, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            p.sendMessage(ChatColor.RED + "Coming Soon");
        });

        GuiItem livesGui = new GuiItem(lives, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            BoxPlugin.instance.getItemLivesManager().openGui(p);
        });

        pane.addItem(ghostGui, 2, 1);

        pane.addItem(reforgeGui, 4, 1);

        pane.addItem(livesGui, 6, 1);

        gui.addPane(pane);

        gui.copy().show(p);
    }
}
