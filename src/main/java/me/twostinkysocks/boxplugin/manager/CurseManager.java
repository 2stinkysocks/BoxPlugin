package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.RenderUtil;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CurseManager {
    private final NamespacedKey isCursed = new NamespacedKey(BoxPlugin.instance, "isCursed");
    private final NamespacedKey soulValueKey = new NamespacedKey(BoxPlugin.instance, "soulValue");
    private final NamespacedKey soulOwnerKey = new NamespacedKey(BoxPlugin.instance, "soulOwner");

    public void removePerksExtraPerks(Player p) {
        if(BoxPlugin.instance.getXpManager().getLevel(p) < 50){//remove all perks
            for(PerksManager.Perk perk : BoxPlugin.instance.getPerksManager().getSelectedPerks(p)) {
                if(perk != null) {
                    perk.instance.onUnequip(p);
                }
            }
            p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "selected_perks"));
        } else {//let them keep one
            List<PerksManager.Perk> currentPerks = BoxPlugin.instance.getPerksManager().getSelectedPerks(p);
            for(PerksManager.Perk perk : currentPerks) {
                if(perk != null && (currentPerks.size() != 1)) {
                    perk.instance.onUnequip(p);
                    currentPerks.remove(currentPerks.size()-1);
                }
            }
            BoxPlugin.instance.getPerksManager().setSelectedPerks(p, currentPerks);
            for(PerksManager.MegaPerk megaPerk : BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p)) {
                if(megaPerk != null) {
                    megaPerk.instance.onUnequip(p);
                }
            }
        }
        p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "selected_megaperks"));
    }

    public ItemStack prepareSoul(Player p){
        int gearScore = GearScoreManager.GetPlayerGearscore(p);
        String UUID = p.getUniqueId().toString();
        int soulValue = (int) (gearScore * (gearScore * 0.07));
        ItemStack soul = Util.soulOfPlayer(p.getName());
        ItemMeta soulMeta = soul.getItemMeta();
        p.getPersistentDataContainer().set(soulValueKey, PersistentDataType.INTEGER, soulValue); //store cost to remove curse on player
        soulMeta.getPersistentDataContainer().set(soulValueKey, PersistentDataType.INTEGER, soulValue);
        soulMeta.getPersistentDataContainer().set(soulOwnerKey, PersistentDataType.STRING, UUID);

        List<String> lore = soulMeta.hasLore() ? soulMeta.getLore() : new ArrayList<>();
        lore.addAll(List.of(
                "",
                ChatColor.LIGHT_PURPLE + "This soul is worth: " + ChatColor.GOLD + "$" + soulValue
        ));
        soulMeta.setLore(lore);

        soul.setItemMeta(soulMeta);
        return soul;
    }

    public void spawnSoulBeaconLine(Location origin){
        Vector travelDirection = origin.clone().add(0, 2, 0).toVector().subtract(origin.toVector());
        RenderUtil.renderParticleLine(origin, travelDirection, Particle.END_ROD, 0);
    }

    public void setCurse(Player p, Location deathLoc) {
        if (hasCurse(p)) {
            return;
        }
        p.getPersistentDataContainer().set(isCursed, PersistentDataType.BOOLEAN, true);
        removePerksExtraPerks(p);

        Item soulItem = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
        soulItem.setItemStack(prepareSoul(p));

        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, task -> {
            if (!soulItem.isValid() || soulItem.isDead()) {
                task.cancel();
                return;
            }

            spawnSoulBeaconLine(soulItem.getLocation());
        }, 5L, 20L);
        p.resetTitle();
        p.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "You've been CURSED", ChatColor.GRAY + "", 10, 40, 10);
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.4f, 1f);
        p.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "CURSED" + ChatColor.RESET + ChatColor.RED + " for losing your soul to the natural world, " +
                "your soul was dropped at " + ChatColor.GREEN + (int) deathLoc.getX() + " " + (int) deathLoc.getY() + " " + (int) deathLoc.getZ() + ChatColor.RED + " vist " +
                ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Aurora " + ChatColor.RESET + ChatColor.RED + "in the witch hut to lift the curse! use /curses for more info");
    }

    public void removeCurse(Player p) {
        if (!hasCurse(p)) {
            return;
        }
        p.sendMessage(ChatColor.AQUA + "The curse has been lifted!");
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        p.getPersistentDataContainer().remove(isCursed);
        p.getPersistentDataContainer().remove(soulValueKey);

    }

    public boolean hasCurse(Player p) {
        return p.getPersistentDataContainer().getOrDefault(isCursed, PersistentDataType.BOOLEAN, false);
    }

    public int getPlayerCurseValue(Player p){
        return p.getPersistentDataContainer().getOrDefault(soulValueKey, PersistentDataType.INTEGER, 0);
    }

    public int getSoulValue(ItemStack soul){
        return soul.getItemMeta().getPersistentDataContainer().getOrDefault(soulValueKey, PersistentDataType.INTEGER, 0);
    }

    public boolean isValidSoul(ItemStack item){
        ItemMeta soulMeta = item.getItemMeta();
        if(soulMeta.getPersistentDataContainer().has(soulValueKey) && soulMeta.getPersistentDataContainer().has(soulOwnerKey)){
            return true;
        }
        return false;
    }

    public boolean soulMatchesPlayer(ItemStack item, Player p){
        if(isValidSoul(item)){
            ItemMeta soulMeta = item.getItemMeta();
            String UUID = soulMeta.getPersistentDataContainer().getOrDefault(soulOwnerKey, PersistentDataType.STRING, null);
            int soulValueItem = soulMeta.getPersistentDataContainer().getOrDefault(soulValueKey, PersistentDataType.INTEGER, 0);
            int soulValuePlayer = p.getPersistentDataContainer().getOrDefault(soulValueKey, PersistentDataType.INTEGER, 0);
            if((UUID.equals(p.getUniqueId().toString())) && (soulValuePlayer == soulValueItem)){
                return true;
            }
        }
        return false;
    }

    public void reclaimSoul(Player p){
        if(!hasCurse(p)){
            p.sendMessage(ChatColor.RED + "You are not cursed");
            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
            return;
        }
        for(int i = 0; i < p.getInventory().getSize(); i++) {
            if(p.getInventory().getItem(i) != null) {
                ItemStack item = p.getInventory().getItem(i);
                if(soulMatchesPlayer(item, p)) {
                    p.getInventory().setItem(i, null);
                    removeCurse(p);
                    return;
                }
            }
        }
        p.sendMessage(ChatColor.RED + "You don't have a valid soul in your inventory");
        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
    }

    public void purchaseSoul(Player p){
        if(!hasCurse(p)){
            p.sendMessage(ChatColor.RED + "You are not cursed");
            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
            return;
        }
        int soulCost = getPlayerCurseValue(p);
        if(BoxPlugin.instance.getMarketManager().getCoinsBalance(p) < soulCost) {
            p.sendMessage(ChatColor.RED + "You don't have enough money in your bank!");
            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
        } else {
            BoxPlugin.instance.getMarketManager().removeCoinsBalance(p, soulCost);
            BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
            removeCurse(p);
        }
    }

    public void sellSoul(Player p){
        for(int i = 0; i < p.getInventory().getSize(); i++) {
            if(p.getInventory().getItem(i) != null) {
                ItemStack item = p.getInventory().getItem(i);
                if(soulMatchesPlayer(item, p)) {
                    p.sendMessage( ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Aurora: " + ChatColor.RESET + ChatColor.RED + "You cant sell your own soul!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                    return;
                } else if (isValidSoul(item) && !soulMatchesPlayer(item, p)) {
                    p.sendMessage( ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Aurora: " + ChatColor.RESET + ChatColor.AQUA +
                            "Hmm... this soul isnt yours but ill give you " + ChatColor.GOLD + ((int) (getSoulValue(item) * 0.75)) + ChatColor.AQUA + " coins for it!");
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                    p.getInventory().setItem(i, null);
                    int amount = (int) (getSoulValue(item) * 0.75);
                    int hexidium = amount / 262144;
                    amount = amount % 262144;
                    int teracube = amount / 4096;
                    amount = amount % 4096;
                    int gigaCoin = amount / 64;
                    amount = amount % 64;
                    int coin = amount;
                    HashMap<Integer, ItemStack> h = p.getInventory().addItem(Util.itemArray(hexidium, Util::hexidium));
                    HashMap<Integer, ItemStack> t = p.getInventory().addItem(Util.itemArray(teracube, Util::teraCube));
                    HashMap<Integer, ItemStack> g = p.getInventory().addItem(Util.itemArray(gigaCoin, Util::gigaCoin));
                    HashMap<Integer, ItemStack> x = p.getInventory().addItem(Util.itemArray(coin, Util::coin));
                    for(ItemStack moneyItem : h.values()) {
                        Item itm = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                        itm.setItemStack(moneyItem);
                    }
                    for(ItemStack moneyItem : t.values()) {
                        Item itm = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                        itm.setItemStack(moneyItem);
                    }
                    for(ItemStack moneyItem : g.values()) {
                        Item itm = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                        itm.setItemStack(moneyItem);
                    }
                    for(ItemStack moneyItem : x.values()) {
                        Item itm = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.ITEM);
                        itm.setItemStack(moneyItem);
                    }
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                    if (amount == 0){
                        p.sendMessage(ChatColor.RED + "This soul was worthless.");
                    }
                    else {
                        p.sendMessage(ChatColor.GREEN + "Gained " + amount + " coins");
                    }
                    return;
                }
            }
        }
        p.sendMessage(ChatColor.RED + "You don't have a soul on you");
        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
    }

    public void openSoulGui(Player p) {
        ChestGui gui = new ChestGui(3, "Soul Sanctum");
        StaticPane pane = new StaticPane(9,3);
        gui.setOnGlobalClick(e -> e.setCancelled(true));

        // set up items in gui

        ItemStack buySoul = new ItemStack(Material.FLOW_BANNER_PATTERN);
        ItemMeta buySoulMeta = buySoul.getItemMeta();
        buySoulMeta.setDisplayName(ChatColor.GOLD + "Purchase your soul");
        buySoulMeta.setLore(List.of(
                ChatColor.LIGHT_PURPLE + "Lift the curse and reclaim",
                ChatColor.LIGHT_PURPLE + "your soul for: " + ChatColor.GOLD + "$" + getPlayerCurseValue(p),
                ""
        ));

        buySoul.setItemMeta(buySoulMeta);


        ItemStack sellOthersSoul = Util.soulItem();
        ItemMeta sellOtherSoulMeta = sellOthersSoul.getItemMeta();
        sellOtherSoulMeta.setDisplayName("§x§8§3§4§3§F§F§l" + "Sell another's Soul");
        sellOtherSoulMeta.setLore(List.of(
                ChatColor.LIGHT_PURPLE + "Sell the soul of another for",
                ChatColor.LIGHT_PURPLE + "75% of its value",
                ""
        ));
        sellOthersSoul.setItemMeta(sellOtherSoulMeta);

        //icon for progression pillars
        ItemStack reclaimSoul = Util.soulItem();
        ItemMeta reclaimSoulMeta = reclaimSoul.getItemMeta();
        reclaimSoulMeta.setDisplayName("§x§8§3§4§3§F§F§l" + "Reclaim your Soul");
        reclaimSoulMeta.setLore(List.of(
                ChatColor.LIGHT_PURPLE + "Find your soul to lift the curse",
                ""
        ));
        reclaimSoul.setItemMeta(reclaimSoulMeta);

        // gui items
        GuiItem guiBuySoul = new GuiItem(buySoul, e -> {
            e.setCancelled(true);
            purchaseSoul(p);
        });
        GuiItem guiReclaimSoul = new GuiItem(reclaimSoul, e -> {
            e.setCancelled(true);
            reclaimSoul(p);
        });
        GuiItem guiSellSoul = new GuiItem(sellOthersSoul, e -> {
            e.setCancelled(true);
            sellSoul(p);
        });

        pane.addItem(guiBuySoul, 2, 1);
        pane.addItem(guiSellSoul, 6, 1);
        pane.addItem(guiReclaimSoul, 4, 1);
        gui.addPane(pane);
        gui.copy().show(p);
    }
}
