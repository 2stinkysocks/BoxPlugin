package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        ItemStack downgrade = new ItemStack(Material.GRINDSTONE);
        ItemMeta downgradeMeta = downgrade.getItemMeta();
        downgradeMeta.setDisplayName(ChatColor.AQUA + "Item Downgrading");
        downgrade.setItemMeta(downgradeMeta);

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
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            BoxPlugin.instance.getReforgeManager().openGui(p);
        });

        GuiItem livesGui = new GuiItem(lives, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            BoxPlugin.instance.getItemLivesManager().openGui(p);
        });

        GuiItem downgradeGui = new GuiItem(downgrade, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openDowngradeGui(p);
        });

        pane.addItem(ghostGui, 2, 1);

        pane.addItem(reforgeGui, 4, 0);

        pane.addItem(livesGui, 6, 1);

        pane.addItem(downgradeGui, 4, 2);

        gui.addPane(pane);

        gui.copy().show(p);
    }

    public void openDowngradeGui(Player p){
        ChestGui gui = new ChestGui(3, "Downgrade Items");
        StaticPane pane = new StaticPane(9,3);


        gui.setOnClose(e -> {
            ArrayList<ItemStack> toAdd = new ArrayList<>();
            if(e.getView().getTopInventory().getItem(13) != null) {
                ItemStack item = e.getView().getTopInventory().getItem(13);
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                try {
                    if(BoxPlugin.instance.getItemPopperManager().isPopable(item)) {
                        lore.remove(lore.size()-1);
                        lore.remove(lore.size()-1);
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
                toAdd.add(item);
            }
            HashMap<Integer, ItemStack> toDrop = e.getPlayer().getInventory().addItem(toAdd.toArray(new ItemStack[toAdd.size()]));
            for(ItemStack stack : toDrop.values()) {
                Item itemEntity = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                itemEntity.setItemStack(stack);
            }
        });

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);

        ItemStack bars = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta barsMeta = bars.getItemMeta();
        barsMeta.setDisplayName(ChatColor.RESET + "");
        bars.setItemMeta(barsMeta);

        GuiItem confirmGui = new GuiItem(confirm.clone(), e -> {
            e.setCancelled(true);
            try {
                downgradeItem(e, p);
            } catch (SQLException | IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            BoxPlugin.instance.getXanatosMenuManager().openGui(p);
        });

        GuiItem barsGui = new GuiItem(bars.clone(), e -> {
            e.setCancelled(true);
        });



        gui.setOnGlobalClick(e -> {
            e.setCancelled(true);
            if(e.getSlot() == e.getRawSlot()) { // clicked in the chest
                if(e.getSlot() == 13) {
                    ItemStack item = e.getInventory().getItem(e.getSlot());
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.getLore();
                    try {
                        if(BoxPlugin.instance.getItemPopperManager().isPopable(item)) {
                            lore.remove(lore.size()-1);
                            lore.remove(lore.size()-1);
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    e.getInventory().setItem(e.getSlot(), item);
                    if(e.getWhoClicked().getInventory().addItem(e.getInventory().getItem(e.getSlot())).size() > 0) {
                        ItemStack currentItem = e.getInventory().getItem(e.getSlot());
                        ItemMeta currentItemMeta = e.getCurrentItem().getItemMeta();
                        List<String> currentLore = currentItemMeta.getLore();
                        int coins = GearScoreManager.GetGearScore(currentItem) * 10;
                        if(BoxPlugin.instance.getReforgeManager().hasReforges(currentItem)){
                            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                            p.sendMessage(ChatColor.RED + "You cant downgrade reforged items!");
                            return;
                        }
                        if(BoxPlugin.instance.getItemLivesManager().hasLives(currentItem)){
                            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                            p.sendMessage(ChatColor.RED + "You cant downgrade items with lives!");
                            return;
                        }
                        if(currentItem.getAmount() > 1){
                            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                            p.sendMessage(ChatColor.RED + "You cant multiple items at once!");
                            return;
                        }
                        lore.addAll(List.of("", ChatColor.GOLD + "" + ChatColor.BOLD + "Reclaim cost: " + coins + " Xanatos coins"));
                        currentItemMeta.setLore(currentLore);
                        currentItem.setItemMeta(currentItemMeta);
                        e.getInventory().setItem(e.getSlot(), item);
                        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                        p.sendMessage(ChatColor.RED + "Your inventory is full!");
                    } else {
                        e.getInventory().setItem(e.getSlot(), null);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                    }
                }
                return;
            }
            try {
                if(e.getCurrentItem() != null && BoxPlugin.instance.getItemPopperManager().isPopable(e.getCurrentItem())) {
                    if(e.getSlot() != e.getRawSlot()) { // not clicked in chest
                        if(e.getView().getTopInventory().getItem(13) != null) {
                            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                            p.sendMessage(ChatColor.RED + "You're already downgrading an item!");
                        } else {
                            ItemStack currentItem = e.getCurrentItem();
                            ItemMeta currentItemMeta = e.getCurrentItem().getItemMeta();
                            List<String> lore = currentItemMeta.getLore();
                            if(lore == null) lore = new ArrayList<>();
                            int coins = GearScoreManager.GetGearScore(currentItem) * 10;
                            if(BoxPlugin.instance.getReforgeManager().hasReforges(currentItem)){
                                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                                p.sendMessage(ChatColor.RED + "You cant downgrade reforged items!");
                                return;
                            }
                            if(BoxPlugin.instance.getItemLivesManager().hasLives(currentItem)){
                                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                                p.sendMessage(ChatColor.RED + "You cant downgrade items with lives!");
                                return;
                            }
                            lore.addAll(List.of("", ChatColor.GOLD + "" + ChatColor.BOLD + "Reclaim cost: " + coins + " Xanatos coins"));
                            currentItemMeta.setLore(lore);
                            currentItem.setItemMeta(currentItemMeta);
                            e.getView().getTopInventory().setItem(13, currentItem);
                            e.getClickedInventory().setItem(e.getSlot(), null);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);

                        }
                    }
                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                    p.sendMessage(ChatColor.RED + "That isn't a downgradeable item!");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                pane.addItem(confirmGui.copy(), i, j);
                pane.addItem(cancelGui.copy(), i+6,j);
                if(!(i == 1 && j == 1)) {
                    pane.addItem(barsGui.copy(), i+3, j);
                }
            }
        }
        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void downgradeItem(InventoryClickEvent e, Player p) throws SQLException, IOException, ClassNotFoundException {
        final int rawSlot = 13;
        ItemStack item = e.getView().getTopInventory().getItem(rawSlot);
        if(item != null && item.hasItemMeta() && BoxPlugin.instance.getItemPopperManager().isPopable(item)) {

            int coins = GearScoreManager.GetGearScore(item) * 10;

            if(BoxPlugin.instance.getMarketManager().getCoinsBalance(p) < coins) {
                p.sendMessage(ChatColor.RED + "You don't have enough money in your bank!");
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            } else {
                BoxPlugin.instance.getMarketManager().removeCoinsBalance(p, coins);
                BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
                e.getView().getTopInventory().setItem(rawSlot, BoxPlugin.instance.getItemPopperManager().getDowngradedItem(item));
                p.sendMessage(ChatColor.GREEN + "Successfully downgraded your item for " + coins + " Xanatos coins!");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                List<ItemStack> itemsToDrop = BoxPlugin.instance.getItemPopperManager().getItemsToDrop(item);
                for(ItemStack stack : itemsToDrop) {
                    Item itemEntity = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                    itemEntity.setItemStack(stack);
                }
            }
        }
    }
}
