package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.rapha149.signgui.SignGUI;
import io.github.rapha149.signgui.SignGUIAction;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.*;

public class MarketManager {

    private HashMap<UUID, Integer> selectedCoins = new HashMap<>();

    public void openGui(Player p) {
        ChestGui gui = new ChestGui(3, "Market");
        StaticPane pane = new StaticPane(9,3);
        gui.setOnGlobalClick(e -> e.setCancelled(true));

        selectedCoins.put(p.getUniqueId(), 0);

        // set up items in gui
        ItemStack market = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta marketmeta = market.getItemMeta();
        marketmeta.setDisplayName(ChatColor.AQUA + "Current Market Data");
        double multiplier = getMarketMultiplier();
        if(multiplier >= 1) {
            marketmeta.setLore(List.of(
                    "",
                    ChatColor.GREEN + "The current market multipler is " + ChatColor.BOLD + multiplier,
                    ""
            ));
        } else {
            marketmeta.setLore(List.of(
                    "",
                    ChatColor.RED + "The current market multiplier is " + ChatColor.BOLD + multiplier,
                    ""
            ));
        }
        market.setItemMeta(marketmeta);


        ItemStack deposit = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta depositmeta = deposit.getItemMeta();
        depositmeta.setDisplayName(ChatColor.GREEN + "Deposit");
        if(getMarketMultiplier() > 1) {
            depositmeta.setLore(List.of(
                    ChatColor.RED + "" + ChatColor.BOLD + "WARNING! Depositing coins",
                    ChatColor.RED + "" + ChatColor.BOLD + "when the market multiplier is over 1 will",
                    ChatColor.RED + "" + ChatColor.BOLD + "deduct a percentage of your deposit!",
                    ChatColor.GRAY + "Deposit xanatos coins into your account",
                    ""
            ));
        } else {
            depositmeta.setLore(List.of(
                    "",
                    ChatColor.GRAY + "Deposit xanatos coins into your account",
                    ""
            ));
        }

        deposit.setItemMeta(depositmeta);


        ItemStack withdraw = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta withdrawmeta = withdraw.getItemMeta();
        withdrawmeta.setDisplayName(ChatColor.RED + "Withdraw");
        if(getMarketMultiplier() < 1) {
            withdrawmeta.setLore(List.of(
                    ChatColor.RED + "" + ChatColor.BOLD + "WARNING! Withdrawing coins",
                    ChatColor.RED + "" + ChatColor.BOLD + "when the market multiplier is under 1 will",
                    ChatColor.RED + "" + ChatColor.BOLD + "deduct a percentage of your withdrawl!",
                    ChatColor.GRAY + "Withdraw xanatos coins from your account",
                    ""
            ));
        } else {
            withdrawmeta.setLore(List.of(
                    "",
                    ChatColor.GRAY + "Withdraw xanatos coins from your account",
                    ""
            ));
        }

        withdraw.setItemMeta(withdrawmeta);


        ItemStack balance = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta balancemeta = (SkullMeta) balance.getItemMeta();
        balancemeta.setDisplayName(ChatColor.AQUA + "Your Balance");
        balancemeta.setLore(List.of(
                "",
                ChatColor.GRAY + "You have " + ChatColor.GOLD + String.format("%.0f",BoxPlugin.instance.getEconomy().getBalance(p)) + ChatColor.GRAY +  " Xanatos coins in your account",
                ""
        ));
        balancemeta.setOwnerProfile(p.getPlayerProfile());
        balance.setItemMeta(balancemeta);

        ItemStack rubies = Util.getSkull("http://textures.minecraft.net/texture/2530191500c2453624dd937ec125d44f0942cc2b664073e2a366b3fa67a0c897");
        SkullMeta rubiesMeta = (SkullMeta) rubies.getItemMeta();
        rubiesMeta.setDisplayName(ChatColor.RED + "Buy and Deposit Rubies");
        rubiesMeta.setLore(List.of(
                "",
                ChatColor.GRAY + "You have " + ChatColor.RED + getRubies(p) + " rubies",
                ""
        ));
        rubies.setItemMeta(rubiesMeta);

        // gui items
        GuiItem guiMarket = new GuiItem(market, e -> e.setCancelled(true));
        GuiItem guiDeposit = new GuiItem(deposit, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openDepositMenu(p);
        });
        GuiItem guiWithdraw = new GuiItem(withdraw, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openWithDrawMenu(p);
        });
        GuiItem guiBalance = new GuiItem(balance, e -> e.setCancelled(true));
        GuiItem guiRubies = new GuiItem(rubies, e -> {
            e.setCancelled(true);
            openRubyGui(p);
        });

        pane.addItem(guiMarket, 4, 0);
        pane.addItem(guiDeposit, 2, 1);
        pane.addItem(guiWithdraw, 6, 1);
        pane.addItem(guiBalance, 0, 2);
        pane.addItem(guiRubies, 8, 2);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openDepositMenu(Player p) {
        ChestGui gui = new ChestGui(3, "Deposit Coins");
        StaticPane pane = new StaticPane(9,3);

        // 3 4 5 12 13 14 21 22 23
        gui.setOnClose(e -> {
            ArrayList<ItemStack> toAdd = new ArrayList<>();
            if(e.getView().getTopInventory().getItem(3) != null) toAdd.add(e.getView().getTopInventory().getItem(3));
            if(e.getView().getTopInventory().getItem(4) != null) toAdd.add(e.getView().getTopInventory().getItem(4));
            if(e.getView().getTopInventory().getItem(5) != null) toAdd.add(e.getView().getTopInventory().getItem(5));
            if(e.getView().getTopInventory().getItem(12) != null) toAdd.add(e.getView().getTopInventory().getItem(12));
            if(e.getView().getTopInventory().getItem(13) != null) toAdd.add(e.getView().getTopInventory().getItem(13));
            if(e.getView().getTopInventory().getItem(14) != null) toAdd.add(e.getView().getTopInventory().getItem(14));
            if(e.getView().getTopInventory().getItem(21) != null) toAdd.add(e.getView().getTopInventory().getItem(21));
            if(e.getView().getTopInventory().getItem(22) != null) toAdd.add(e.getView().getTopInventory().getItem(22));
            if(e.getView().getTopInventory().getItem(23) != null) toAdd.add(e.getView().getTopInventory().getItem(23));
            HashMap<Integer, ItemStack> toDrop = e.getPlayer().getInventory().addItem(toAdd.toArray(new ItemStack[toAdd.size()]));
            for(ItemStack stack : toDrop.values()) {
                Item itemEntity = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
                itemEntity.setItemStack(stack);
            }
        });

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        if(getMarketMultiplier() > 1) {
            confirmMeta.setLore(List.of(
                    ChatColor.RED + "" + ChatColor.BOLD + "WARNING! Depositing coins",
                    ChatColor.RED + "" + ChatColor.BOLD + "when the market multiplier is over 1 will",
                    ChatColor.RED + "" + ChatColor.BOLD + "deduct a percentage of your deposit!"
            ));
        }
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);

        GuiItem confirmGui = new GuiItem(confirm.clone(), e -> {
            e.setCancelled(true);
            confirmDeposit(e, p);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            cancelDeposit(e, p);
        });

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                pane.addItem(confirmGui.copy(), i, j);
                pane.addItem(cancelGui.copy(), i+6,j);
            }
        }
        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openWithDrawMenu(Player p) {
        ChestGui gui = new ChestGui(3, "Withdraw Coins");
        StaticPane pane = new StaticPane(9,3);

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirmMeta.setLore(List.of(
                ChatColor.GOLD  + "Withdraw " + selectedCoins.get(p.getUniqueId()) + " coins"));
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);

        ItemStack plus16 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta plus16Meta = plus16.getItemMeta();
        plus16Meta.setDisplayName(ChatColor.GREEN + "+16");
        plus16.setItemMeta(plus16Meta);

        ItemStack plus64 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta plus64Meta = plus64.getItemMeta();
        plus64Meta.setDisplayName(ChatColor.GREEN + "+64");
        plus64Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "1 Giga Coin"));
        plus64.setItemMeta(plus64Meta);

        ItemStack plus640 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta plus640Meta = plus640.getItemMeta();
        plus640Meta.setDisplayName(ChatColor.GREEN + "+640");
        plus640Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "10 Giga Coins"));
        plus640.setItemMeta(plus640Meta);

        ItemStack plus2048 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta plus2048Meta = plus2048.getItemMeta();
        plus2048Meta.setDisplayName(ChatColor.GREEN + "+2048");
        plus2048Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "32 Giga Coin "));
        plus2048.setItemMeta(plus2048Meta);

        ItemStack plus4096 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta plus4096Meta = plus4096.getItemMeta();
        plus4096Meta.setDisplayName(ChatColor.GREEN + "+4096");
        plus4096Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "64 Giga Coin "));
        plus4096.setItemMeta(plus4096Meta);

        ItemStack takeAll = new ItemStack(Material.GOLD_INGOT);
        ItemMeta takeAllMeta = takeAll.getItemMeta();
        takeAllMeta.setDisplayName(ChatColor.GOLD + "Withdraw all");
        takeAll.setItemMeta(takeAllMeta);

        ItemStack typeVal = new ItemStack(Material.OAK_SIGN);
        ItemMeta typeValMeta = typeVal.getItemMeta();
        typeValMeta.setDisplayName(ChatColor.GOLD + "Type Value");
        typeVal.setItemMeta(typeValMeta);

        ItemStack minus16 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta minus16Meta = minus16.getItemMeta();
        minus16Meta.setDisplayName(ChatColor.RED + "-16");
        minus16.setItemMeta(minus16Meta);

        ItemStack minus64 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta minus64Meta = minus64.getItemMeta();
        minus64Meta.setDisplayName(ChatColor.RED + "-64");
        minus64Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "1 Giga Coin"));
        minus64.setItemMeta(minus64Meta);

        ItemStack minus640 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta minus640Meta = minus640.getItemMeta();
        minus640Meta.setDisplayName(ChatColor.RED + "-640");
        minus640Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "10 Giga Coins"));
        minus640.setItemMeta(minus640Meta);

        ItemStack minus2048 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta minus2048Meta = minus2048.getItemMeta();
        minus2048Meta.setDisplayName(ChatColor.RED + "-2048");
        minus2048Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "32 Giga Coins"));
        minus2048.setItemMeta(minus2048Meta);

        ItemStack minus4096 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta minus4096Meta = minus4096.getItemMeta();
        minus4096Meta.setDisplayName(ChatColor.RED + "-4096");
        minus4096Meta.setLore(List.of(
                ChatColor.GOLD  + "" + ChatColor.BOLD + "64 Giga Coins"));
        minus4096.setItemMeta(minus4096Meta);

        ItemStack balance = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta balancemeta = (SkullMeta) balance.getItemMeta();
        balancemeta.setDisplayName(ChatColor.AQUA + "Your Balance");
        balancemeta.setLore(List.of(
                "",
                ChatColor.GRAY + "You have " + ChatColor.GOLD + String.format("%.0f",BoxPlugin.instance.getEconomy().getBalance(p)) + ChatColor.GRAY +  " Xanatos coins in your account",
                ""
        ));
        balancemeta.setOwnerProfile(p.getPlayerProfile());
        balance.setItemMeta(balancemeta);

        GuiItem confirmGui = new GuiItem(confirm.clone(), e -> {
            e.setCancelled(true);
            confrimWithdraw(e, p);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            cancelDeposit(e, p);
        });

        GuiItem plus16Gui = new GuiItem(plus16.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, 16);
        });

        GuiItem plus64Gui = new GuiItem(plus64.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, 64);
        });

        GuiItem plus640Gui = new GuiItem(plus640.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, 640);
        });

        GuiItem plus2048Gui = new GuiItem(plus2048.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, 2048);
        });

        GuiItem plus4096Gui = new GuiItem(plus4096.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, 4096);
        });

        GuiItem minus16Gui = new GuiItem(minus16.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, -16);
        });

        GuiItem minus64Gui = new GuiItem(minus64.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, -64);
        });

        GuiItem minus640Gui = new GuiItem(minus640.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, -640);
        });

        GuiItem minus2048Gui = new GuiItem(minus2048.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, -2048);
        });

        GuiItem minus4096Gui = new GuiItem(minus4096.clone(), e -> {
            e.setCancelled(true);
            add2Depo(e, p, -4096);
        });

        GuiItem takeAllGui = new GuiItem(takeAll.clone(), e -> {
            e.setCancelled(true);
            removeAllCoins(e, p);
        });

        GuiItem typeValGui = new GuiItem(typeVal.clone(), e -> {
            e.setCancelled(true);
            typeValue(e, p);
        });
        GuiItem guiBalance = new GuiItem(balance, e -> e.setCancelled(true));

        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 3; j++) {
                pane.addItem(confirmGui.copy(), i, j);
                pane.addItem(cancelGui.copy(), i+7,j);
            }
        }
        pane.addItem(plus16Gui.copy(), 2, 0);
        pane.addItem(plus64Gui.copy(), 3, 0);
        pane.addItem(plus640Gui.copy(), 4, 0);
        pane.addItem(plus2048Gui.copy(), 5, 0);
        pane.addItem(plus4096Gui.copy(), 6, 0);

        pane.addItem(minus16Gui.copy(), 2, 2);
        pane.addItem(minus64Gui.copy(), 3, 2);
        pane.addItem(minus640Gui.copy(), 4, 2);
        pane.addItem(minus2048Gui.copy(), 5, 2);
        pane.addItem(minus4096Gui.copy(), 6, 2);

        pane.addItem(takeAllGui.copy(), 2, 1);
        pane.addItem(guiBalance.copy(), 4, 1);
        pane.addItem(typeValGui.copy(), 6, 1);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void confirmDeposit(InventoryClickEvent e, Player p) {
        ArrayList<ItemStack> toAdd = new ArrayList<>();
        if(e.getView().getTopInventory().getItem(3) != null) toAdd.add(e.getView().getTopInventory().getItem(3));
        if(e.getView().getTopInventory().getItem(4) != null) toAdd.add(e.getView().getTopInventory().getItem(4));
        if(e.getView().getTopInventory().getItem(5) != null) toAdd.add(e.getView().getTopInventory().getItem(5));
        if(e.getView().getTopInventory().getItem(12) != null) toAdd.add(e.getView().getTopInventory().getItem(12));
        if(e.getView().getTopInventory().getItem(13) != null) toAdd.add(e.getView().getTopInventory().getItem(13));
        if(e.getView().getTopInventory().getItem(14) != null) toAdd.add(e.getView().getTopInventory().getItem(14));
        if(e.getView().getTopInventory().getItem(21) != null) toAdd.add(e.getView().getTopInventory().getItem(21));
        if(e.getView().getTopInventory().getItem(22) != null) toAdd.add(e.getView().getTopInventory().getItem(22));
        if(e.getView().getTopInventory().getItem(23) != null) toAdd.add(e.getView().getTopInventory().getItem(23));
        int total = 0;
        for(ItemStack item : toAdd) {
            if(Util.isCoin(item)) {
                total += item.getAmount();
            } else if(Util.isGigaCoin(item)) {
                total += item.getAmount() * 64;
            } else if(Util.isTeraCube(item)) {
                total += item.getAmount() * 4096;
            } else if(Util.isHexidium(item)) {
                total += item.getAmount() * 262144;
            }
        }
        if(getMarketMultiplier() > 1) {
            total *= (1.0/getMarketMultiplier());
        }
        if(!p.hasPermission("boxplugin.tenmilbanklimit") && !p.hasPermission("boxplugin.twentyfivemilbanklimit") && !p.hasPermission("boxplugin.nobanklimit") && total + getCoinsBalance(p) > 500000) {
            p.sendMessage(ChatColor.RED + "You cannot exceed 500000 coins in your account!");
            e.getView().close();
            return;
        }
        if(p.hasPermission("boxplugin.tenmilbanklimit") && !p.hasPermission("boxplugin.twentyfivemilbanklimit") && !p.hasPermission("boxplugin.nobanklimit") && total + getCoinsBalance(p) > 10000000) {
            p.sendMessage(ChatColor.RED + "You cannot exceed 10000000 coins in your account!");
            e.getView().close();
            return;
        }
        if(p.hasPermission("boxplugin.twentyfivemilbanklimit") && !p.hasPermission("boxplugin.nobanklimit") && total + getCoinsBalance(p) > 25000000) {
            p.sendMessage(ChatColor.RED + "You cannot exceed 25000000 coins in your account!");
            e.getView().close();
            return;
        }
        if(Util.isCurrency(e.getView().getTopInventory().getItem(3))) e.getView().getTopInventory().setItem(3, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(4))) e.getView().getTopInventory().setItem(4, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(5))) e.getView().getTopInventory().setItem(5, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(12))) e.getView().getTopInventory().setItem(12, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(13))) e.getView().getTopInventory().setItem(13, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(14))) e.getView().getTopInventory().setItem(14, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(21))) e.getView().getTopInventory().setItem(21, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(22))) e.getView().getTopInventory().setItem(22, null);
        if(Util.isCurrency(e.getView().getTopInventory().getItem(23))) e.getView().getTopInventory().setItem(23, null);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        p.sendMessage(ChatColor.GREEN + "Added " + total + " coins to your account!");
        addCoinsBalance(p, total);
        BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
        e.getView().close();
    }

    public void cancelDeposit(InventoryClickEvent e, Player p) {
        e.getView().close();
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        openGui(p);
    }

    public void add2Depo(InventoryClickEvent e, Player p, int userNum) {
        if (selectedCoins.containsKey(p.getUniqueId())){
            if (userNum > 0) {
                if (selectedCoins.get(p.getUniqueId()) + userNum >= getCoinsBalance(p)) {
                    selectedCoins.put((p.getUniqueId()), getCoinsBalance(p));
                }
                else {
                    selectedCoins.put((p.getUniqueId()), selectedCoins.get(p.getUniqueId()) + userNum);
                }
            }
            if (userNum < 0) {
                if (selectedCoins.get(p.getUniqueId()) + userNum <= 0) {
                    selectedCoins.put((p.getUniqueId()), 0);
                }
                else {
                    selectedCoins.put((p.getUniqueId()), selectedCoins.get(p.getUniqueId()) + userNum);
                }
            }
        }
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        openWithDrawMenu(p);
    }
    public void typeValue(InventoryClickEvent e, Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        openWithdrawSign(p);
    }
    public void removeAllCoins(InventoryClickEvent e, Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        if (selectedCoins.containsKey(p.getUniqueId())){
            selectedCoins.put((p.getUniqueId()), getCoinsBalance(p));
        }
        openWithDrawMenu(p);
    }
    public void confrimWithdraw(InventoryClickEvent e, Player p) {
        int amount = selectedCoins.get(p.getUniqueId());
        int orig = amount;
        amount *= getMarketMultiplier();
        int withMult = amount;
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
        for(ItemStack item : h.values()) {
            Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
            i.setItemStack(item);
        }
        for(ItemStack item : t.values()) {
            Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
            i.setItemStack(item);
        }
        for(ItemStack item : g.values()) {
            Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
            i.setItemStack(item);
        }
        for(ItemStack item : x.values()) {
            Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
            i.setItemStack(item);
        }
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        if (amount == 0){
            p.sendMessage(ChatColor.GREEN + "Withdrew 0 coins.");
        }
        else {
            p.sendMessage(ChatColor.GREEN + "Withdrew " + orig + " coins, now worth " + withMult + " from the market's current value!");
            BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
            removeCoinsBalance(p, orig);
        }
        e.getView().close();

    }

    public void openWithdrawSign(Player p) {
        SignGUI sign = SignGUI.builder()
                .setLines(null, "===============",  "Withdraw Amount", "(or \"all\")")
                .setType(Material.OAK_SIGN)
                .setHandler((pl, result) -> {
                    Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                        String input = result.getLine(0);
                        try{
                            int amount;
                            if(input.equals("all")) {
                                amount = getCoinsBalance(p);
                            } else {
                                amount = Integer.parseInt(input);
                            }
                            if(amount > getCoinsBalance(pl)) {
                                pl.sendMessage(ChatColor.RED + "You don't have enough coins in your account!");
                                return;
                            }
                            int orig = amount;
                            amount *= getMarketMultiplier();
                            int withMult = amount;
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
                            for(ItemStack item : h.values()) {
                                Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
                                i.setItemStack(item);
                            }
                            for(ItemStack item : t.values()) {
                                Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
                                i.setItemStack(item);
                            }
                            for(ItemStack item : g.values()) {
                                Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
                                i.setItemStack(item);
                            }
                            for(ItemStack item : x.values()) {
                                Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
                                i.setItemStack(item);
                            }
                            removeCoinsBalance(p, orig);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                            p.sendMessage(ChatColor.GREEN + "Withdrew " + orig + " coins, now worth " + withMult + " from the market's current value!");
                            openGui(p);
                            BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
                        } catch (NumberFormatException e) {
                            pl.sendMessage(ChatColor.RED + "Invalid input!");
                        }
                    }, 1L);
                    return Collections.emptyList();
                })
                .build();
        sign.open(p);
    }

    public void openRubyGui(Player p) {
        ChestGui gui = new ChestGui(3, "Exchange Rubies");
        StaticPane pane = new StaticPane(9, 3);

        ItemStack buy = new ItemStack(Material.GOLD_INGOT);
        ItemMeta buyMeta = buy.getItemMeta();
        buyMeta.setDisplayName(ChatColor.GOLD + "Buy rubies");
        buyMeta.setLore(List.of(
                "",
                ChatColor.GRAY + "Buying rubies takes money out of your bank balance.",
                ChatColor.GRAY + "You have " + ChatColor.GOLD + getCoinsBalance(p) + " coins in your bank account.",
                "",
                ChatColor.GRAY + "Cost: " + ChatColor.GOLD + "10,000 coins",
                ChatColor.GREEN + "" + ChatColor.BOLD + "Click to buy " + ChatColor.RED + "1x Ruby",
                ""
        ));
        buy.setItemMeta(buyMeta);

        ItemStack exchange = Util.getSkull("http://textures.minecraft.net/texture/2530191500c2453624dd937ec125d44f0942cc2b664073e2a366b3fa67a0c897");
        ItemMeta exchangeMeta = exchange.getItemMeta();
        exchangeMeta.setDisplayName(ChatColor.GREEN + "Deposit Ruby items");
        exchangeMeta.setLore(List.of(
                "",
                ChatColor.GRAY + "Click to exchange all Rubies in your inventory",
                ChatColor.GRAY + "for rubies in your bank account",
                "",
                ChatColor.RED + "This process is irreversible!"
        ));
        exchange.setItemMeta(exchangeMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go back");
        cancel.setItemMeta(cancelMeta);

        gui.setOnGlobalClick(e -> {
            e.setCancelled(true);
        });

        GuiItem buyGui = new GuiItem(buy, e -> {
            e.setCancelled(true);
            if(getCoinsBalance(p) >= 10000) {
                removeCoinsBalance(p, 10000);
                addRubies(p, 1);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                p.sendMessage(ChatColor.GREEN + "Bought 1x ruby for " + ChatColor.GOLD + "10000 coins");
            } else {
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "You don't have enough coins in your account!");
            }
            BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
        });

        GuiItem exchangeGui = new GuiItem(exchange, e -> {
            e.setCancelled(true);
            int count = 0;
            for(int i = 0; i < e.getView().getBottomInventory().getSize(); i++) {
                if(Util.isRuby(e.getView().getBottomInventory().getItem(i))) {
                    count += e.getView().getBottomInventory().getItem(i).getAmount();
                    e.getView().getBottomInventory().setItem(i, null);
                }
            }
            addRubies(p, count);
            BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
            p.sendMessage(ChatColor.GREEN + "Added " + count + " rubies to you account!");
            openGui(p);
        });

        GuiItem cancelGui = new GuiItem(cancel, e -> {
            e.setCancelled(true);
            openGui(p);
        });

        pane.addItem(buyGui, 2, 1);

        pane.addItem(exchangeGui, 6, 1);

        pane.addItem(cancelGui, 4, 2);

        gui.addPane(pane);

        gui.copy().show(p);
    }

    public double getMarketMultiplier() {
        return Bukkit.getWorld("Xanatos").getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "MARKET_MULTIPLIER"), PersistentDataType.DOUBLE);
    }

    public double randomizeMarketMultiplier() {
        double min = 0.8;
        double max = 1.1;
        double newMult = min + (new Random().nextDouble() * (max-min));
        newMult = ((int)(newMult * 100))/100.0;
        Bukkit.getWorld("Xanatos").getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "MARKET_MULTIPLIER"), PersistentDataType.DOUBLE, newMult);
        return newMult;
    }

    public void setMarketMultiplier(double d) {
        Bukkit.getWorld("Xanatos").getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "MARKET_MULTIPLIER"), PersistentDataType.DOUBLE, d);

    }

    public void initializeMarketMultiplier() {
        if(!Bukkit.getWorld("Xanatos").getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "MARKET_MULTIPLIER"), PersistentDataType.DOUBLE)) {
            Bukkit.getWorld("Xanatos").getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "MARKET_MULTIPLIER"), PersistentDataType.DOUBLE, 1.0);
        }
    }

    public int getCoinsBalance(OfflinePlayer p) {
        return (int)BoxPlugin.instance.getEconomy().getBalance(p);
    }

    public void addCoinsBalance(OfflinePlayer p, double amount) {
        BoxPlugin.instance.getEconomy().depositPlayer(p, amount);
    }

    public void removeCoinsBalance(OfflinePlayer p, double amount) {
        BoxPlugin.instance.getEconomy().withdrawPlayer(p, amount);
    }

    public int getRubies(Player p) {
        if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "rubies"), PersistentDataType.INTEGER)) {
            return p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "rubies"), PersistentDataType.INTEGER);
        } else {
            return 0;
        }
    }

    public void setRubies(Player p, int amount) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "rubies"), PersistentDataType.INTEGER, amount);
    }

    public void addRubies(Player p, int amount) {
        setRubies(p, getRubies(p)+amount);
    }

}
