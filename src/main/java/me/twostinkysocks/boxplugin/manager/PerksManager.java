package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.google.common.collect.Maps;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.perks.*;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerksManager {

    // TODO: perk obsidian
    // TODO: megaperks: speed 5, strength 6, resistance 2, regen 1


    // perk_speed, etc, are all integers, 0 or 1

    // selected_perks is a string of all perk keys, separated by \n

    // xp is current xp

    // xp_boost_level is current xp boost level (1,2,3,4,5,etc.)

    public enum Perk {
        SPEED(new PerkSpeed()),
        STRENGTH(new PerkStrength()),
        WATER_BREATHING(new PerkWaterBreathing()),
        HASTE(new PerkHaste()),
        ROCKETS(new PerkRockets()),
        OBSIDIAN(new PerkObsidian()),
        XPBOOST(new PerkXPBoost()),
        MAGNET(new PerkMagnet());

        public final AbstractPerk instance;

        private static final Map<String, Perk> BY_NAME = Maps.newHashMap();

        private Perk(AbstractPerk instance) {
            this.instance = instance;
        }

        public static Perk getByName(String name) {
            return BY_NAME.get(name);
        }

        public static List<String> getKeys() {
            return new ArrayList<String>(BY_NAME.keySet());
        }

        static {
            for (Perk perk : values()) {
                BY_NAME.put(perk.instance.getKey(), perk);
            }
        }
    }

    public enum MegaPerk {
        //MEGA_SPEED(new MegaPerkSpeed()), no more mega speed lol, upgradeable normal now
        MEGA_HEARTSTEEL(new MegaPerkHeartSteal()),
        MEGA_STRENGTH(new MegaPerkStrength()),
        MEGA_RESISTANCE(new MegaPerkResistance()),
        MEGA_REGENERATION(new MegaPerkRegeneration()),

        MEGA_COOLDOWN_REDUCTION(new MegaPerkCooldownReduction()),
        MEGA_LIFESTEAL(new MegaPerkLifeSteal());

        public final AbstractPerk instance;

        private static final Map<String, MegaPerk> BY_NAME = Maps.newHashMap();

        private MegaPerk(AbstractPerk instance) {
            this.instance = instance;
        }

        public static MegaPerk getByName(String name) {
            return BY_NAME.get(name);
        }

        public static List<String> getKeys() {
            return new ArrayList<String>(BY_NAME.keySet());
        }

        static {
            for (MegaPerk perk : values()) {
                BY_NAME.put(perk.instance.getKey(), perk);
            }
        }
    }

    public void openMainGui(Player p) {
        ChestGui gui = new ChestGui(5, "Equipped Perks");
        gui.setOnGlobalClick(e -> {
            e.setCancelled(true);
        });
        StaticPane pane = new StaticPane(0, 0, 9, 5);
        List<Perk> selectedPerks = getSelectedPerks(p);
        GuiItem perkOne = null;
        GuiItem perkTwo = null;
        GuiItem perkThree = null;
        // note to self, never write code like this ever again
        if(BoxPlugin.instance.getXpManager().getLevel(p) < 50) {
            if(selectedPerks.size() == 0) {
                ItemStack itemOne = new ItemStack(Material.STONE);
                ItemMeta itemOneMeta = itemOne.getItemMeta();
                itemOneMeta.setDisplayName(ChatColor.GRAY + "No perk selected");
                List<String> newItemOneLore = itemOneMeta.getLore() == null ? new ArrayList<>() : itemOneMeta.getLore();
                newItemOneLore.add("");
                newItemOneLore.add(ChatColor.GRAY + "Click to change perk");
                itemOneMeta.setLore(newItemOneLore);
                itemOne.setItemMeta(itemOneMeta);
                perkOne = new GuiItem(itemOne, e -> {
                    clickPerkSlot(e, 1);
                });
                ItemStack itemTwo = new ItemStack(Material.BEDROCK);
                ItemMeta itemTwoMeta = itemTwo.getItemMeta();
                itemTwoMeta.setDisplayName(ChatColor.RED + "Unlocks at level 50");
                itemTwo.setItemMeta(itemTwoMeta);
                perkTwo = new GuiItem(itemTwo, e -> {
                    e.setCancelled(true);
                });
            } else { // 1 or 2 has the same behavior
                Perk selected = selectedPerks.get(0);
                ItemStack itemOne = selected.instance.getGuiItem(p).clone();
                ItemMeta itemOneMeta = itemOne.getItemMeta();
                List<String> newItemOneLore = itemOneMeta.getLore() == null ? new ArrayList<>() : itemOneMeta.getLore();
                newItemOneLore.add("");
                newItemOneLore.add(ChatColor.GRAY + "Click to change perk");
                itemOneMeta.setLore(newItemOneLore);
                itemOne.setItemMeta(itemOneMeta);
                perkOne = new GuiItem(itemOne, e -> {
                    clickPerkSlot(e, 1);
                });
                ItemStack itemTwo = new ItemStack(Material.BEDROCK);
                ItemMeta itemTwoMeta = itemTwo.getItemMeta();
                itemTwoMeta.setDisplayName(ChatColor.RED + "Unlocks at level 50");
                itemTwo.setItemMeta(itemTwoMeta);
                perkTwo = new GuiItem(itemTwo, e -> {
                    e.setCancelled(true);
                });
            }
        } else {
            if(selectedPerks.size() == 0) {
                ItemStack itemOne = new ItemStack(Material.STONE);
                ItemMeta itemOneMeta = itemOne.getItemMeta();
                itemOneMeta.setDisplayName(ChatColor.GRAY + "No perk selected");
                List<String> newItemOneLore = itemOneMeta.getLore() == null ? new ArrayList<>() : itemOneMeta.getLore();
                newItemOneLore.add("");
                newItemOneLore.add(ChatColor.GRAY + "Click to change perk");
                itemOneMeta.setLore(newItemOneLore);
                itemOne.setItemMeta(itemOneMeta);
                perkOne = new GuiItem(itemOne, e -> {
                    clickPerkSlot(e, 1);
                });
                ItemStack itemTwo = new ItemStack(Material.STONE);
                ItemMeta itemTwoMeta = itemTwo.getItemMeta();
                itemTwoMeta.setDisplayName(ChatColor.GRAY + "No perk selected");
                List<String> newItemTwoLore = itemTwoMeta.getLore() == null ? new ArrayList<>() : itemTwoMeta.getLore();
                newItemTwoLore.add("");
                newItemTwoLore.add(ChatColor.GRAY + "Click to change perk");
                itemTwoMeta.setLore(newItemTwoLore);
                itemTwo.setItemMeta(itemTwoMeta);
                perkTwo = new GuiItem(itemTwo, e -> {
                    clickPerkSlot(e, 2);
                });
            } else if(selectedPerks.size() == 1 && selectedPerks.get(0) != null) {
                Perk selected = selectedPerks.get(0);
                ItemStack itemOne = selected.instance.getGuiItem(p).clone();
                ItemMeta itemOneMeta = itemOne.getItemMeta();
                List<String> newItemOneLore = itemOneMeta.getLore() == null ? new ArrayList<>() : itemOneMeta.getLore();
                newItemOneLore.add("");
                newItemOneLore.add(ChatColor.GRAY + "Click to change perk");
                itemOneMeta.setLore(newItemOneLore);
                itemOne.setItemMeta(itemOneMeta);
                perkOne = new GuiItem(itemOne, e -> {
                    clickPerkSlot(e, 1);
                });
                ItemStack itemTwo = new ItemStack(Material.STONE);
                ItemMeta itemTwoMeta = itemTwo.getItemMeta();
                itemTwoMeta.setDisplayName(ChatColor.GRAY + "No perk selected");
                List<String> newItemTwoLore = itemTwoMeta.getLore() == null ? new ArrayList<>() : itemTwoMeta.getLore();
                newItemTwoLore.add("");
                newItemTwoLore.add(ChatColor.GRAY + "Click to change perk");
                itemTwoMeta.setLore(newItemTwoLore);
                itemTwo.setItemMeta(itemTwoMeta);
                perkTwo = new GuiItem(itemTwo, e -> {
                    clickPerkSlot(e, 2);
                });
            } else if(selectedPerks.size() >= 2 && selectedPerks.get(0) != null && selectedPerks.get(1) != null) {
                Perk selected = selectedPerks.get(0);
                ItemStack itemOne = selected.instance.getGuiItem(p).clone();
                ItemMeta itemOneMeta = itemOne.getItemMeta();
                List<String> newItemOneLore = itemOneMeta.getLore() == null ? new ArrayList<>() : itemOneMeta.getLore();
                newItemOneLore.add("");
                newItemOneLore.add(ChatColor.GRAY + "Click to change perk");
                itemOneMeta.setLore(newItemOneLore);
                itemOne.setItemMeta(itemOneMeta);
                perkOne = new GuiItem(itemOne, e -> {
                    clickPerkSlot(e, 1);
                });
                selected = selectedPerks.get(1);
                ItemStack itemTwo = selected.instance.getGuiItem(p).clone();
                ItemMeta itemTwoMeta = itemTwo.getItemMeta();
                List<String> newItemTwoLore = itemTwoMeta.getLore() == null ? new ArrayList<>() : itemTwoMeta.getLore();
                newItemTwoLore.add("");
                newItemTwoLore.add(ChatColor.GRAY + "Click to change perk");
                itemTwoMeta.setLore(newItemTwoLore);
                itemTwo.setItemMeta(itemTwoMeta);
                perkTwo = new GuiItem(itemTwo, e -> {
                    clickPerkSlot(e, 2);
                });
            }
        }
        if(!BoxPlugin.instance.getMarketManager().has3rdPerk(p)) {
            ItemStack itemThree = new ItemStack(Material.BEDROCK);
            ItemMeta itemThreeMeta = itemThree.getItemMeta();
            itemThreeMeta.setDisplayName(ChatColor.RED + "Unlocked from Progression Pillars");
            itemThreeMeta.setLore(List.of(
                    ChatColor.RED + "Visit the " + ChatColor.AQUA + "" + ChatColor.BOLD + "Banker" + ChatColor.RED +" to claim"
            ));
            itemThree.setItemMeta(itemThreeMeta);
            perkThree = new GuiItem(itemThree, e -> {
                e.setCancelled(true);
            });
        } else {
            if(selectedPerks.size() < 3) {
                ItemStack itemThree = new ItemStack(Material.STONE);
                ItemMeta itemThreeMeta = itemThree.getItemMeta();
                itemThreeMeta.setDisplayName(ChatColor.GRAY + "No perk selected");
                List<String> newItemThreeLore = itemThreeMeta.getLore() == null ? new ArrayList<>() : itemThreeMeta.getLore();
                newItemThreeLore.add("");
                newItemThreeLore.add(ChatColor.GRAY + "Click to change perk");
                itemThreeMeta.setLore(newItemThreeLore);
                itemThree.setItemMeta(itemThreeMeta);
                perkThree = new GuiItem(itemThree, e -> {
                    clickPerkSlot(e, 3);
                });
            } else {
                Perk selected = selectedPerks.get(2);
                ItemStack itemThree = selected.instance.getGuiItem(p).clone();
                ItemMeta itemThreeMeta = itemThree.getItemMeta();
                List<String> newItemThreeLore = itemThreeMeta.getLore() == null ? new ArrayList<>() : itemThreeMeta.getLore();
                newItemThreeLore.add("");
                newItemThreeLore.add(ChatColor.GRAY + "Click to change perk");
                itemThreeMeta.setLore(newItemThreeLore);
                itemThree.setItemMeta(itemThreeMeta);
                perkThree = new GuiItem(itemThree, e -> {
                    clickPerkSlot(e, 3);
                });
            }
        }

        pane.addItem(perkOne, 2,1);
        pane.addItem(perkTwo, 4,1);
        pane.addItem(perkThree, 6, 1);

        GuiItem megaperkOne = null;
        GuiItem megaperkTwo = null;

        if(BoxPlugin.instance.getXpManager().getLevel(p) < 200) {
            ItemStack item = new ItemStack(Material.BEDROCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Unlocks at level 200");
            item.setItemMeta(meta);
            megaperkOne = new GuiItem(item, e -> {
                e.setCancelled(true);
            });
        } else {
            if(getSelectedMegaPerks(p).size() == 0) { // unlocked but nothing equipped
                ItemStack item = new ItemStack(Material.STONE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GRAY + "No Megaperk selected");
                List<String> newLore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
                newLore.add("");
                newLore.add(ChatColor.GRAY + "Click to change perk");
                meta.setLore(newLore);
                item.setItemMeta(meta);
                megaperkOne = new GuiItem(item, e -> {
                    clickMegaPerkSlot(e, 1);
                });
            } else { // unlocked and equipped
                MegaPerk selected = getSelectedMegaPerks(p).get(0);
                ItemStack item = selected.instance.getGuiItem(p).clone();
                ItemMeta meta = item.getItemMeta();
                List<String> newItemTwoLore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
                newItemTwoLore.add("");
                if(selected.equals(MegaPerk.MEGA_HEARTSTEEL)){
                    newItemTwoLore.add(ChatColor.GREEN + "Your stacks: " + BoxPlugin.instance.getMegaPerkHeartSteal().getStacks(p));
                    newItemTwoLore.add("");
                }
                newItemTwoLore.add(ChatColor.GRAY + "Click to change perk");
                meta.setLore(newItemTwoLore);
                item.setItemMeta(meta);
                megaperkOne = new GuiItem(item, e -> {
                    clickMegaPerkSlot(e, 1);
                });
            }
        }
        if(BoxPlugin.instance.getXpManager().getLevel(p) < 400) {
            ItemStack item = new ItemStack(Material.BEDROCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Unlocks at level 400");
            item.setItemMeta(meta);
            megaperkTwo = new GuiItem(item, e -> {
                e.setCancelled(true);
            });
        } else {
            if(getSelectedMegaPerks(p).size() <= 1) { // unlocked but nothing equipped
                ItemStack item = new ItemStack(Material.STONE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GRAY + "No Megaperk selected");
                List<String> newLore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
                newLore.add("");
                newLore.add(ChatColor.GRAY + "Click to change perk");
                meta.setLore(newLore);
                item.setItemMeta(meta);
                megaperkTwo = new GuiItem(item, e -> {
                    clickMegaPerkSlot(e, 2);
                });
            } else { // unlocked and equipped
                MegaPerk selected = getSelectedMegaPerks(p).get(1);
                ItemStack item = selected.instance.getGuiItem(p).clone();
                ItemMeta meta = item.getItemMeta();
                List<String> newItemTwoLore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
                newItemTwoLore.add("");
                if(selected.equals(MegaPerk.MEGA_HEARTSTEEL)){
                    newItemTwoLore.add(ChatColor.GREEN + "Your stacks: " + BoxPlugin.instance.getMegaPerkHeartSteal().getStacks(p));
                    newItemTwoLore.add("");
                }
                newItemTwoLore.add(ChatColor.GRAY + "Click to change perk");
                meta.setLore(newItemTwoLore);
                item.setItemMeta(meta);
                megaperkTwo = new GuiItem(item, e -> {
                    clickMegaPerkSlot(e, 2);
                });
            }
        }

        // xp upgrade
//        ItemStack xpUpgradeStack = new ItemStack(Material.EXPERIENCE_BOTTLE);
//        ItemMeta xpMeta = xpUpgradeStack.getItemMeta();
//        xpMeta.setDisplayName(ChatColor.GREEN + "Permanent XP Boost");
//        BigDecimal bd = new BigDecimal((calculateXPMultiplier(p)+0.1));
//        bd = bd.round(new MathContext(4));
//        double upgradedValue = bd.doubleValue();
//        xpMeta.setLore(List.of(
//                "",
//                ChatColor.AQUA + "Current boost: " + (calculateXPMultiplier(p)) + "x",
//                "",
//                ChatColor.GRAY + "Upgrade to " + ChatColor.AQUA + "" + upgradedValue + "x " + ChatColor.GRAY + "for " + ChatColor.GOLD + ChatColor.BOLD + calculateXPGigaCoinCost(p) + "" + " Giga Coins"
//        ));
//        xpUpgradeStack.setItemMeta(xpMeta);
//        GuiItem xpUpgradeItem = new GuiItem(xpUpgradeStack, this::clickXPUpgrade);
        //



        pane.addItem(megaperkOne, 3, 3);
        pane.addItem(megaperkTwo, 5, 3);


        gui.addPane(pane);

        gui.copy().show(p);
    }

    public void resetPerks(Player p) {
        for(Perk perk : getSelectedPerks(p)) {
            if(perk != null) {
                perk.instance.onUnequip(p);
            }
        }
        for(MegaPerk megaPerk : getSelectedMegaPerks(p)) {
            if(megaPerk != null) {
                megaPerk.instance.onUnequip(p);
            }
        }
        p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "selected_megaperks"));
        p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "selected_perks"));
        for(MegaPerk perk: MegaPerk.values()) {
            if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey()), PersistentDataType.INTEGER)) {
                p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey()));
            }
        }
        for(Perk perk : Perk.values()) {
            if(perk.instance instanceof Upgradable && p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey() + "_level"), PersistentDataType.INTEGER)) {
                p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey() + "_level"));
            }
            if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey()), PersistentDataType.INTEGER)) {
                p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey()));
            }
        }
    }

    public void openMegaPerkBuyGui(Player p, int slot) {
        ChestGui gui = new ChestGui(6, "Available Megaperks");
        gui.setOnGlobalClick(e -> {
            e.setCancelled(true);
        });
        StaticPane pane = new StaticPane(0, 0, 9, 6);
        for(int i = 0; i < MegaPerk.getKeys().size(); i++) {
            final int j = i;
            MegaPerk perk = MegaPerk.getByName(MegaPerk.getKeys().get(i));
            ItemStack visualItem = perk.instance.getGuiItem(p).clone();
            ItemMeta meta = visualItem.getItemMeta();
            assert meta != null;
            List<String> newLore = meta.getLore();
            assert newLore != null;
            if(ownsMegaPerk(p, perk)) { // show "owned"
                if(perk.equals(MegaPerk.MEGA_HEARTSTEEL)){
                    newLore.add("");
                    newLore.add(ChatColor.GREEN + "Your stacks: " + BoxPlugin.instance.getMegaPerkHeartSteal().getStacks(p));
                }
                newLore.add("");
                newLore.add(ChatColor.GREEN + "Owned");
                newLore.add(ChatColor.GRAY + "Click to equip");
            } else { // show cost
                newLore.add("");
                newLore.add(ChatColor.GOLD + "Costs " + perk.instance.getCost() + ChatColor.translateAlternateColorCodes('&', " &x&4&4&4&4&4&4&lG&x&4&9&3&D&3&D&lh&x&4&E&3&6&3&6&la&x&5&2&3&0&3&0&ls&x&5&7&2&9&2&9&lt&x&5&C&2&2&2&2&ll&x&6&1&1&B&1&B&ly &x&6&6&1&4&1&4&lH&x&6&A&0&E&0&E&le&x&6&F&0&7&0&7&lr&x&7&4&0&0&0&0&lb"));
                newLore.add(ChatColor.GRAY + "Click to buy and equip");
            }
            meta.setLore(newLore);
            visualItem.setItemMeta(meta);
            GuiItem item = new GuiItem(visualItem, e -> {
                e.setCancelled(true);
                List<MegaPerk> selectedPerks = getSelectedMegaPerks(p);
                if(selectedPerks.contains(perk)) {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25f, 0.5f);
                    p.sendMessage(ChatColor.RED + "You already have this megaperk equipped!");
                    return;
                }
                if(BoxPlugin.instance.getGhostTokenManager().hasGhostItems(p) && selectedPerks.size()>=0) {
                    p.sendMessage(ChatColor.RED + "You have ghost items in your inventory! You can only equip one perk!");
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                    return;
                }
                if(ownsMegaPerk(p, perk)) {
                    if(selectedPerks.size() < slot) {
                        selectedPerks.add(perk);
                    } else {
                        selectedPerks.set(slot-1, perk);
                    }
                    setSelectedMegaPerks(p, selectedPerks);
                    p.sendMessage(ChatColor.GREEN + "Equipped megaperk!");
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                    openMainGui(p);
                } else {
                    // this deducts coins but does not add the perk
                    boolean canBuy = buyMegaPerk(perk, p);
                    if(canBuy) {
                        addMegaPerk(p, perk);
                        if(selectedPerks.size() < slot) {
                            selectedPerks.add(perk);
                        } else {
                            selectedPerks.set(slot-1, perk);
                        }
                        setSelectedMegaPerks(p, selectedPerks);
                        p.sendMessage(ChatColor.GREEN + "Bought megaperk!");
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                        openMainGui(p);
                    } else {
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25f, 0.5f);
                        p.sendMessage(ChatColor.RED + "You don't have enough " + ChatColor.translateAlternateColorCodes('&', "&x&4&4&4&4&4&4&lG&x&4&9&3&D&3&D&lh&x&4&E&3&6&3&6&la&x&5&2&3&0&3&0&ls&x&5&7&2&9&2&9&lt&x&5&C&2&2&2&2&ll&x&6&1&1&B&1&B&ly &x&6&6&1&4&1&4&lH&x&6&A&0&E&0&E&le&x&6&F&0&7&0&7&lr&x&7&4&0&0&0&0&lbs"));
                    }
                }
            });
            if (j <= 2) {
                pane.addItem(item, j+1, 2);
            } else {
                pane.addItem(item, j+2, 2);
            }

            ItemStack cancel = new ItemStack(Material.BARRIER);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.setDisplayName(ChatColor.RED + "Go back");
            cancel.setItemMeta(cancelMeta);
            pane.addItem(new GuiItem(cancel, e -> {
                e.setCancelled(true);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                openMainGui(p);
            }), 4, 5);

            ItemStack unequip = new ItemStack(Material.BLAZE_POWDER);
            ItemMeta unequipMeta = unequip.getItemMeta();
            unequipMeta.setDisplayName(ChatColor.RED + "Unequip Megaperk");
            unequip.setItemMeta(unequipMeta);
            pane.addItem(new GuiItem(unequip, e -> {
                e.setCancelled(true);
                if(getSelectedMegaPerks(p).size() != 0) {
                    List<MegaPerk> currentPerks = getSelectedMegaPerks(p);
                    try{
                        currentPerks.remove(slot-1);
                        setSelectedMegaPerks(p, currentPerks);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                        openMainGui(p);
                    } catch (IndexOutOfBoundsException ex) {
                        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                        p.sendMessage(ChatColor.RED + "You don't have a perk equipped in this slot!");
                    }

                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                    p.sendMessage(ChatColor.RED + "You don't have a perk equipped in this slot!");
                }
            }), 4, 4);

            gui.addPane(pane);
            gui.copy().show(p);
        }
    }

    public void openPerkBuyGui(Player p, int slot) {
        ChestGui gui = new ChestGui(6, "Available Perks");
        gui.setOnGlobalClick(e -> {
            e.setCancelled(true);
        });
        StaticPane pane = new StaticPane(0, 0, 9, 6);
        for(int i = 0; i < Perk.getKeys().size(); i++) {
            final int j = i;
            Perk perk = Perk.getByName(Perk.getKeys().get(i));
            ItemStack visualItem = perk.instance.getGuiItem(p).clone();
            ItemMeta meta = visualItem.getItemMeta();
            assert meta != null;
            List<String> newLore = meta.getLore();
            assert newLore != null;
            if(ownsPerk(p, perk)) { // show "owned"
                newLore.add("");
                newLore.add(ChatColor.GREEN + "Owned");
                newLore.add(ChatColor.GRAY + "Click to equip");
            } else { // show cost
                newLore.add("");
                newLore.add(ChatColor.GOLD + "Costs " + perk.instance.getCost() + ChatColor.BOLD + " Giga Coins");
                newLore.add(ChatColor.GRAY + "Click to buy and equip");
            }
            if(perk.instance instanceof Upgradable && ((Upgradable) perk.instance).getLevel(p) < ((Upgradable) perk.instance).getMaxLevel()) {
                Upgradable upgradable = (Upgradable) perk.instance;
                newLore.add("");
                if(ownsPerk(p, perk)) {
                    newLore.add(ChatColor.AQUA + "Right click to upgrade to Level " + (upgradable.getLevel(p)+1));
                    if(upgradable.getNextHexidiumCost(upgradable.getLevel(p)) > 0) {
                        if(upgradable.getNextTeraCost(upgradable.getLevel(p)) > 0) {
                            if(upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) > 0) {
                                // htg
                                newLore.add(ChatColor.GRAY + "Upgrade cost: §x§2§7§F§D§2§9§l" + upgradable.getNextHexidiumCost(upgradable.getLevel(p)) + " " + ChatColor.translateAlternateColorCodes('&', "&x&2&7&F&D&2&9&lH&x&2&1&F&D&3&5&lE&x&1&C&F&D&4&1&lX&x&1&6&F&D&4&D&lI&x&1&1&F&D&5&8&lD&x&0&B&F&D&6&4&lI&x&0&6&F&D&7&0&lU&x&0&0&F&D&7&C&lM") + ChatColor.GRAY + ", " + "§x§1§F§7§0§F§B§l" + upgradable.getNextTeraCost(upgradable.getLevel(p)) + ChatColor.translateAlternateColorCodes('&', " &x&1&F&7&0&F&B&lT&x&1&D&8&3&F&B&le&x&1&C&9&6&F&C&lr&x&1&A&A&9&F&C&la&x&1&8&B&D&F&C&lC&x&1&6&D&0&F&C&lu&x&1&5&E&3&F&D&lb&x&1&3&F&6&F&D&les ") + ChatColor.GRAY + "and " + ChatColor.GOLD + ChatColor.BOLD + upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) + " Giga Coins");
                            } else {
                                // ht
                                newLore.add(ChatColor.GRAY + "Upgrade cost: §x§2§7§F§D§2§9§l" + upgradable.getNextHexidiumCost(upgradable.getLevel(p)) + " " + ChatColor.translateAlternateColorCodes('&', "&x&2&7&F&D&2&9&lH&x&2&1&F&D&3&5&lE&x&1&C&F&D&4&1&lX&x&1&6&F&D&4&D&lI&x&1&1&F&D&5&8&lD&x&0&B&F&D&6&4&lI&x&0&6&F&D&7&0&lU&x&0&0&F&D&7&C&lM ") + ChatColor.GRAY + "and " + "§x§1§F§7§0§F§B§l" + upgradable.getNextTeraCost(upgradable.getLevel(p)) + ChatColor.translateAlternateColorCodes('&', " &x&1&F&7&0&F&B&lT&x&1&D&8&3&F&B&le&x&1&C&9&6&F&C&lr&x&1&A&A&9&F&C&la&x&1&8&B&D&F&C&lC&x&1&6&D&0&F&C&lu&x&1&5&E&3&F&D&lb&x&1&3&F&6&F&D&le") + (upgradable.getNextTeraCost(upgradable.getLevel(p)) > 1 ? "s" : ""));
                            }
                        } else {
                            if(upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) > 0) {
                                // hg
                                newLore.add(ChatColor.GRAY + "Upgrade cost: §x§2§7§F§D§2§9§l" + upgradable.getNextHexidiumCost(upgradable.getLevel(p)) + " " + ChatColor.translateAlternateColorCodes('&', "&x&2&7&F&D&2&9&lH&x&2&1&F&D&3&5&lE&x&1&C&F&D&4&1&lX&x&1&6&F&D&4&D&lI&x&1&1&F&D&5&8&lD&x&0&B&F&D&6&4&lI&x&0&6&F&D&7&0&lU&x&0&0&F&D&7&C&lM") + ChatColor.GRAY + ", " + "§x§1§F§7§0§F§B§l " + ChatColor.GRAY + "and " + ChatColor.GOLD + ChatColor.BOLD + upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) + " Giga Coins");
                            } else {
                                // h
                                newLore.add(ChatColor.GRAY + "Upgrade cost: §x§2§7§F§D§2§9§l" + upgradable.getNextHexidiumCost(upgradable.getLevel(p)) + " " + ChatColor.translateAlternateColorCodes('&', "&x&2&7&F&D&2&9&lH&x&2&1&F&D&3&5&lE&x&1&C&F&D&4&1&lX&x&1&6&F&D&4&D&lI&x&1&1&F&D&5&8&lD&x&0&B&F&D&6&4&lI&x&0&6&F&D&7&0&lU&x&0&0&F&D&7&C&lM"));
                            }
                        }
                    } else {
                        if(upgradable.getNextTeraCost(upgradable.getLevel(p)) > 0) {
                            if(upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) > 0) {
                                // tg
                                newLore.add(ChatColor.GRAY + "Upgrade cost: §x§1§F§7§0§F§B§l" + upgradable.getNextTeraCost(upgradable.getLevel(p)) + ChatColor.translateAlternateColorCodes('&', " &x&1&F&7&0&F&B&lT&x&1&D&8&3&F&B&le&x&1&C&9&6&F&C&lr&x&1&A&A&9&F&C&la&x&1&8&B&D&F&C&lC&x&1&6&D&0&F&C&lu&x&1&5&E&3&F&D&lb&x&1&3&F&6&F&D&le") + (upgradable.getNextTeraCost(upgradable.getLevel(p)) > 1 ? "s" : "") + ChatColor.GRAY + " and " + ChatColor.GOLD + ChatColor.BOLD + upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) + " Giga Coins");
                            } else {
                                // t
                                newLore.add(ChatColor.GRAY + "Upgrade cost: §x§1§F§7§0§F§B§l" + upgradable.getNextTeraCost(upgradable.getLevel(p)) + ChatColor.translateAlternateColorCodes('&', " &x&1&F&7&0&F&B&lT&x&1&D&8&3&F&B&le&x&1&C&9&6&F&C&lr&x&1&A&A&9&F&C&la&x&1&8&B&D&F&C&lC&x&1&6&D&0&F&C&lu&x&1&5&E&3&F&D&lb&x&1&3&F&6&F&D&le") + (upgradable.getNextTeraCost(upgradable.getLevel(p)) > 1 ? "s" : ""));
                            }
                        } else {
                            if(upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) > 0) {
                                // g
                                newLore.add(ChatColor.GRAY + "Upgrade cost: " + ChatColor.GOLD + ChatColor.BOLD + upgradable.getNextRemainderGigaCost(upgradable.getLevel(p)) + " Giga Coins");
                            }
                        }
                    }
                } else {
                    newLore.add(ChatColor.AQUA + "This perk is upgradable!");
                }
            } else if(perk.instance instanceof Upgradable && ((Upgradable) perk.instance).getLevel(p) >= ((Upgradable) perk.instance).getMaxLevel()) {
                Upgradable upgradable = (Upgradable) perk.instance;
                newLore.add("");
                newLore.add(ChatColor.AQUA + "Level " + upgradable.getLevel(p));
                newLore.add(ChatColor.GRAY + "This perk's level is maxed out!");
            }
            meta.setLore(newLore);
            visualItem.setItemMeta(meta);
            GuiItem item = new GuiItem(visualItem, e -> {
                e.setCancelled(true);
                if(perk.instance instanceof Upgradable && (e.getAction() == InventoryAction.PICKUP_HALF || e.getAction() == InventoryAction.PICKUP_SOME)) {
                    if(ownsPerk(p, perk)) {
                        Upgradable upgradable = (Upgradable) perk.instance;
                        if(upgradable.getLevel(p) < upgradable.getMaxLevel()) {
                            boolean success = upgradable.upgrade(p);
                            if(success) {
                                p.sendMessage(ChatColor.GREEN + "Upgraded perk to level " + upgradable.getLevel(p) + "!");
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                                p.closeInventory();
                                openPerkBuyGui(p, slot);
                            } else {
                                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                                p.sendMessage(ChatColor.RED + "You can't afford this!");
                            }
                        }
                    } else {
                        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                        p.sendMessage(ChatColor.RED + "You have to buy the perk first!");
                    }
                } else {
                    List<Perk> selectedPerks = getSelectedPerks(p);
                    if(selectedPerks.contains(perk)) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25f, 0.5f);
                        p.sendMessage(ChatColor.RED + "You already have this perk equipped!");
                        return;
                    }
                    if(BoxPlugin.instance.getGhostTokenManager().hasGhostItems(p) && selectedPerks.size()>0) {
                        p.sendMessage(ChatColor.RED + "You have ghost items in your inventory! You can only equip one perk!");
                        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                        return;
                    }
                    if(ownsPerk(p, perk)) {
                        if(selectedPerks.size() < slot) {
                            selectedPerks.add(perk);
                        } else {
                            selectedPerks.set(slot-1, perk);
                        }
                        setSelectedPerks(p, selectedPerks);
                        p.sendMessage(ChatColor.GREEN + "Equipped perk!");
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                        openMainGui(p);
                    } else {
                        // this deducts coins but does not add the perk
                        boolean canBuy = buyPerk(perk, p);
                        if(canBuy) {
                            addPerk(p, perk);
                            if(selectedPerks.size() < slot) {
                                selectedPerks.add(perk);
                            } else {
                                selectedPerks.set(slot-1, perk);
                            }
                            setSelectedPerks(p, selectedPerks);
                            p.sendMessage(ChatColor.GREEN + "Bought perk!");
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                            openMainGui(p);
                        } else {
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25f, 0.5f);
                            p.sendMessage(ChatColor.RED + "You don't have enough " + ChatColor.GOLD + "" + ChatColor.BOLD + "Giga Coins");
                        }
                    }
                }
            });
            int x = 0;
            int y = 0;
            if(j == 2 || j == 3 || j == 6 || j == 7) {
                x+=1;
            }
            if(j >= 4) {
                y+=1;
                x-=4;
            }
            pane.addItem(item, j+2+x, 1+y);
        }
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta meta = cancel.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Go back");
        cancel.setItemMeta(meta);
        pane.addItem(new GuiItem(cancel, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openMainGui(p);
        }), 4, 5);

        ItemStack unequip = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta unequipMeta = unequip.getItemMeta();
        unequipMeta.setDisplayName(ChatColor.RED + "Unequip Perk");
        unequip.setItemMeta(unequipMeta);
        pane.addItem(new GuiItem(unequip, e -> {
            e.setCancelled(true);
            if(getSelectedPerks(p).size() != 0) {
                List<Perk> currentPerks = getSelectedPerks(p);
                try{
                    currentPerks.remove(slot-1);
                    setSelectedPerks(p, currentPerks);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
                    openMainGui(p);
                } catch (IndexOutOfBoundsException ex) {
                    p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                    p.sendMessage(ChatColor.RED + "You don't have a perk equipped in this slot!");
                }

            } else {
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                p.sendMessage(ChatColor.RED + "You don't have a perk equipped in this slot!");
            }
        }), 4, 4);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    // callback for clicking perk slot
    public void clickPerkSlot(InventoryClickEvent e, int slot) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        e.setCancelled(true);
        openPerkBuyGui(p, slot);
    }

    public void clickMegaPerkSlot(InventoryClickEvent e, int slot) {
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
        e.setCancelled(true);
        openMegaPerkBuyGui(p, slot);
    }

    /**
     * Check if a perk can be bought, and buy it if possible
     * @param perk The perk being purchased
     * @param p The player making the purchase
     * @return True if the purchase is valid, false if not (doesn't handle giving user item)
     */
    public boolean buyPerk(Perk perk, Player p) {
        int gigaCoinsHeld = 0;
        int cost = perk.instance.getCost();
        for(ItemStack item : p.getInventory().getContents()) {
            if(Util.isGigaCoin(item)) {
                gigaCoinsHeld += item.getAmount();
            }
        }
        if(gigaCoinsHeld >= cost) {
            for(ItemStack item : p.getInventory().getContents()) {
                if(cost == 0) return true;
                if(Util.isGigaCoin(item)) {
                    int amount = item.getAmount();
                    for(int i = 0; i < amount; i++) {
                        cost--;
                        item.setAmount(item.getAmount() - 1);
                        if(cost == 0) return true;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean buyMegaPerk(MegaPerk perk, Player p) {
        int herbsHeld = 0;
        int cost = perk.instance.getCost();
        for(ItemStack item: p.getInventory().getContents()) {
            if(Util.isGhastlyHerb(item)) {
                herbsHeld += item.getAmount();
            }
        }
        if(herbsHeld >= cost) {
            for(ItemStack item : p.getInventory().getContents()) {
                if(cost == 0) return true;
                if(Util.isGhastlyHerb(item)) {
                    int amount = item.getAmount();
                    for(int i = 0; i < amount; i++) {
                        cost--;
                        item.setAmount(item.getAmount() - 1);
                        if(cost == 0) return true;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Perk getPerkBySlot(Player p, int slot) {
        return getPurchasedPerks(p).get(slot - 1);
    }

    public boolean ownsPerk(Player p, Perk perk) {
        return getPurchasedPerks(p).contains(perk);
    }

    public boolean ownsMegaPerk(Player p, MegaPerk perk) {
        return getPurchasedMegaPerks(p).contains(perk);
    }

    public void addPerk(Player p, Perk perk) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey()), PersistentDataType.INTEGER, 1);
    }

    public void addMegaPerk(Player p, MegaPerk perk) {
        p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, perk.instance.getKey()), PersistentDataType.INTEGER, 1);
    }


    public void setSelectedPerks(Player p, List<Perk> perks) {
        List<Perk> prevPerks = getSelectedPerks(p);
        if(perks.size() == 0) {
            p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "selected_perks"));
        } else {
            p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "selected_perks"), PersistentDataType.STRING, String.join("\n", perks.stream().map(pe -> pe.instance.getKey()).collect(Collectors.toList())));
        }
        for(Perk perk : perks) {
            perk.instance.onEquip(p);
        }
        for(Perk perk : prevPerks) { // todo test
            if(!perks.contains(perk)) {
                perk.instance.onUnequip(p);
            }
        }
    }

    public void setSelectedMegaPerks(Player p, List<MegaPerk> perks) {
        List<MegaPerk> prevPerks = getSelectedMegaPerks(p);
        if(perks.size() == 0) {
            p.getPersistentDataContainer().remove(new NamespacedKey(BoxPlugin.instance, "selected_megaperks"));
        } else {
            p.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "selected_megaperks"), PersistentDataType.STRING, String.join("\n", perks.stream().map(pe -> pe.instance.getKey()).collect(Collectors.toList())));
        }
        for(MegaPerk perk : perks) {
            perk.instance.onEquip(p);
        }
        for(MegaPerk perk : prevPerks) { // todo test
            if(!perks.contains(perk)) {
                perk.instance.onUnequip(p);
            }
        }
    }


    public List<Perk> getPurchasedPerks(Player p) {
        List<Perk> perks = new ArrayList<Perk>();
        for(String key : Perk.getKeys()) {
            if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, key), PersistentDataType.INTEGER) && p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, key), PersistentDataType.INTEGER) == 1) {
                perks.add(Perk.getByName(key));
            }
        }
        return perks;
    }

    public List<MegaPerk> getPurchasedMegaPerks(Player p) {
        List<MegaPerk> perks = new ArrayList<MegaPerk>();
        for(String key : MegaPerk.getKeys()) {
            if(p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, key), PersistentDataType.INTEGER) && p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, key), PersistentDataType.INTEGER) == 1) {
                perks.add(MegaPerk.getByName(key));
            }
        }
        return perks;
    }

    // stored as list of perks separated by \n, inside one string "selected_perks"
    public ArrayList<Perk> getSelectedPerks(Player p) {
        if(!p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "selected_perks"), PersistentDataType.STRING)) {
            return new ArrayList<>();
        } else {
            List<Perk> l = Arrays.stream(p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "selected_perks"), PersistentDataType.STRING).split("\n")).map(s -> Perk.getByName(s)).collect(Collectors.toList());
            ArrayList<Perk> newList = new ArrayList<Perk>(l);
            return newList;
        }
    }

    // stored as list of perks separated by \n, inside one string "selected_megaperks"
    public ArrayList<MegaPerk> getSelectedMegaPerks(Player p) {
        if(!p.getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "selected_megaperks"), PersistentDataType.STRING)) {
            return new ArrayList<>();
        } else {
            List<MegaPerk> l = Arrays.stream(p.getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "selected_megaperks"), PersistentDataType.STRING).split("\n")).map(s -> MegaPerk.getByName(s)).collect(Collectors.toList());
            ArrayList<MegaPerk> newList = new ArrayList<>(l);
            return newList;
        }
    }

    public MegaPerk getMegaPerk(MegaPerk megaPerk){
        return megaPerk;
    }


}
