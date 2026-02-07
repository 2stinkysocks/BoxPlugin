package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ItemLivesManager {

    public final NamespacedKey itemLivesKey = new NamespacedKey(BoxPlugin.instance, "lives"); // integer type

    public void openGui(Player p) {
        ChestGui gui = new ChestGui(6, "Item Lives");
        StaticPane pane = new StaticPane(9, 6);

        gui.setOnClose(e -> {
            if(e.getView().getTopInventory().getItem(13) != null) {
                HashMap<Integer, ItemStack> toDrop = e.getPlayer().getInventory().addItem(e.getView().getTopInventory().getItem(13));
                for(ItemStack stack : toDrop.values()) {
                    Item itemEntity = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                    itemEntity.setItemStack(stack);
                }
            }
        });

        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundMeta = background.getItemMeta();
        backgroundMeta.setDisplayName(ChatColor.RESET + "");
        background.setItemMeta(backgroundMeta);
        GuiItem backgroundGui = new GuiItem(background, e -> {
            e.setCancelled(true);
        });

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);
        pane.addItem(new GuiItem(cancel, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            BoxPlugin.instance.getXanatosMenuManager().openGui(p);
        }), 4, 5);

        ItemStack outline = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta outlineMeta = outline.getItemMeta();
        outlineMeta.setDisplayName(ChatColor.DARK_RED + "Item Lives");
        outlineMeta.setLore(List.of(ChatColor.RED + "Items can have a max of 10 lives", ChatColor.RED + "Lives cost 1.5 rubies each", ChatColor.RED + "" + ChatColor.BOLD + "Items with lives cannot be upgraded!"));
        outline.setItemMeta(outlineMeta);
        GuiItem outlineGui = new GuiItem(outline, e -> {
            e.setCancelled(true);
        });

        ItemStack plus2 = new ItemStack(Material.RED_DYE);
        plus2.setAmount(2);
        ItemMeta plus2Meta = plus2.getItemMeta();
        plus2Meta.setDisplayName(ChatColor.RED + "+2 Lives");
        plus2Meta.setLore(List.of(ChatColor.GREEN + "Costs 3 rubies"));
        plus2.setItemMeta(plus2Meta);
        GuiItem plus2Gui = new GuiItem(plus2, e -> {
            e.setCancelled(true);
            if(e.getView().getTopInventory().getItem(13) == null) {
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "No item in slot!");
                return;
            }
            boolean success = addLives(p, e.getView().getTopInventory().getItem(13), 2);
            if(!success) {
                p.sendMessage(ChatColor.RED + "Failed to add lives!");
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            } else {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                p.sendMessage(ChatColor.GREEN + "Added 2 lives to your item!");
            }
        });

        ItemStack plus10 = new ItemStack(Material.RED_DYE);
        plus10.setAmount(10);
        ItemMeta plus10Meta = plus10.getItemMeta();
        plus10Meta.setDisplayName(ChatColor.RED + "+10 Lives");
        plus10Meta.setLore(List.of(ChatColor.GREEN + "Costs 15 rubies"));
        plus10.setItemMeta(plus10Meta);
        GuiItem plus10Gui = new GuiItem(plus10, e -> {
            e.setCancelled(true);
            if(e.getView().getTopInventory().getItem(13) == null) {
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "No item in slot!");
                return;
            }
            boolean success = addLives(p, e.getView().getTopInventory().getItem(13), 10);
            if(!success) {
                p.sendMessage(ChatColor.RED + "Failed to add lives!");
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            } else {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                p.sendMessage(ChatColor.GREEN + "Added 10 lives to your item!");
            }
        });

        ItemStack strip = new ItemStack(Material.REDSTONE);
        ItemMeta stripMeta = strip.getItemMeta();
        stripMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Remove all lives");
        stripMeta.setLore(List.of(ChatColor.RED + "Remove lives and get refunded either", ChatColor.RED + "80% or 100% of the rubies."));
        strip.setItemMeta(stripMeta);
        GuiItem stripGui = new GuiItem(strip, e -> {
            e.setCancelled(true);
            if(e.getView().getTopInventory().getItem(13) == null) {
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "No item in slot!");
                return;
            }
            if(!hasLives(e.getView().getTopInventory().getItem(13))){
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "This item does not have lives!");
                return;
            }
            int stripped = stripLives(e.getView().getTopInventory().getItem(13));
            Random luck = new Random();
            int randLuck = luck.nextInt(10);
            double rubyMult = 1;
            if(randLuck <= 4){//50% chance to lose 20% of rubies
                rubyMult = 0.8;
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Xanatos: OOPS! I messed up a bit, lives are tricky to work with...");
            }

            BoxPlugin.instance.getMarketManager().addRubies(p, (int) (stripped*1.5*rubyMult));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);

            if(rubyMult == 1){
                p.sendMessage(ChatColor.GREEN + "Stripped " + stripped + " lives from your item and gained all rubies back (" + (int) (stripped*1.5*rubyMult) + " rubies)!");
            } else {
                p.sendMessage(ChatColor.GREEN + "Stripped " + stripped + " lives from your item and returned 80% of rubies (" + (int) (stripped*1.5*rubyMult) + " rubies)!");
            }
        });

        for(int i = 3; i < 6; i++) {
            for(int j = 0; j < 3; j++) {
                if(i != 4 || j != 1) {
                    pane.addItem(outlineGui, i, j);
                }
            }
        }

        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 6; j++) {
                if((i == 4 && j == 1) || (i >= 3 && i <= 5 && j == 0) || (i == 3 && j >= 0 && j <= 2) || (i == 5 && j >= 0 && j <= 2) || (j == 2 && i >= 3 && i <= 5) || (i == 2 && j == 3) || (i == 4 && j == 3) || (i == 6 && j == 3) || (i == 4 && j == 5)) {
                    continue;
                }
                pane.addItem(backgroundGui, i, j);
            }
        }

        pane.addItem(plus2Gui, 2, 3);
        pane.addItem(plus10Gui, 6, 3);
        pane.addItem(stripGui, 4, 3);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    /**
     * Add lives to an item
     * @param p The player to charge rubies to
     * @param item The item to add lives to
     * @param amount Lives to add
     * @return if it was successful (will deduct currency)
     */
    public boolean addLives(Player p, ItemStack item, int amount) {
        if(item == null) return false;
        if(BoxPlugin.instance.getGhostTokenManager().isGhostItem(item)) return false;
        if(Util.isSoulbound(item)) return false;
        if(amount < 1) return false;
        if(amount > 10) return false;

        if(item.getAmount() > 1){
            p.sendMessage(ChatColor.RED + "You cannot put lives on stacked items!");
            return false;
        }
        int initialLives = 0;
        if(hasLives(item)) {
            initialLives = getLives(item);
        }
        // can only put 10 lives on an item that has no lives
        if(amount == 10 && initialLives > 0) return false;
        // maximum of 10 lives
        if(initialLives + amount > 10) return false;
        int cost = (int) (amount * 1.5);
        if(BoxPlugin.instance.getMarketManager().getRubies(p) < cost) {
            return false;
        }
        ItemMeta meta =  item.getItemMeta();
        meta.getPersistentDataContainer().set(itemLivesKey, PersistentDataType.INTEGER, initialLives+amount);
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if(hasLives(item)) {
            lore.set(lore.size()-1, ChatColor.RED + "" + (initialLives+amount) + " Lives");
        } else {
            lore.addAll(List.of("", ChatColor.RED + "" + (initialLives+amount) + " Lives"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        BoxPlugin.instance.getMarketManager().setRubies(p, BoxPlugin.instance.getMarketManager().getRubies(p)-(cost));
        BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
        return true;
    }

    public void setLives(ItemStack item, int amount){
        if(item == null) return;
        if(BoxPlugin.instance.getGhostTokenManager().isGhostItem(item)) return;
        if(Util.isSoulbound(item)) return;
        if(amount < 1) return;
        if(amount > 10) return;

        ItemMeta meta =  item.getItemMeta();
        meta.getPersistentDataContainer().set(itemLivesKey, PersistentDataType.INTEGER, amount);
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if(hasLives(item)) {
            lore.set(lore.size()-1, ChatColor.RED + "" + (amount) + " Lives");
        } else {
            lore.addAll(List.of("", ChatColor.RED + "" + (amount) + " Lives"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     *
     * @param item The item to remove lives from
     * @return if the item still has more lives
     */
    public boolean decrementLives(ItemStack item) {
        if(!hasLives(item)) return false;
        int lives = getLives(item);
        ItemMeta meta = item.getItemMeta();
        if(lives > 1) {
            meta.getPersistentDataContainer().set(itemLivesKey, PersistentDataType.INTEGER, lives-1);
            List<String> lore = meta.getLore();
            lore.set(lore.size()-1, ChatColor.RED + "" + (lives-1) + " Lives");
            meta.setLore(lore);
            item.setItemMeta(meta);
            return true;
        } else {
            stripLives(item);
            return false;
        }

    }

    public boolean hasLives(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(itemLivesKey, PersistentDataType.INTEGER);
    }

    public int getLives(ItemStack item) {
        if(!hasLives(item)) return 0;
        return item.getItemMeta().getPersistentDataContainer().get(itemLivesKey, PersistentDataType.INTEGER);
    }

    /**
     * Note: this function does not refund rubies
     * @return amount of lives stripped
     */
    public int stripLives(ItemStack item) {
        if(item == null || !item.hasItemMeta()) return 0;
        int lives = hasLives(item) ? getLives(item) : 0;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.remove(lore.size()-1);
        lore.remove(lore.size()-1);
        meta.setLore(lore);
        meta.getPersistentDataContainer().remove(itemLivesKey);
        item.setItemMeta(meta);
        return lives;
    }

}
