package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customEnchants.CustomEnchantsMain;
import me.twostinkysocks.boxplugin.customEnchants.Enchants.OverGrowthEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ElementalUIManager {
    ItemStack backround = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    GuiItem backroundGUI = new GuiItem(backround, e -> {
        e.setCancelled(true);
    });

    public void openMainGui(Player p) {
        ChestGui gui = new ChestGui(5, "Elemental Emporium");
        StaticPane pane = new StaticPane(9, 5);

        ItemStack plantElement = new ItemStack(Material.AZALEA_LEAVES);
        ItemMeta elementMeta = plantElement.getItemMeta();
        elementMeta.setDisplayName(ChatColor.GREEN + "Plant Element");
        elementMeta.addEnchant(Enchantment.MENDING, 1, true);
        elementMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        plantElement.setItemMeta(elementMeta);

        ItemStack fireElement = new ItemStack(Material.MAGMA_BLOCK);
        elementMeta = fireElement.getItemMeta();
        elementMeta.setDisplayName(ChatColor.DARK_RED + "Fire Element");
        elementMeta.addEnchant(Enchantment.MENDING, 1, true);
        elementMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        fireElement.setItemMeta(elementMeta);

        ItemStack iceElement = new ItemStack(Material.PACKED_ICE);
        elementMeta = iceElement.getItemMeta();
        elementMeta.setDisplayName(ChatColor.AQUA + "Ice Element");
        elementMeta.addEnchant(Enchantment.MENDING, 1, true);
        elementMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        iceElement.setItemMeta(elementMeta);

        ItemStack waterElement = new ItemStack(Material.WATER_BUCKET);
        elementMeta = waterElement.getItemMeta();
        elementMeta.setDisplayName(ChatColor.BLUE + "Water Element");
        elementMeta.addEnchant(Enchantment.MENDING, 1, true);
        elementMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        waterElement.setItemMeta(elementMeta);

        ItemStack lightningElement = new ItemStack(Material.YELLOW_DYE);
        elementMeta = lightningElement.getItemMeta();
        elementMeta.setDisplayName(ChatColor.YELLOW + "Lightning Element");
        elementMeta.addEnchant(Enchantment.MENDING, 1, true);
        elementMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        lightningElement.setItemMeta(elementMeta);

        ItemStack voidElement = new ItemStack(Material.CRYING_OBSIDIAN);
        elementMeta = voidElement.getItemMeta();
        elementMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Void Element");
        elementMeta.addEnchant(Enchantment.MENDING, 1, true);
        elementMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        voidElement.setItemMeta(elementMeta);

        ItemStack otherElement = new ItemStack(Material.NETHER_STAR);
        elementMeta = otherElement.getItemMeta();
        elementMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Titan");
        elementMeta.setLore(List.of(
                ChatColor.AQUA + "Deal " + CustomEnchantsMain.Enchant.Titan.instance.getDamageAmpFromTotalLevel(100) + "% of your max health",
                ChatColor.AQUA + "per level as bonus damage on-hit. ",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        elementMeta.addEnchant(Enchantment.MENDING, 1, true);
        elementMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        otherElement.setItemMeta(elementMeta);

        gui.setOnGlobalClick(e -> {
            e.setCancelled(true);
        });

        GuiItem plantGui = new GuiItem(plantElement, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openNatureGui(p);
        });

        GuiItem fireGui = new GuiItem(fireElement, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openFireGui(p);
        });

        GuiItem iceGui = new GuiItem(iceElement, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openIceGui(p);
        });

        GuiItem waterGui = new GuiItem(waterElement, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openWaterGui(p);
        });

        GuiItem lightningGui = new GuiItem(lightningElement, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openZeusGui(p);
        });

        GuiItem voidGui = new GuiItem(voidElement, e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openVoidGui(p);
        });

        GuiItem specialGui = new GuiItem(otherElement, e -> {
            e.setCancelled(true);
        });

        Set<String> usedSlots = Set.of(
                "3,0",
                "5,0",
                "2,2",
                "4,2",
                "6,2",
                "3,4",
                "5,4"
        );
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (usedSlots.contains(i + "," + j)) {
                    continue;
                }
                pane.addItem(backroundGUI, i, j);
            }
        }

        pane.addItem(fireGui, 3, 0);
        pane.addItem(plantGui, 5, 0);
        pane.addItem(iceGui, 2, 2);
        pane.addItem(specialGui, 4, 2);
        pane.addItem(voidGui, 6, 2);
        pane.addItem(waterGui, 3, 4);
        pane.addItem(lightningGui, 5, 4);

        gui.addPane(pane);

        gui.copy().show(p);
    }

    public void openNatureGui(Player p){
        ChestGui gui = new ChestGui(5, "Nature Enchants");
        StaticPane pane = new StaticPane(9,5);

        ItemStack overgrowth = new ItemStack(Material.JUNGLE_SAPLING);
        ItemMeta ogmeta = overgrowth.getItemMeta();
        ogmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Overgrowth");
        ogmeta.setLore(List.of(//says lvl 100 cuz 100* % val
                ChatColor.AQUA + "Each level of Overgrowth has a " + CustomEnchantsMain.Enchant.Overgrowth.instance.getChanceFromTotalLevel(1) + "% chance",
                ChatColor.AQUA + "to heal " + Math.round(BoxPlugin.instance.getEnchantOvergrowth().getHealFromTotalLevel(100)*100)/100 + "% of the damage you took per level",
                ChatColor.AQUA + "back over the next 5 seconds.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        overgrowth.setItemMeta(ogmeta);

        ItemStack prickle = new ItemStack(Material.CACTUS);
        ItemMeta prickmeta = prickle.getItemMeta();
        prickmeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Prickle");
        prickmeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Prickle deals " + CustomEnchantsMain.Enchant.Prickle.instance.getDamageFromTotalLevel(1),
                ChatColor.AQUA + "Cactus damage on-hit."
        ));
        prickle.setItemMeta(prickmeta);

        ItemStack bramble = new ItemStack(Material.DEAD_BUSH);
        ItemMeta bramblemeta = bramble.getItemMeta();
        bramblemeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Bramble");
        bramblemeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Bramble has a " + CustomEnchantsMain.Enchant.Bramble.instance.getChanceFromTotalLevel(1) + "% chance",
                ChatColor.AQUA + "to deal " + CustomEnchantsMain.Enchant.Bramble.instance.getDamageFromTotalLevel(1) + " Cactus damage per level",
                ChatColor.AQUA + "when attacked.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        bramble.setItemMeta(bramblemeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Nature Enchants Overview:");
        elementInfoItemMeta.setLore(List.of(
                ChatColor.AQUA + "Each piece of armor with a Nature Enchant",
                ChatColor.AQUA + "will take increased damage from the Magma Enchant,",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Cactus damage is severally mitigated by armor and protection,",
                ChatColor.AQUA + "However Cactus damage does bonus damage to, Water Enchants"
        ));
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack godThorns = new ItemStack(Material.LIME_GLAZED_TERRACOTTA);
        ItemMeta godThornsMeta = godThorns.getItemMeta();
        godThornsMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "God Thorns");
        godThornsMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Nature + Lightning for God Thorns: When hit, " + CustomEnchantsMain.Enchant.GodThorns.instance.getChanceFromTotalLevel(1) + "%",
                ChatColor.AQUA + "chance to reflect " + Math.round(CustomEnchantsMain.Enchant.GodThorns.instance.getDamageFromTotalLevel(100)*100)/100 +  "% of the damage taken",
                ChatColor.AQUA + "back to the attacker as Lightning damage, Both the chance and",
                ChatColor.AQUA + "damage is per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        godThorns.setItemMeta(godThornsMeta);

        ItemStack fireThorns = new ItemStack(Material.CRIMSON_STEM);
        ItemMeta fireThornsItemMeta = fireThorns.getItemMeta();
        fireThornsItemMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Lava Thorns");
        fireThornsItemMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Nature + Fire for Lava Thorns: When hit, " + CustomEnchantsMain.Enchant.LavaThorns.instance.getChanceFromTotalLevel(1) + "%",
                ChatColor.AQUA + "chance to deal " + CustomEnchantsMain.Enchant.LavaThorns.instance.getDamageFromTotalLevel(1) +  " Lava damage back",
                ChatColor.AQUA + "to the attacker, Both the chance and damage is per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        fireThorns.setItemMeta(fireThornsItemMeta);

        ItemStack iceThorns = new ItemStack(Material.SNOWBALL);
        ItemMeta iceThornsItemMeta = iceThorns.getItemMeta();
        iceThornsItemMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Ice Thorns");
        iceThornsItemMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Nature + Ice for Ice Thorns: When hit, " + CustomEnchantsMain.Enchant.IceThorns.instance.getChanceFromTotalLevel(1) + "%",
                ChatColor.AQUA + "chance to deal " + CustomEnchantsMain.Enchant.IceThorns.instance.getDamageFromTotalLevel(1) +  " Ice damage back",
                ChatColor.AQUA + "to the attacker, Both the chance and damage is per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        iceThorns.setItemMeta(iceThornsItemMeta);

        GuiItem godThornsGui = new GuiItem(godThorns.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem fireThornsGui = new GuiItem(fireThorns.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem iceThornsGui = new GuiItem(iceThorns.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem overGrownGui = new GuiItem(overgrowth.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem prickleGui = new GuiItem(prickle.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem brambleGui = new GuiItem(bramble.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem infoGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openMainGui(p);
        });

        Set<String> usedSlots = Set.of(
                "4,0",
                "2,2",
                "4,2",
                "6,2",
                "0,4",
                "3,4",
                "4,4",
                "5,4"
        );
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (usedSlots.contains(i + "," + j)) {
                    continue;
                }
                pane.addItem(backroundGUI, i, j);
            }
        }

        pane.addItem(infoGui, 4, 0);
        pane.addItem(overGrownGui, 2, 2);
        pane.addItem(prickleGui, 4, 2);
        pane.addItem(brambleGui, 6, 2);
        pane.addItem(cancelGui, 0, 4);
        pane.addItem(godThornsGui, 3, 4);
        pane.addItem(iceThornsGui, 4, 4);
        pane.addItem(fireThornsGui, 5, 4);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openFireGui(Player p){
        ChestGui gui = new ChestGui(5, "Fire Enchants");
        StaticPane pane = new StaticPane(9,5);

        ItemStack magma = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta magmaMeta = magma.getItemMeta();
        magmaMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Magma");
        magmaMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Magma deals " + CustomEnchantsMain.Enchant.Magma.instance.getDamageFromTotalLevel(1),
                ChatColor.AQUA + "Lava damage on-hit."
        ));
        magma.setItemMeta(magmaMeta);

        ItemStack fireBorn = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta fireBornMeta = fireBorn.getItemMeta();
        fireBornMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Fire Born");
        fireBornMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Fire Born increases all Lava damage",
                ChatColor.AQUA + "by " + Math.round((CustomEnchantsMain.Enchant.FireBorn.instance.getDamageAmpFromTotalLevel(1)-1.0)*10000)/100 + "%",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        fireBorn.setItemMeta(fireBornMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Fire Enchants Overview:");
        elementInfoItemMeta.setLore(List.of(
                ChatColor.AQUA + "Each piece of armor with a Fire Enchant",
                ChatColor.AQUA + "will take increased damage from the Asphyxiation Enchant,",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Lava damage is mitigated by armor and protection, However",
                ChatColor.AQUA + "Lava damage does bonus damage to, Ice and Nature Enchants",
                ChatColor.AQUA + "",
                ChatColor.RED + "NOTE: Entities with Fire Resistance are IMMUNE to this damage!"
        ));
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack sublimation = new ItemStack(Material.SNOW_BLOCK);
        ItemMeta sublimationMeta = sublimation.getItemMeta();
        sublimationMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Sublimation");
        sublimationMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Sublimation,",
                ChatColor.AQUA + "Click to learn more."
        ));
        sublimation.setItemMeta(sublimationMeta);

        ItemStack fireThorns = new ItemStack(Material.CRIMSON_STEM);
        ItemMeta fireThornsItemMeta = fireThorns.getItemMeta();
        fireThornsItemMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Lava Thorns");
        fireThornsItemMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Nature + Fire for Lava Thorns: When hit, " + CustomEnchantsMain.Enchant.LavaThorns.instance.getChanceFromTotalLevel(1) + "%",
                ChatColor.AQUA + "chance to deal " + CustomEnchantsMain.Enchant.LavaThorns.instance.getDamageFromTotalLevel(1) +  " Lava damage back",
                ChatColor.AQUA + "to the attacker, Both the chance and damage is per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        fireThorns.setItemMeta(fireThornsItemMeta);

        ItemStack steam = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta steamMeta = steam.getItemMeta();
        steamMeta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Feather Weight");
        steamMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Feather Weight,",
                ChatColor.AQUA + "Click to learn more."
        ));
        steam.setItemMeta(steamMeta);

        ItemStack eventHor = new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
        ItemMeta eventHorMeta = eventHor.getItemMeta();
        eventHorMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Event Horizon");
        eventHorMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Event Horizon,",
                ChatColor.AQUA + "Click to learn more."
        ));
        eventHor.setItemMeta(eventHorMeta);

        GuiItem eventHorGUI = new GuiItem(eventHor.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openEventHorGui(p, false);
        });

        GuiItem fireThornsGui = new GuiItem(fireThorns.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem sublimationGui = new GuiItem(sublimation.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openSublimationGui(p, true);
        });

        GuiItem magmaGui = new GuiItem(magma.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem fireBornGui = new GuiItem(fireBorn.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem featherGui = new GuiItem(steam.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openSteanGui(p, true);
        });

        GuiItem infoGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openMainGui(p);
        });

        Set<String> usedSlots = Set.of(
                "4,0",
                "3,2",
                "5,2",
                "2,4",
                "0,4",
                "3,4",
                "5,4",
                "6,4"
        );
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (usedSlots.contains(i + "," + j)) {
                    continue;
                }
                pane.addItem(backroundGUI, i, j);
            }
        }

        pane.addItem(infoGui, 4, 0);
        pane.addItem(magmaGui, 3, 2);
        pane.addItem(fireBornGui, 5, 2);
        pane.addItem(eventHorGUI, 2, 4);
        pane.addItem(cancelGui, 0, 4);
        pane.addItem(sublimationGui, 3, 4);
        pane.addItem(featherGui, 5, 4);
        pane.addItem(fireThornsGui, 6, 4);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openIceGui(Player p){
        ChestGui gui = new ChestGui(5, "Ice Enchants");
        StaticPane pane = new StaticPane(9,5);

        ItemStack iceAsp = new ItemStack(Material.ICE);
        ItemMeta iceAspmeta = iceAsp.getItemMeta();
        iceAspmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Ice Aspect");
        iceAspmeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Ice Aspect deals " + CustomEnchantsMain.Enchant.IceAspect.instance.getDamageFromTotalLevel(1),
                ChatColor.AQUA + "Ice damage on-hit."
        ));
        iceAsp.setItemMeta(iceAspmeta);

        ItemStack born = new ItemStack(Material.PACKED_ICE);
        ItemMeta bornMeta = born.getItemMeta();
        bornMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Ice Born");
        bornMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Ice Born increases all Ice damage",
                ChatColor.AQUA + "by " + Math.round((CustomEnchantsMain.Enchant.IceBorn.instance.getDamageAmpFromTotalLevel(1)-1.0)*10000)/100 + "%, It also increases",
                ChatColor.AQUA + "freeze stacks by " + Math.round((CustomEnchantsMain.Enchant.IceBorn.instance.getStackingSpeedFromTotalLevel(1)-1)*10000)/100 + "% per hit",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        born.setItemMeta(bornMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Ice Enchants Overview:");
        elementInfoItemMeta.setLore(List.of(
                ChatColor.AQUA + "Each piece of armor with a Ice Enchant",
                ChatColor.AQUA + "will take increased damage from the Magma Enchant,",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Ice damage is mitigated by protection, but ignores armor",
                ChatColor.AQUA + "Ice damage also does bonus damage to, Water and Void Enchants",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Each instance of Ice damage applies freeze stacks to the target which decay",
                ChatColor.AQUA + "over time. When the target is frozen, their FOV is decreased, movement is",
                ChatColor.AQUA + "slowed, and Ice damage is increased by 30%, if the freeze stacks go over",
                ChatColor.AQUA + "3.5x the vanilla limit, damage is increased by 60%"
        ));
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack sublimation = new ItemStack(Material.SNOW_BLOCK);
        ItemMeta sublimationMeta = sublimation.getItemMeta();
        sublimationMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Sublimation");
        sublimationMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Sublimation,",
                ChatColor.AQUA + "Click to learn more."
        ));
        sublimation.setItemMeta(sublimationMeta);

        ItemStack iceThorns = new ItemStack(Material.SNOWBALL);
        ItemMeta iceThornsItemMeta = iceThorns.getItemMeta();
        iceThornsItemMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Ice Thorns");
        iceThornsItemMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Nature + Ice for Ice Thorns: When hit, " + CustomEnchantsMain.Enchant.IceThorns.instance.getChanceFromTotalLevel(1) + "%",
                ChatColor.AQUA + "chance to deal " + CustomEnchantsMain.Enchant.IceThorns.instance.getDamageFromTotalLevel(1) +  " Ice damage back",
                ChatColor.AQUA + "to the attacker, Both the chance and damage is per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        iceThorns.setItemMeta(iceThornsItemMeta);

        ItemStack arctic = new ItemStack(Material.PACKED_ICE);
        ItemMeta arcticMeta = arctic.getItemMeta();
        arcticMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Arctic");
        arcticMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Arctic,",
                ChatColor.AQUA + "Click to learn more."
        ));
        arctic.setItemMeta(arcticMeta);

        ItemStack blackIce = new ItemStack(Material.BLUE_GLAZED_TERRACOTTA);
        ItemMeta blackIceMeta = blackIce.getItemMeta();
        blackIceMeta.setDisplayName(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Black Ice");
        blackIceMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Ice + Void for Black Ice: Deal " + CustomEnchantsMain.Enchant.BlackIce.instance.getDamageAmpFromTotalLevel(100) + "% of",
                ChatColor.AQUA + "your missing health as bonus Ice damage per level. This",
                ChatColor.AQUA + "damage also scales with freeze stacks and your weapon",
                ChatColor.AQUA + "must have Ice Aspect to work.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        blackIce.setItemMeta(blackIceMeta);

        GuiItem blakIceGUI = new GuiItem(blackIce.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem iceThornsGui = new GuiItem(iceThorns.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem sublimationGui = new GuiItem(sublimation.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openSublimationGui(p, false);
        });

        GuiItem iceAspGui = new GuiItem(iceAsp.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem iceBornGui = new GuiItem(born.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem arcticGUI = new GuiItem(arctic.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openArcticGui(p, true);
        });

        GuiItem infoGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openMainGui(p);
        });

        Set<String> usedSlots = Set.of(
                "4,0",
                "3,2",
                "5,2",
                "2,4",
                "0,4",
                "3,4",
                "5,4",
                "6,4"
        );
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (usedSlots.contains(i + "," + j)) {
                    continue;
                }
                pane.addItem(backroundGUI, i, j);
            }
        }

        pane.addItem(infoGui, 4, 0);
        pane.addItem(iceAspGui, 3, 2);
        pane.addItem(iceBornGui, 5, 2);
        pane.addItem(blakIceGUI, 2, 4);
        pane.addItem(cancelGui, 0, 4);
        pane.addItem(sublimationGui, 3, 4);
        pane.addItem(arcticGUI, 5, 4);
        pane.addItem(iceThornsGui, 6, 4);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openWaterGui(Player p){
        ChestGui gui = new ChestGui(5, "Water Enchants");
        StaticPane pane = new StaticPane(9,5);

        ItemStack Asphyxiation = new ItemStack(Material.WATER_BUCKET);
        ItemMeta AsphyxiationMeta = Asphyxiation.getItemMeta();
        AsphyxiationMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Asphyxiation");
        AsphyxiationMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Asphyxiation deals " + CustomEnchantsMain.Enchant.Asphyxiate.instance.getDamageFromTotalLevel(1),
                ChatColor.AQUA + "Drown damage on-hit."
        ));
        Asphyxiation.setItemMeta(AsphyxiationMeta);

        ItemStack born = new ItemStack(Material.SPONGE);
        ItemMeta bornMeta = born.getItemMeta();
        bornMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Water Born");
        bornMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Water Born increases Drown stacks",
                ChatColor.AQUA + "by " + Math.round((CustomEnchantsMain.Enchant.WaterBorn.instance.getStackingSpeedFromTotalLevel(1)-1.0)*10000)/100 + "%, per hit.",
                ChatColor.AQUA + "When a full set of Water Born is equipped, gain movement",
                ChatColor.AQUA + "speed scaling with depth strider and Water Born level",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        born.setItemMeta(bornMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Water Enchants Overview:");
        elementInfoItemMeta.setLore(List.of(
                ChatColor.AQUA + "Each piece of armor with a Water Enchant",
                ChatColor.AQUA + "will take increased damage from Lightning,",
                ChatColor.AQUA + "Nature, and Ice Enchants",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Drown damage is mitigated by protection, but ignores armor",
                ChatColor.AQUA + "Drown damage also does bonus damage to, Fire Enchants",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Each instance of Drown damage applies Drown stacks to the target which decay",
                ChatColor.AQUA + "over time. When the target is drowning, Asphyxiation damage is increased by 50%",
                ChatColor.AQUA + "If the drown stacks go over 3x the vanilla limit then damage is increased by 70%",
                ChatColor.AQUA + "and the target gets weakness 4.",
                ChatColor.AQUA + "",
                ChatColor.RED + "NOTE: Entities with Water Breathing are IMMUNE to this damage!"
        ));
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack steam = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta steamMeta = steam.getItemMeta();
        steamMeta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Feather Weight");
        steamMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Feather Weight,",
                ChatColor.AQUA + "Click to learn more."
        ));
        steam.setItemMeta(steamMeta);

        ItemStack arctic = new ItemStack(Material.PACKED_ICE);
        ItemMeta arcticMeta = arctic.getItemMeta();
        arcticMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Arctic");
        arcticMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Arctic,",
                ChatColor.AQUA + "Click to learn more."
        ));
        arctic.setItemMeta(arcticMeta);

        ItemStack stormBorn = new ItemStack(Material.DARK_PRISMARINE);
        ItemMeta stormBornMeta = stormBorn.getItemMeta();
        stormBornMeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Storm Born");
        stormBornMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Storm Born,",
                ChatColor.AQUA + "Click to learn more."
        ));
        stormBorn.setItemMeta(stormBornMeta);

        GuiItem stormGUI = new GuiItem(stormBorn.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openStormGui(p, true);
        });

        GuiItem arcticGUI = new GuiItem(arctic.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openArcticGui(p, false);
        });

        GuiItem steamGUI = new GuiItem(steam.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openSteanGui(p, false);
        });

        GuiItem asphyxiationGui = new GuiItem(Asphyxiation.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem waterBornGui = new GuiItem(born.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem infoGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openMainGui(p);
        });

        Set<String> usedSlots = Set.of(
                "4,0",
                "3,2",
                "5,2",
                "0,4",
                "3,4",
                "4,4",
                "5,4"
        );
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (usedSlots.contains(i + "," + j)) {
                    continue;
                }
                pane.addItem(backroundGUI, i, j);
            }
        }

        pane.addItem(infoGui, 4, 0);
        pane.addItem(asphyxiationGui, 3, 2);
        pane.addItem(waterBornGui, 5, 2);
        pane.addItem(cancelGui, 0, 4);
        pane.addItem(steamGUI, 3, 4);
        pane.addItem(arcticGUI, 4, 4);
        pane.addItem(stormGUI, 5, 4);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openZeusGui(Player p){
        ChestGui gui = new ChestGui(5, "Lightning Enchants");
        StaticPane pane = new StaticPane(9,5);

        ItemStack lightning = new ItemStack(Material.LIGHTNING_ROD);
        ItemMeta lightningMeta = lightning.getItemMeta();
        lightningMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Aspect of the Gods");
        lightningMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Aspect of the Gods deals " + CustomEnchantsMain.Enchant.Zeus.instance.getDamageFromTotalLevel(100) + "%",
                ChatColor.AQUA + "Max health Lightning damage with a " + CustomEnchantsMain.Enchant.Zeus.instance.getChanceFromTotalLevel(100) +  "% chance",
                ChatColor.AQUA + "per level to activate on-hit.",
                ChatColor.AQUA + "Damage is capped at 175"
        ));
        lightning.setItemMeta(lightningMeta);

        ItemStack born = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta bornMeta = born.getItemMeta();
        bornMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "God Born");
        bornMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of God Born increases the chances of",
                ChatColor.AQUA + "Aspect of the Gods activating by " + CustomEnchantsMain.Enchant.GodBorn.instance.getChanceFromTotalLevel(100) + "%, per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        born.setItemMeta(bornMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Lightning Enchants Overview:");
        elementInfoItemMeta.setLore(List.of(
                ChatColor.AQUA + "Each piece of armor with a Lightning Enchant",
                ChatColor.AQUA + "will take increased damage from Void Enchants.",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Lightning damage is mitigated by protection, but ignores armor",
                ChatColor.AQUA + "Lightning damage also does bonus damage to Water Enchants"
        ));
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack godThorns = new ItemStack(Material.LIME_GLAZED_TERRACOTTA);
        ItemMeta godThornsMeta = godThorns.getItemMeta();
        godThornsMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "God Thorns");
        godThornsMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Nature + Lightning for God Thorns: When hit, " + CustomEnchantsMain.Enchant.GodThorns.instance.getChanceFromTotalLevel(1) + "%",
                ChatColor.AQUA + "chance to reflect " + Math.round(CustomEnchantsMain.Enchant.GodThorns.instance.getDamageFromTotalLevel(100)*100)/100 +  "% of the damage taken",
                ChatColor.AQUA + "back to the attacker as Lightning damage, Both the chance and",
                ChatColor.AQUA + "damage is per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        godThorns.setItemMeta(godThornsMeta);

        ItemStack divine = new ItemStack(Material.CRYING_OBSIDIAN);
        ItemMeta divinemeta = divine.getItemMeta();
        divinemeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Divine");
        divinemeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Divine,",
                ChatColor.AQUA + "Click to learn more."
        ));
        divine.setItemMeta(divinemeta);

        ItemStack stormBorn = new ItemStack(Material.DARK_PRISMARINE);
        ItemMeta stormBornMeta = stormBorn.getItemMeta();
        stormBornMeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Storm Born");
        stormBornMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Storm Born,",
                ChatColor.AQUA + "Click to learn more."
        ));
        stormBorn.setItemMeta(stormBornMeta);

        GuiItem stormGUI = new GuiItem(stormBorn.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openStormGui(p, false);
        });

        GuiItem divineGui = new GuiItem(divine.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openDivineGui(p, false);
        });

        GuiItem godThornsGui = new GuiItem(godThorns.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem aspectOfGodGui = new GuiItem(lightning.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem godBornGui = new GuiItem(born.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem infoGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openMainGui(p);
        });

        Set<String> usedSlots = Set.of(
                "4,0",
                "3,2",
                "5,2",
                "0,4",
                "3,4",
                "4,4",
                "5,4"
        );
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (usedSlots.contains(i + "," + j)) {
                    continue;
                }
                pane.addItem(backroundGUI, i, j);
            }
        }

        pane.addItem(infoGui, 4, 0);
        pane.addItem(aspectOfGodGui, 3, 2);
        pane.addItem(godBornGui, 5, 2);
        pane.addItem(cancelGui, 0, 4);
        pane.addItem(divineGui, 3, 4);
        pane.addItem(godThornsGui, 4, 4);
        pane.addItem(stormGUI, 5, 4);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openVoidGui(Player p){
        ChestGui gui = new ChestGui(5, "Void Enchants");
        StaticPane pane = new StaticPane(9,5);

        ItemStack voidAsp = new ItemStack(Material.OBSIDIAN);
        ItemMeta voidAspMeta = voidAsp.getItemMeta();
        voidAspMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Void Aspect");
        voidAspMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Void Aspect deals " + CustomEnchantsMain.Enchant.VoidAspect.instance.getDamageFromTotalLevel(1) + " Void",
                ChatColor.AQUA + "damage on-hit."

        ));
        voidAsp.setItemMeta(voidAspMeta);

        ItemStack born = new ItemStack(Material.ENDER_PEARL);
        ItemMeta bornMeta = born.getItemMeta();
        bornMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Void Born");
        bornMeta.setLore(List.of(
                ChatColor.AQUA + "Each level of Void Born increases the damage of",
                ChatColor.AQUA + "Void Aspect by " + Math.round((CustomEnchantsMain.Enchant.VoidBorn.instance.getDamageAmpFromTotalLevel(1)-1.0)*10000)/100 + "%, per level.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        born.setItemMeta(bornMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Void Enchants Overview:");
        elementInfoItemMeta.setLore(List.of(
                ChatColor.AQUA + "Each piece of armor with a Void Enchant",
                ChatColor.AQUA + "will take increased damage from Ice Enchants.",
                ChatColor.AQUA + "",
                ChatColor.AQUA + "Void damage ignores all resistances. Void damage",
                ChatColor.AQUA + "also does bonus damage to Lightning Enchants"
        ));
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack blackIce = new ItemStack(Material.BLUE_GLAZED_TERRACOTTA);
        ItemMeta blackIceMeta = blackIce.getItemMeta();
        blackIceMeta.setDisplayName(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Black Ice");
        blackIceMeta.setLore(List.of(
                ChatColor.AQUA + "Combine Ice + Void for Black Ice: Deal " + CustomEnchantsMain.Enchant.BlackIce.instance.getDamageAmpFromTotalLevel(100) + "% of",
                ChatColor.AQUA + "your missing health as bonus Ice damage per level. This",
                ChatColor.AQUA + "damage also scales with freeze stacks and your weapon",
                ChatColor.AQUA + "must have Ice Aspect to work.",
                ChatColor.GREEN + "Total level is cumulative over all armor."
        ));
        blackIce.setItemMeta(blackIceMeta);

        ItemStack divine = new ItemStack(Material.GILDED_BLACKSTONE);
        ItemMeta divinemeta = divine.getItemMeta();
        divinemeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Divine");
        divinemeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Divine,",
                ChatColor.AQUA + "Click to learn more."
        ));
        divine.setItemMeta(divinemeta);

        ItemStack eventHor = new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
        ItemMeta eventHorMeta = eventHor.getItemMeta();
        eventHorMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Event Horizon");
        eventHorMeta.setLore(List.of(
                ChatColor.AQUA + "This element builds into Event Horizon,",
                ChatColor.AQUA + "Click to learn more."
        ));
        eventHor.setItemMeta(eventHorMeta);

        GuiItem eventHorGUI = new GuiItem(eventHor.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openEventHorGui(p, true);
        });

        GuiItem divineGui = new GuiItem(divine.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openDivineGui(p, true);
        });

        GuiItem blackIceGui = new GuiItem(blackIce.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem voidAspectGui = new GuiItem(voidAsp.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem voidBornGui = new GuiItem(born.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem infoGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            openMainGui(p);
        });

        Set<String> usedSlots = Set.of(
                "4,0",
                "3,2",
                "5,2",
                "0,4",
                "3,4",
                "4,4",
                "5,4"
        );
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (usedSlots.contains(i + "," + j)) {
                    continue;
                }
                pane.addItem(backroundGUI, i, j);
            }
        }

        pane.addItem(infoGui, 4, 0);
        pane.addItem(voidAspectGui, 3, 2);
        pane.addItem(voidBornGui, 5, 2);
        pane.addItem(cancelGui, 0, 4);
        pane.addItem(divineGui, 3, 4);
        pane.addItem(eventHorGUI, 4, 4);
        pane.addItem(blackIceGui, 5, 4);

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openSublimationGui(Player p, boolean returnFire){
        ChestGui gui = new ChestGui(3, "Fire + Ice Enchants");
        StaticPane pane = new StaticPane(9,3);

        ItemStack brittle = new ItemStack(Material.ANVIL);
        ItemMeta brittleMeta = brittle.getItemMeta();
        brittleMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Brittle");
        brittleMeta.setLore(List.of(
                ChatColor.AQUA + "Every 3rd crit on a target activates brittle, dealing 250%",
                ChatColor.AQUA + "of your combined Ice damage + Lava damage as bonus Anvil damage."

        ));
        brittle.setItemMeta(brittleMeta);

        ItemStack armorEnch = new ItemStack(Material.POWDER_SNOW_BUCKET);
        ItemMeta armorEnchMeta = armorEnch.getItemMeta();
        armorEnchMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Sublimation");
        armorEnchMeta.setLore(List.of(
                ChatColor.AQUA + "Wear a full set of Sublimation for Brittle to work"
        ));
        armorEnch.setItemMeta(armorEnchMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Combine Ice + Lava:");
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        GuiItem mainEnchGui = new GuiItem(brittle.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem armorGui = new GuiItem(armorEnch.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem elementGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            if(returnFire){
                openFireGui(p);
            } else {
                openIceGui(p);
            }
        });

        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 3; j++){
                if(i == 3 && j == 1){
                    pane.addItem(mainEnchGui, i, j);
                } else if(i == 5 && j == 1){
                    pane.addItem(armorGui, i, j);
                } else if(i == 0 && j == 2){
                    pane.addItem(cancelGui, i, j);
                } else if(i == 4 && j == 0){
                    pane.addItem(elementGui, i, j);
                } else {
                    pane.addItem(backroundGUI, i, j);
                }
            }
        }

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openSteanGui(Player p, boolean returnFire){
        ChestGui gui = new ChestGui(3, "Fire + Water Enchants");
        StaticPane pane = new StaticPane(9,3);

        ItemStack cloud = new ItemStack(Material.MACE);
        ItemMeta cloudMeta = cloud.getItemMeta();
        cloudMeta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Cloud Burst");
        cloudMeta.setLore(List.of(
                ChatColor.AQUA + "Landing a crit when on the ground launches you up!",
                ChatColor.AQUA + "Useful with mace."

        ));
        cloud.setItemMeta(cloudMeta);

        ItemStack armorEnch = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta armorEnchMeta = armorEnch.getItemMeta();
        armorEnchMeta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Feather Weight");
        armorEnchMeta.setLore(List.of(
                ChatColor.AQUA + "Wear a full set of Feather Weight for Cloud Burst to work"
        ));
        armorEnch.setItemMeta(armorEnchMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Combine Water + Lava:");
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        GuiItem mainEnchGui = new GuiItem(cloud.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem armorGui = new GuiItem(armorEnch.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem elementGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            if(returnFire){
                openFireGui(p);
            } else {
                openWaterGui(p);
            }
        });

        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 3; j++){
                if(i == 3 && j == 1){
                    pane.addItem(mainEnchGui, i, j);
                } else if(i == 5 && j == 1){
                    pane.addItem(armorGui, i, j);
                } else if(i == 0 && j == 2){
                    pane.addItem(cancelGui, i, j);
                } else if(i == 4 && j == 0){
                    pane.addItem(elementGui, i, j);
                } else {
                    pane.addItem(backroundGUI, i, j);
                }
            }
        }

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openArcticGui(Player p, boolean returnIce){
        ChestGui gui = new ChestGui(3, "Ice + Water Enchants");
        StaticPane pane = new StaticPane(9,3);

        ItemStack cloud = new ItemStack(Material.CONDUIT);
        ItemMeta cloudMeta = cloud.getItemMeta();
        cloudMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Slippery");
        cloudMeta.setLore(List.of(
                ChatColor.AQUA + "When holding a weapon with Slippery, gain 40%",
                ChatColor.AQUA + "more bonus speed with water born."

        ));
        cloud.setItemMeta(cloudMeta);

        ItemStack armorEnch = new ItemStack(Material.BLUE_ICE);
        ItemMeta armorEnchMeta = armorEnch.getItemMeta();
        armorEnchMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Arctic");
        armorEnchMeta.setLore(List.of(
                ChatColor.AQUA + "Wear a full set of Arctic for Slippery to work"
        ));
        armorEnch.setItemMeta(armorEnchMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Combine Water + Ice:");
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        GuiItem mainEnchGui = new GuiItem(cloud.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem armorGui = new GuiItem(armorEnch.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem elementGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            if(returnIce){
                openIceGui(p);
            } else {
                openWaterGui(p);
            }
        });

        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 3; j++){
                if(i == 3 && j == 1){
                    pane.addItem(mainEnchGui, i, j);
                } else if(i == 5 && j == 1){
                    pane.addItem(armorGui, i, j);
                } else if(i == 0 && j == 2){
                    pane.addItem(cancelGui, i, j);
                } else if(i == 4 && j == 0){
                    pane.addItem(elementGui, i, j);
                } else {
                    pane.addItem(backroundGUI, i, j);
                }
            }
        }

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openEventHorGui(Player p, boolean returnVoid){
        ChestGui gui = new ChestGui(3, "Void + Lava Enchants");
        StaticPane pane = new StaticPane(9,3);

        ItemStack darkFlame = new ItemStack(Material.SOUL_CAMPFIRE);
        ItemMeta darkFlameMeta = darkFlame.getItemMeta();
        darkFlameMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Dark Flame");
        darkFlameMeta.setLore(List.of(
                ChatColor.AQUA + "Landing a crit has a " + CustomEnchantsMain.Enchant.DarkFlame.instance.getChanceFromTotalLevel(1) + "% chance to",
                ChatColor.AQUA + "*stun* by applying Weakness 11, Slowness 11,",
                ChatColor.AQUA + "and blindness while resetting the targets velocity",
                ChatColor.AQUA + "for (1 * (Void Aspect Lvl + Magma Lvl) / 8) seconds"

        ));
        darkFlame.setItemMeta(darkFlameMeta);

        ItemStack armorEnch = new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
        ItemMeta armorEnchMeta = armorEnch.getItemMeta();
        armorEnchMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Event Horizon");
        armorEnchMeta.setLore(List.of(
                ChatColor.AQUA + "Wear a full set of Event Horizon for Dark Flame to work"
        ));
        armorEnch.setItemMeta(armorEnchMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Combine Void + Lava:");
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        GuiItem mainEnchGui = new GuiItem(darkFlame.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem armorGui = new GuiItem(armorEnch.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem elementGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            if(returnVoid){
                openVoidGui(p);
            } else {
                openFireGui(p);
            }
        });

        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 3; j++){
                if(i == 3 && j == 1){
                    pane.addItem(mainEnchGui, i, j);
                } else if(i == 5 && j == 1){
                    pane.addItem(armorGui, i, j);
                } else if(i == 0 && j == 2){
                    pane.addItem(cancelGui, i, j);
                } else if(i == 4 && j == 0){
                    pane.addItem(elementGui, i, j);
                } else {
                    pane.addItem(backroundGUI, i, j);
                }
            }
        }

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openStormGui(Player p, boolean returnWater){
        ChestGui gui = new ChestGui(3, "Water + Lightning Enchants");
        StaticPane pane = new StaticPane(9,3);

        ItemStack shock = new ItemStack(Material.END_ROD);
        ItemMeta shockMeta = shock.getItemMeta();
        shockMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Shocking");
        shockMeta.setLore(List.of(
                ChatColor.AQUA + "Landing a lighting strike with Aspect of the Gods",
                ChatColor.AQUA + "Does the damage again to up to 10 near by entities",
                ChatColor.AQUA + "around the target, damage is spread out equally to all",
                ChatColor.AQUA + "targets. eg: 10 targets takes 10% of initial strike",
                ChatColor.AQUA + "damage each.",
                ChatColor.AQUA + "The Max Health damage cap is doubled before calculating."

        ));
        shock.setItemMeta(shockMeta);

        ItemStack armorEnch = new ItemStack(Material.PRISMARINE_BRICKS);
        ItemMeta armorEnchMeta = armorEnch.getItemMeta();
        armorEnchMeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Storm Born");
        armorEnchMeta.setLore(List.of(
                ChatColor.AQUA + "Wear a full set of Storm Born for Shocking to work"
        ));
        armorEnch.setItemMeta(armorEnchMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Combine Lighting + Water:");
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        GuiItem mainEnchGui = new GuiItem(shock.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem armorGui = new GuiItem(armorEnch.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem elementGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            if(returnWater){
                openWaterGui(p);
            } else {
                openZeusGui(p);
            }
        });

        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 3; j++){
                if(i == 3 && j == 1){
                    pane.addItem(mainEnchGui, i, j);
                } else if(i == 5 && j == 1){
                    pane.addItem(armorGui, i, j);
                } else if(i == 0 && j == 2){
                    pane.addItem(cancelGui, i, j);
                } else if(i == 4 && j == 0){
                    pane.addItem(elementGui, i, j);
                } else {
                    pane.addItem(backroundGUI, i, j);
                }
            }
        }

        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void openDivineGui(Player p, boolean returnVoid){
        ChestGui gui = new ChestGui(3, "Void + Lightning Enchants");
        StaticPane pane = new StaticPane(9,3);

        ItemStack shock = new ItemStack(Material.PURPLE_CANDLE);
        ItemMeta shockMeta = shock.getItemMeta();
        shockMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Black Light");
        shockMeta.setLore(List.of(
                ChatColor.AQUA + "Landing 3 full charged hits within 4 seconds activates",
                ChatColor.AQUA + "either Divine Darkness or Divine Light.",
                "",
                ChatColor.AQUA + "Divine Darkness: If both you AND the target have at least",
                ChatColor.AQUA + "33% max health remaining, deal 15% of the targets current",
                ChatColor.AQUA + "health as bonus void damage!",
                "",
                ChatColor.AQUA + "Divine Light: Otherwise gain absorption 3 for 3 seconds",
                ChatColor.AQUA + "(6 Gold Hearts). Both Abilities share the same cooldown",
                ChatColor.AQUA + "of 5 seconds, which can be reduced by Cooldown Reduction",
                ChatColor.AQUA + "Mega Perk."

        ));
        shock.setItemMeta(shockMeta);

        ItemStack armorEnch = new ItemStack(Material.CRYING_OBSIDIAN);
        ItemMeta armorEnchMeta = armorEnch.getItemMeta();
        armorEnchMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Divine");
        armorEnchMeta.setLore(List.of(
                ChatColor.AQUA + "Wear a full set of Divine for Black Light to work"
        ));
        armorEnch.setItemMeta(armorEnchMeta);

        ItemStack elementInfo = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta elementInfoItemMeta = elementInfo.getItemMeta();
        elementInfoItemMeta.setDisplayName(ChatColor.DARK_GREEN + "Combine Lighting + Void:");
        elementInfo.setItemMeta(elementInfoItemMeta);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Go Back");
        cancel.setItemMeta(cancelMeta);

        GuiItem mainEnchGui = new GuiItem(shock.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem armorGui = new GuiItem(armorEnch.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem elementGui = new GuiItem(elementInfo.clone(), e -> {
            e.setCancelled(true);
        });

        GuiItem cancelGui = new GuiItem(cancel.clone(), e -> {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            if(returnVoid){
                openVoidGui(p);
            } else {
                openZeusGui(p);
            }
        });

        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 3; j++){
                if(i == 3 && j == 1){
                    pane.addItem(mainEnchGui, i, j);
                } else if(i == 5 && j == 1){
                    pane.addItem(armorGui, i, j);
                } else if(i == 0 && j == 2){
                    pane.addItem(cancelGui, i, j);
                } else if(i == 4 && j == 0){
                    pane.addItem(elementGui, i, j);
                } else {
                    pane.addItem(backroundGUI, i, j);
                }
            }
        }

        gui.addPane(pane);
        gui.copy().show(p);
    }
}