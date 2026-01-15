package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.google.common.collect.Maps;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.util.ListDataType;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GhostTokenManager {

    // there has to be a better way but i think this works
    public enum Reclaimable {
        ENTERPRISE(64),
        XANATOS(100, null, "XANATOS_COIN"),
        LEAFY(148),
        STONE_GOLEM(250),
        MOLTEN_IRON(340, null, "MOLTEN_IRON_BOX"),
        SPOOKY(680, "☠☠☠", "SPOOKY_BOX"),
        HELLFIRE(980, null, "HELLFIRE_BOX"),
        OBSIDIAN(1200, null, "OBSIDIAN_BOX"),
        SUPER_GOLDEN(1600, null, "SUPER_GOLDEN_BOX"),
        AXOLOTL(2200, "TASTY_SNACK", "AXOLOTL_BOX"),
        AXOLOTL_SAMURAI(2750, "PUFFERMACE", "AXOLOTL_SAMURAI_BOX"),
        MODERN(3450, "FUTURISTIC_DEVICE", "MODERN_BOX"),
        SPEED(3950, null, "MODERN_SPEED_BOX"),
        GLASS_CANNON(4250, null, "MODERN_GLASS_CANNON_BOX"),
        TANK(4250, null, "MODERN_TANK_BOX"),
        SHINY_DIAMOND(10000, null, "SHINY_DIAMOND_BOX"),
        CHEF(13800, "LET_ME_COOK"),
        GUARDIAN(18600, "OCEAN_GLASS"),
        EMERALD(26000),
        ANUBIS(40000),
        ZEUS(55000, null, "GODLY_ZEUS"),
        DRAGON_SCALE(125000, "FIREBALL"),
        SUPREME(950000, "XANATOS_STAR"),
        NEPTUNE(30000),
        CANNON(5000),
        ENERGIZED_SCYTHE(30000),
        SOUL_SMITE(42000),
        BANE_OF_THE_DEAD(2000),
        FIRE_FLOWER(2800),
        PACK(8000),
        XANATOASTER(500),
        SCEPTER(2200),
        VOID_SCEPTER(12000),
        GOD_SLAYER(120000),
        JAVELIN(31000),
        LOYAL_SOUL_JAVELIN(45000),
        CAGE(4500),
        GIGA_DIGGER(2300),
        BIGGER_GIGA_DIGGER(5000),
        DRILL(9000),
        EXCAVATOR(27000),
        CROSSBOW(800),
        DREADNOUGHT(7500),
        RIPTIDE(3400),
        PLUTONIUM(110000),
        EXOTIC_PICKAXE(17000),
        EXOTIC_BOW(1050000);

        public final int cost;
        public final String altKey;
        public final String not;

        private static final Map<List<String>, Reclaimable> BY_NAME = Maps.newHashMap();

        private Reclaimable(int cost) {
            this.cost = cost;
            this.altKey = null;
            this.not = null;
        }
        private Reclaimable(int cost, String altKey, String not) {
            this.cost = cost;
            this.altKey = altKey;
            this.not = not;
        }
        private Reclaimable(int cost, String altKey) {
            this.cost = cost;
            this.altKey = altKey;
            this.not = null;
        }

        /**
         *
         * @return a list of strings that items of this type can have in their custom names
         */
        public List<String> getPossibleNames() {
            if(this.altKey != null) {
                return List.of(this.name().toLowerCase().replaceAll("_", " "), this.altKey.toLowerCase().replaceAll("_", " "));
            } else {
                return List.of(this.name().toLowerCase().replaceAll("_", " "));
            }
        }

        /**
         *
         * @param name The name of the item being searched
         * @return the highest priced reclaimable that matches the name entered
         */
        @Nullable
        public static Reclaimable getByName(String name) {
            int highest = 0;
            Reclaimable reclaimable = null;
            for(List<String> names : BY_NAME.keySet()) {
                for(String possibleName : names) {
                    String lowerStripped = ChatColor.stripColor(name).toLowerCase();
                    if(lowerStripped.contains(possibleName)) {
                        if(BY_NAME.get(names).cost > highest) {
                            reclaimable = BY_NAME.get(names);
                            if(reclaimable.not != null && lowerStripped.contains(reclaimable.not.toLowerCase().replaceAll("_", " "))) {
                                reclaimable = null;
                            } else {
                                highest = BY_NAME.get(names).cost;
                            }
                        }
                    }
                }
            }
            return reclaimable;
        }

        static {
            for (Reclaimable reclaimable : values()) {
                BY_NAME.put(reclaimable.getPossibleNames(), reclaimable);
            }
        }
    }

    private static final NamespacedKey reclaimablesKey = new NamespacedKey(BoxPlugin.instance, "reclaimables");
    private static final NamespacedKey hasGhostItemsKey = new NamespacedKey(BoxPlugin.instance, "hasGhostItems");


    private void storeReclaimablesInPDC(Player p, List<ItemStack> items) {
        p.getPersistentDataContainer().set(reclaimablesKey, new ListDataType(), items);
    }

    public void onPreDeath(List<ItemStack> drops, Player p) {
        List<ItemStack> ghostItems = new ArrayList<>();
        for(int i = 0; i < p.getInventory().getContents().length; i++) {
            ItemStack item = p.getInventory().getContents()[i];
            if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING).equals("GHOST_TOKEN")) {
                p.getInventory().setItem(i, null);
            }
        }
        for(ItemStack item : p.getInventory().getContents()) {
            if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER)) {
                ghostItems.add(item);
            }
        }
        p.getPersistentDataContainer().remove(hasGhostItemsKey);
        Util.debug(p, "Found " + ghostItems.size() + " ghost items");
        if(ghostItems.size() > 0) {
            drops.removeAll(ghostItems);
            for(int i = 0; i < p.getInventory().getContents().length; i++) {
                ItemStack item = p.getInventory().getContents()[i];
                if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER)) {
                    p.getInventory().setItem(i, null);
                }
            }
            p.sendMessage(ChatColor.RED + "You lost " + ghostItems.size() + " ghost items permanently!");
        }
    }

    public void openGui(Player p) {
        ChestGui gui = new ChestGui(3, "Reclaim Items");
        StaticPane pane = new StaticPane(9,3);


        gui.setOnClose(e -> {
            ArrayList<ItemStack> toAdd = new ArrayList<>();
            if(e.getView().getTopInventory().getItem(13) != null) {
                ItemStack item = e.getView().getTopInventory().getItem(13);
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                if(isGhostItem(item)) {
                    lore.remove(lore.size()-1);
                    lore.remove(lore.size()-1);
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
            reclaimItem(e, p);
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
                    if(isGhostItem(item)) {
                        lore.remove(lore.size()-1);
                        lore.remove(lore.size()-1);
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    e.getInventory().setItem(e.getSlot(), item);
                    if(e.getWhoClicked().getInventory().addItem(e.getInventory().getItem(e.getSlot())).size() > 0) {
                        ItemStack currentItem = e.getInventory().getItem(e.getSlot());
                        ItemMeta currentItemMeta = e.getCurrentItem().getItemMeta();
                        List<String> currentLore = currentItemMeta.getLore();
                        int coins = Reclaimable.getByName(currentItem.getItemMeta().getDisplayName()).cost;
                        if(BoxPlugin.instance.getReforgeManager().hasReforges(currentItem)){
                            int bonusCoins = BoxPlugin.instance.getReforgeManager().getNumReforges(currentItem) * 40000;
                            coins += bonusCoins;
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
            if(e.getCurrentItem() != null && isGhostItem(e.getCurrentItem())) {
                if(e.getSlot() != e.getRawSlot()) { // not clicked in chest
                    if(e.getView().getTopInventory().getItem(13) != null) {
                        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                        p.sendMessage(ChatColor.RED + "You're already reclaiming an item!");
                    } else {
                        ItemStack currentItem = e.getCurrentItem();
                        ItemMeta currentItemMeta = e.getCurrentItem().getItemMeta();
                        List<String> lore = currentItemMeta.getLore();
                        if(lore == null) lore = new ArrayList<>();
                        int coins = Reclaimable.getByName(currentItem.getItemMeta().getDisplayName()).cost;
                        if(BoxPlugin.instance.getReforgeManager().hasReforges(currentItem)){
                            int bonusCoins = BoxPlugin.instance.getReforgeManager().getNumReforges(currentItem) * 40000;
                            coins += bonusCoins;
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
                p.sendMessage(ChatColor.RED + "That isn't a ghost item!");
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

    public boolean hasGhostItems(Player p) {
        return p.getPersistentDataContainer().has(hasGhostItemsKey, PersistentDataType.INTEGER);
    }

    public void reclaimItem(InventoryClickEvent e, Player p) {
        final int rawSlot = 13;
        ItemStack item = e.getView().getTopInventory().getItem(rawSlot);
        if(item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            Reclaimable reclaimable = Reclaimable.getByName(item.getItemMeta().getDisplayName());
            if(reclaimable == null || !isGhostItem(item)) {
                p.sendMessage(ChatColor.RED + "That isn't a ghost item!");
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                return;
            }
            int coins = reclaimable.cost * (item.getType() == Material.SHIELD ? 2 : 1);
            if(BoxPlugin.instance.getReforgeManager().hasReforges(item)){
                int bonusCoins = BoxPlugin.instance.getReforgeManager().getNumReforges(item) * 40000;
                coins += bonusCoins;
            }
            if(BoxPlugin.instance.getMarketManager().getCoinsBalance(p) < coins) {
                p.sendMessage(ChatColor.RED + "You don't have enough money in your bank!");
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            } else {
                BoxPlugin.instance.getMarketManager().removeCoinsBalance(p, coins);
                BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
                e.getView().getTopInventory().setItem(rawSlot, stripGhost(e.getView().getTopInventory().getItem(rawSlot)));
                p.sendMessage(ChatColor.GREEN + "Successfully reclaimed your ghost item for " + coins + " Xanatos coins!");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                boolean hasMore = false;
                for(ItemStack i : p.getInventory().getContents()) {
                    if(isGhostItem(i)) {
                        Util.debug(p, "Remaining ghost item " + i.getItemMeta().getDisplayName());
                        hasMore = true;
                    }
                }
                if(!hasMore) {
                    Util.debug(p, "Removed ghost item tag from player");
                    p.getPersistentDataContainer().remove(hasGhostItemsKey);
                }
            }
        }
    }

    public void onPostDeath(List<ItemStack> drops, Player p) {
        if((BoxPlugin.instance.getConfig().contains("check-ip") && BoxPlugin.instance.getConfig().getBoolean("check-ip")) && (p.getKiller() != null && p.getKiller().getAddress().equals(p.getAddress()))) return;
        List<ItemStack> reclaimables = new ArrayList<>();
        for(ItemStack item : drops) {
            if(item != null && !isGhostItem(item) && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && Reclaimable.getByName(item.getItemMeta().getDisplayName()) != null && !item.getType().name().endsWith("_BOX")) {
                reclaimables.add(item.clone()); // not sure if this needs deep copy but I'll be safe
            }
        }
        storeReclaimablesInPDC(p, reclaimables);
        Util.debug(p, "You had " + reclaimables.size() + " reclaimables out of " + drops.size() + " drops");
        if(reclaimables.size() > 0) {
            Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                giveGhostToken(p);
                p.sendMessage(ChatColor.RED + "Your ghost token preserved " + reclaimables.size() + " items as ghost items. Right click it to claim them.");
            }, 5L);
        }
    }

    public int getReclaimableCountFromPDC(Player p) {
        List<ItemStack> reclaimables = p.getPersistentDataContainer().get(reclaimablesKey, new ListDataType());
        if(reclaimables != null) {
            return reclaimables.size();
        } else {
            return 0;
        }
    }

    public void clearReclaimables(Player p) {
        p.getPersistentDataContainer().remove(reclaimablesKey);
    }

    public void restoreReclaimables(Player p) {
        List<ItemStack> reclaimables = p.getPersistentDataContainer().get(reclaimablesKey, new ListDataType());
        if(reclaimables != null) {
            HashMap<Integer, ItemStack> toDrop = p.getInventory().addItem(reclaimables.stream().map(i -> makeGhost(i)).collect(Collectors.toList()).toArray(new ItemStack[reclaimables.size()]));
            p.sendMessage(ChatColor.AQUA + "Restored " + reclaimables.size() + " ghost items!");
            if(toDrop.size() > 0) {
                p.sendMessage(ChatColor.RED + "" + toDrop.size() + " ghost items could not fit into your inventory and were permanently deleted!");
            }
        }
        p.getPersistentDataContainer().set(hasGhostItemsKey, PersistentDataType.INTEGER, 1);
        clearReclaimables(p);
    }

    public boolean isGhostItem(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER);
    }

    public ItemStack makeGhost(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if(meta != null && !meta.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER)) {
            meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "SOULBOUND"), PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "ghost_uuid"), PersistentDataType.STRING, UUID.randomUUID().toString());
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.AQUA + "This is a ghost item");
            lore.add(ChatColor.AQUA + "" + ChatColor.ITALIC + "/ghostitem for more info");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack stripGhost(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if(meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "ghost"), PersistentDataType.INTEGER)) {
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 5) {
                for (int i = 0; i < 5; i++) {
                    lore.remove(lore.size() - 1);
                }
            }
            meta.setLore(lore);
            meta.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "ghost"));
            meta.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "SOULBOUND"));
            meta.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "ghost_uuid"));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void giveGhostToken(Player p) {
        p.getInventory().addItem(CustomItemsMain.instance.getItem("GHOST_TOKEN").getItemStack());
    }
}
