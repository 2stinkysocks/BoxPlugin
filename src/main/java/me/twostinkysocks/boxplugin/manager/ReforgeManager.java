package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.ItemModification.ModifyAtribute;
import me.twostinkysocks.boxplugin.ItemModification.RegisteredItem;
import me.twostinkysocks.boxplugin.util.Util;
import net.minecraft.world.item.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static su.nexmedia.engine.utils.ItemUtil.isArmor;

public class ReforgeManager {

    public final NamespacedKey isReforgedKey = new NamespacedKey(BoxPlugin.instance, "isReforged"); // boolean type
    public final NamespacedKey reforgeStatusKey = new NamespacedKey(BoxPlugin.instance, "numReforges"); // integer type
    public final NamespacedKey freeReforgeStatusKey = new NamespacedKey(BoxPlugin.instance, "numFreeReforges"); // integer type
    public final NamespacedKey reforgeListKey = new NamespacedKey(BoxPlugin.instance, "currentReforgesStored"); //list type
    private List<String> reforgeList = new ArrayList<>();

    public final int REFORGECOST = 5;

    public int getREFORGECOST(){
        return REFORGECOST;
    }

    public int getNumReforges(@NotNull ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        int numRefoges = 0;
        if(itemMeta == null || !itemMeta.getPersistentDataContainer().has(isReforgedKey)){
            return numRefoges;
        }
        if(itemMeta.getPersistentDataContainer().has(reforgeStatusKey)){
            numRefoges = itemMeta.getPersistentDataContainer().get(reforgeStatusKey, PersistentDataType.INTEGER);
        }
        return numRefoges;
    }
    public boolean hasReforges(@NotNull ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta.getPersistentDataContainer().has(isReforgedKey)){
            if(itemMeta.getPersistentDataContainer().get(isReforgedKey, PersistentDataType.BOOLEAN)){
                return true;
            }
        }
        return false;
    }
    public ItemStack setNumReforges(@NotNull ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        int numReforges = 1;

        if(itemMeta == null){
            return item;
        }

        if(hasReforges(item)){
            numReforges += getNumReforges(item);
            itemMeta.getPersistentDataContainer().set(reforgeStatusKey, PersistentDataType.INTEGER, numReforges);
        } else {
            itemMeta.getPersistentDataContainer().set(reforgeStatusKey, PersistentDataType.INTEGER, numReforges);
            BoxPlugin.instance.getRegisteredItem().SetReforgedStatus(itemMeta, true);
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    public boolean hasFreeReforges(@NotNull ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta.getPersistentDataContainer().has(freeReforgeStatusKey)){
            if(itemMeta.getPersistentDataContainer().get(freeReforgeStatusKey, PersistentDataType.INTEGER) >= 1){
                return true;
            }
        }
        return false;
    }
    public int getNumFreeReforges(@NotNull ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        int freeReforges = 0;
        if (itemMeta == null || !itemMeta.getPersistentDataContainer().has(freeReforgeStatusKey)) {
            return freeReforges;
        }
        if (itemMeta.getPersistentDataContainer().has(freeReforgeStatusKey)) {
            freeReforges = itemMeta.getPersistentDataContainer().get(freeReforgeStatusKey, PersistentDataType.INTEGER);
        }
        return freeReforges;
    }


    public ItemStack setNumFreeReforges(@NotNull ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        int numFreeReforges = 1;

        if(itemMeta == null){
            return item;
        }

        if(hasFreeReforges(item)){
            numFreeReforges += getNumFreeReforges(item);
            itemMeta.getPersistentDataContainer().set(freeReforgeStatusKey, PersistentDataType.INTEGER, numFreeReforges);
        } else {
            itemMeta.getPersistentDataContainer().set(freeReforgeStatusKey, PersistentDataType.INTEGER, numFreeReforges);
        }
        item.setItemMeta(itemMeta);
        return item;
    }


    public ItemStack setReforgeList(ItemStack item, String reforge){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return item;
        }

        if(itemMeta.getPersistentDataContainer().has(reforgeListKey)){
            List<String> stored = itemMeta.getPersistentDataContainer().get(reforgeListKey, PersistentDataType.LIST.strings());

            reforgeList = stored != null ? new ArrayList<>(stored) : new ArrayList<>();
        } else {
            reforgeList = new ArrayList<>();
        }

        reforgeList.add(reforge);

        itemMeta.getPersistentDataContainer().set(reforgeListKey, PersistentDataType.LIST.strings(), reforgeList);

        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack removeReforgeList(@NotNull ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return item;
        }

        if(itemMeta.getPersistentDataContainer().has(reforgeListKey)){
            itemMeta.getPersistentDataContainer().remove(reforgeListKey);
        } else {
            return item;
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    public List getReforgeList(@NotNull ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        reforgeList.clear();
        if(itemMeta == null){
            return reforgeList;
        }

        if(itemMeta.getPersistentDataContainer().has(reforgeListKey)){
            reforgeList = itemMeta.getPersistentDataContainer().get(reforgeListKey, PersistentDataType.LIST.strings());
        }
        return reforgeList;
    }

    public void openGui(Player p) {
        ChestGui gui = new ChestGui(6, "Reforges");
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

        ItemStack magmaBlock = new ItemStack(Material.MAGMA_BLOCK);
        ItemMeta magmaBlockMeta = magmaBlock.getItemMeta();
        magmaBlockMeta.setDisplayName(ChatColor.RESET + "");
        magmaBlock.setItemMeta(magmaBlockMeta);
        GuiItem magmaBlockGui = new GuiItem(magmaBlock, e -> {
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
        }), 2, 1);

        ItemStack reforgeAnvil = new ItemStack(Material.ANVIL);
        ItemMeta reforgeAnvilMeta = reforgeAnvil.getItemMeta();
        reforgeAnvilMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Reforge Item");
        reforgeAnvilMeta.setLore(List.of(ChatColor.GREEN + "Costs " + REFORGECOST + " rubies"));
        reforgeAnvil.setItemMeta(reforgeAnvilMeta);
        pane.addItem(new GuiItem(reforgeAnvil, e -> {
            e.setCancelled(true);
            ItemStack item = e.getView().getTopInventory().getItem(13);
            if(item == null){
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "No item in slot!");
                return;
            }
            if(BoxPlugin.instance.getMarketManager().getRubies(p) < REFORGECOST){
                p.sendMessage(ChatColor.RED + "You dont have enough rubies!");
                return;
            }
            ItemMeta itemMeta = item.getItemMeta();
            if(!BoxPlugin.instance.getRegisteredItem().IsRegistered(itemMeta)){
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "This item is not reforgeable, read /reforges for more info!");
                return;
            }
            if(getNumReforges(item) >= 10){
                p.sendMessage(ChatColor.RED + "This item has too many reforges, read /reforges for more info!");
                return;
            }

            if(BoxPlugin.instance.getRegisteredItem().IsRegistered(itemMeta) && isntBlacklistedItem(item)){
                Random random = new Random();
                int doubleRolls = random.nextInt(10);
                if(doubleRolls == 1){
                    boolean reforged2 = DecideItemTypeToReforge(item, p);
                    if(!reforged2){//cancel if not eligible even if you got the 2nd chance
                        p.sendMessage(ChatColor.RED + "Unable to reforge item!");
                        return;
                    }
                    p.sendMessage(ChatColor.GOLD + "You received a 2nd free reforge!");
                    setNumReforges(item);
                    setNumFreeReforges(item);
                }
                boolean reforged = DecideItemTypeToReforge(item, p);
                if(!reforged){//cancel if not eligible
                    p.sendMessage(ChatColor.RED + "Unable to reforge item!");
                    return;
                }

                setNumReforges(item);
                p.sendMessage(ChatColor.GREEN + "Reforging complete!");
                p.sendMessage(ChatColor.GREEN + "Use /listreforges to see all reforges!");

                BoxPlugin.instance.getMarketManager().setRubies(p, BoxPlugin.instance.getMarketManager().getRubies(p)-(REFORGECOST));
                BoxPlugin.instance.getScoreboardManager().queueUpdate(p);

                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 0.76f);
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.3f, 0.78f);
                p.playSound(p.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1f, 0.5f);
            }

        }), 4, 2);

        ItemStack outline = new ItemStack(Material.IRON_BARS);
        ItemMeta outlineMeta = outline.getItemMeta();
        outlineMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Reforges:");
        outlineMeta.setLore(List.of(ChatColor.GOLD + "Items can have a max of 10 reforges", ChatColor.GOLD + "Reforges cost 5 rubies each", ChatColor.GOLD + "Use /reforges for more info", ChatColor.GOLD + "" + ChatColor.BOLD + "Items with reforges cannot be upgraded!", ChatColor.DARK_AQUA + "Use /listreforges to see your items reforges!"));
        outline.setItemMeta(outlineMeta);
        GuiItem outlineGui = new GuiItem(outline, e -> {
            e.setCancelled(true);
        });

        ItemStack strip = new ItemStack(Material.GRINDSTONE);
        ItemMeta stripMeta = strip.getItemMeta();
        stripMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Remove all reforges");
        stripMeta.setLore(List.of(ChatColor.RED + "Remove reforges and get refunded either", ChatColor.RED + "80% or 100% of the rubies."));
        strip.setItemMeta(stripMeta);
        pane.addItem(new GuiItem(strip, e -> {
            e.setCancelled(true);
            ItemStack item = e.getView().getTopInventory().getItem(13);
            if(item == null) {
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
                p.sendMessage(ChatColor.RED + "No item in slot!");
                return;
            }
            try {
                stripReforges(item, p);
            } catch (SQLException | IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            BoxPlugin.instance.getScoreboardManager().queueUpdate(p);

        }), 6, 1);

        // 1) Iron bars frame (the “window” area in the top)
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 6; y++) {
                //  set iron bars
                if ((x > 2 && x < 6) && (y == 0)){
                    pane.addItem(outlineGui, x, y);
                }
                if ((x == 3 || x == 5) && (y >= 1 && y <= 3)){
                    pane.addItem(outlineGui, x, y);
                }
                if ((x == 2 || x == 6) && (y >= 2 && y <= 4)){
                    pane.addItem(outlineGui, x, y);
                }
                if ((x == 1 || x == 7) && (y >= 3)){
                    pane.addItem(outlineGui, x, y);
                }
            }
        }

        // 2) Background everywhere else, except magma and the iron-bars frame
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 6; y++) {

                // gray stained glass on sides
                if ((x == 0) || (x == 8)){
                    pane.addItem(backgroundGui, x, y);
                }
                if (((x == 1) || (x == 7)) && (y <= 2)){
                    pane.addItem(backgroundGui, x, y);
                }
                if (((x == 2) || (x == 6)) && (y == 0)){
                    pane.addItem(backgroundGui, x, y);
                }
            }
        }

        // 3) Magma triangle (bottom)
        int startY = 3;         // first row of magma
        int height = 3;         // 3 rows
        int centerX = 4;        // middle column

        for (int row = 0; row < height; row++) {
            int y = startY + row;
            int width = 1 + 2 * row;          // 1, 3, 5 blocks
            int startX = centerX - row;       // 4, 3, 2

            for (int dx = 0; dx < width; dx++) {
                int x = startX + dx;
                pane.addItem(magmaBlockGui, x, y);
            }
        }


        gui.addPane(pane);
        gui.show(p);
    }

    public ItemStack stripReforges(ItemStack item, Player p) throws SQLException, IOException, ClassNotFoundException {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null || BoxPlugin.instance.getGhostTokenManager().isGhostItem(item)) {
            p.sendMessage(ChatColor.RED + "This item is not valid!");
            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            return item;
        }

        if (hasReforges(item)) {
            int numReforges = getNumReforges(item) - getNumFreeReforges(item); //if you had free reforges, the bonus no longer counts to your refund
            Random luck = new Random();
            int randLuck = luck.nextInt(10);
            double rubyMult = 1;

            if (randLuck <= 2) {//30% chance to lose 20% of rubies
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Xanatos:" + ChatColor.GOLD + " OOPS! I messed up a bit...");
                rubyMult = 0.8;
            }

                BoxPlugin.instance.getMarketManager().addRubies(p, (int) (numReforges * REFORGECOST * rubyMult));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);

                if (rubyMult == 1) {
                    p.sendMessage(ChatColor.GREEN + "Stripped " + numReforges + " reforges from your item and gained all rubies back (" + (int) (numReforges * REFORGECOST * rubyMult) + " rubies)!");
                } else {
                    p.sendMessage(ChatColor.GREEN + "Stripped " + numReforges + " reforges from your item and returned " + (rubyMult*100) + "% of rubies (" + (int) (numReforges * REFORGECOST * rubyMult) + " rubies)!");
                }
                itemMeta.getPersistentDataContainer().remove(reforgeStatusKey);
                if(hasFreeReforges(item)){
                    itemMeta.getPersistentDataContainer().remove(freeReforgeStatusKey);
                }
                BoxPlugin.instance.getRegisteredItem().SetReforgedStatus(itemMeta, false);
                item.setItemMeta(itemMeta);

                removeReforgeList(item);
                BoxPlugin.instance.getRegisteredItem().SetToRegisteredItem(item);//sets toe registered defualt
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2f);
            } else {
                p.sendMessage(ChatColor.RED + "This item is not reforged!");
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 3.0F, 1.0F);
            }
        return item;
    }

    public boolean isntBlacklistedItem(ItemStack item){
        if(item.getType() == Material.SHIELD){
            return false;
        }

        return true;
    }

    public boolean DecideItemTypeToReforge(ItemStack item, Player p){
        if(item.getAmount() > 1){
            p.sendMessage(ChatColor.RED + "You cannot put reforges on stacked items!");
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        Material itemType = item.getType();

        if(itemMeta instanceof SkullMeta){
            p.sendMessage(ChatColor.GREEN + "Reforging your helmet!");
            HelmetReforges(item, p);

        } else if (itemMeta instanceof ArmorMeta) {

            if(itemType.name().endsWith("_HELMET")){
                p.sendMessage(ChatColor.GREEN + "Reforging your helmet!");
                HelmetReforges(item, p);

            } else if (itemType.name().endsWith("_CHESTPLATE")) {
                p.sendMessage(ChatColor.GREEN + "Reforging your chestplate!");
                ChestplateReforge(item, p);

            } else if (itemType.name().endsWith("_LEGGINGS")) {
                p.sendMessage(ChatColor.GREEN + "Reforging your leggings!");
                LeggingsReforge(item, p);

            } else if (itemType.name().endsWith("_BOOTS")) {
                p.sendMessage(ChatColor.GREEN + "Reforging your boots!");
                BootsReforge(item, p);

            }
        } else if (itemType.name().endsWith("_PICKAXE")) {
            p.sendMessage(ChatColor.GREEN + "Reforging your pickaxe!");
            PickaxeReforge(item, p);

        } else if (itemType.name().endsWith("_AXE")) {
            p.sendMessage(ChatColor.GREEN + "Reforging your axe!");
            AxeReforge(item, p);

        } else if (item.getType() == Material.TRIDENT) {
            p.sendMessage(ChatColor.GREEN + "Reforging your trident!");
            TridentReforge(item, p);

        } else if (item.getType() == Material.BOW) {
            p.sendMessage(ChatColor.GREEN + "Reforging your bow!");
            BowReforge(item, p);

        } else if (item.getType() == Material.CROSSBOW) {
            p.sendMessage(ChatColor.GREEN + "Reforging your crossbow!");
            CrossBowReforge(item, p);

        } else {
            //must be a sword if gotten this far
            p.sendMessage(ChatColor.GREEN + "Reforging your weapon!");
            SwordReforge(item, p);
        }

        return true;
    }

    public ItemStack HelmetReforges(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 11){ //choose protection
            p.sendMessage(ChatColor.GREEN + "Common reforge: protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 protection");
            }

        }

        else if (reforgeChoiceChance1 <= 22) {//chose blast prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: blast protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 blast protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 blast protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 blast protection");
            }
        }

        else if (reforgeChoiceChance1 <= 32) {//chose proj prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: projectile protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 projectile protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 projectile protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 projectile protection");
            }
        }

        else if (reforgeChoiceChance1 <= 42) {//choose fire prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: fire protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 1 fire protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 2 fire protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 3 fire protection");
            }
        }

        else if (reforgeChoiceChance1 <= 53) {//choose resperation
            p.sendMessage(ChatColor.GREEN + "Common reforge: respiration level increased!");
            if(reforgeChoiceChance2 <= 70){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.RESPIRATION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 1 respiration");
            } else if (reforgeChoiceChance2 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.RESPIRATION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 2 respiration");
            }
        }

        else if (reforgeChoiceChance1 <= 64) {//choose max hp
            p.sendMessage(ChatColor.GREEN + "Common reforge: max health increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatHealth(item, 1, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 max health");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatHealth(item, 2, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 max health");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatHealth(item, 3, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 max health");
            }
        }

        else if (reforgeChoiceChance1 <= 74) { //choose armor
            p.sendMessage(ChatColor.GREEN + "Common reforge: armor increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmor(item, 1, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 armor");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmor(item, 2, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 armor");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmor(item, 3, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 armor");
            }
        }

        else if (reforgeChoiceChance1 <= 85) { //choose move speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: move speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.05, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5% move speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.1, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 10% move speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.15, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 8);
                item = setReforgeList(item, "+ 15% move speed");
            }
        }

        else if (reforgeChoiceChance1 <= 90) { //choose waterMine eff
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: submerged mining speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentWaterMineSpeed(item, 0.8, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 80% water mining speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentWaterMineSpeed(item, 1.2, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 120% water mining speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentWaterMineSpeed(item, 2, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 200% water mining speed");
            }
        }

        else if (reforgeChoiceChance1 <= 95) { //choose armor toughness
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: armor toughness increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.3, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ .3 armor toughness");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.6, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ .6 armor toughness");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.9, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ .9 armor toughness");
            }
        }

        else if (reforgeChoiceChance1 <= 100) { //choose kb resistance
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: knock back resistance increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.03, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 3% knockback resistance");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.06, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 6% knockback resistance");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.1, EquipmentSlotGroup.HEAD);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 10% knockback resistance");
            }
        }

        return item;
    }

    public ItemStack ChestplateReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 11){ //choose protection
            p.sendMessage(ChatColor.GREEN + "Common reforge: protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 protection");
            }

        }

        else if (reforgeChoiceChance1 <= 22) {//chose blast prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: blast protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 blast protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 blast protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 blast protection");
            }
        }

        else if (reforgeChoiceChance1 <= 33) {//chose proj prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: projectile protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 projectile protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 projectile protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 projectile protection");
            }
        }

        else if (reforgeChoiceChance1 <= 44) {//choose fire prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: fire protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 1 fire protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 2 fire protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 3 fire protection");
            }
        }

        else if (reforgeChoiceChance1 <= 55) {//choose max hp
            p.sendMessage(ChatColor.GREEN + "Common reforge: max health increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatHealth(item, 1, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 max health");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatHealth(item, 2, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 max health");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatHealth(item, 3, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 max health");
            }
        }

        else if (reforgeChoiceChance1 <= 66) {//choose attack damage
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack damage increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.05, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 5% attack damage");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.1, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "+ 10% attack damage");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.15, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 15);
                item = setReforgeList(item, "+ 15% attack damage");
            }
        }

        else if (reforgeChoiceChance1 <= 77) { //choose armor
            p.sendMessage(ChatColor.GREEN + "Common reforge: armor increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmor(item, 1, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 armor");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmor(item, 2, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 armor");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmor(item, 3, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 armor");
            }
        }

        else if (reforgeChoiceChance1 <= 88) { //choose move speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentAttackSpeed(item, 0.05, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5% attack speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentAttackSpeed(item, 0.1, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ 10% attack speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentAttackSpeed(item, 0.15, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "+ 15% attack speed");
            }
        }

        else if (reforgeChoiceChance1 <= 93) { //choose armor toughness
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: armor toughness increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.5, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ .5 armor toughness");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmorToughness(item, 1, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 1 armor toughness");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmorToughness(item, 1.5, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 1.5 armor toughness");
            }
        }

        else if (reforgeChoiceChance1 <= 98) { //choose kb resistance
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: knock back resistance increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.05, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5% knockback resistance");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.1, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 10% knockback resistance");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.15, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 15% knockback resistance");
            }
        }

        else if (reforgeChoiceChance1 <= 100) { //choose goliath (legendary)
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Legendary reforge: Goliath: increase size and attack reach!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.2, EquipmentSlotGroup.CHEST);
                ModifyAtribute.ModifyPercentScale(item, 0.2, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ .2 reach and 20% size");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.4, EquipmentSlotGroup.CHEST);
                ModifyAtribute.ModifyPercentScale(item, 0.4, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 14);
                item = setReforgeList(item, "+ .4 reach and 40% size");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.6, EquipmentSlotGroup.CHEST);
                ModifyAtribute.ModifyPercentScale(item, 0.6, EquipmentSlotGroup.CHEST);
                item = ModifyAtribute.ModifyGearScore(item, 21);
                item = setReforgeList(item, "+ .6 reach and 60% size");
            }
        }

        return item;
    }

    public ItemStack LeggingsReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 12){ //choose protection
            p.sendMessage(ChatColor.GREEN + "Common reforge: protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 protection");
            }

        }

        else if (reforgeChoiceChance1 <= 24) {//chose blast prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: blast protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 blast protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 blast protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 blast protection");
            }
        }

        else if (reforgeChoiceChance1 <= 36) {//chose proj prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: projectile protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 projectile protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 projectile protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 projectile protection");
            }
        }

        else if (reforgeChoiceChance1 <= 48) {//choose fire prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: fire protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 1 fire protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 2 fire protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 3 fire protection");
            }
        }

        else if (reforgeChoiceChance1 <= 60) {//choose max hp
            p.sendMessage(ChatColor.GREEN + "Common reforge: max health increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatHealth(item, 1, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 max health");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatHealth(item, 2, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 max health");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatHealth(item, 3, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 max health");
            }
        }

        else if (reforgeChoiceChance1 <= 72) {//choose attack damage
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack damage increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackDmg(item, 1, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 1 attack damage");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackDmg(item, 2, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 8);
                item = setReforgeList(item, "+ 2 attack damage");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackDmg(item, 4, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 12);
                item = setReforgeList(item, "+ 4 attack damage");
            }
        }

        else if (reforgeChoiceChance1 <= 84) { //choose armor
            p.sendMessage(ChatColor.GREEN + "Common reforge: armor increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmor(item, 1, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 armor");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmor(item, 2, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 armor");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmor(item, 3, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 armor");
            }
        }

        else if (reforgeChoiceChance1 <= 88) { //choose armor toughness
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: armor toughness increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.5, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ .5 armor toughness");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmorToughness(item, 1, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 1 armor toughness");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmorToughness(item, 1.5, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 1.5 armor toughness");
            }
        }

        else if (reforgeChoiceChance1 <= 92) { //choose kb resistance
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: knock back resistance increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.05, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5% knockback resistance");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.1, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 10% knockback resistance");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatKBResistance(item, 0.15, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 15% knockback resistance");
            }
        }

        else if (reforgeChoiceChance1 <= 96) { //choose swiftsneak
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: swift sneak increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.SWIFT_SNEAK  , 1);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 1 swift sneak");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.SWIFT_SNEAK, 2);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 2 swift sneak");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.SWIFT_SNEAK, 3);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 3 swift sneak");
            }
        }

        else if (reforgeChoiceChance1 <= 100) { //choose low grav
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: gravity lowered!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentGravity(item, -0.1, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "- 10% gravity");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatKBResistance(item, -0.2, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "- 20% gravity");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatKBResistance(item, -0.3, EquipmentSlotGroup.LEGS);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "- 30% gravity");
            }
        }

        return item;
    }

    public ItemStack BootsReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 9){ //choose protection
            p.sendMessage(ChatColor.GREEN + "Common reforge: protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 protection");
            }

        }

        else if (reforgeChoiceChance1 <= 18) {//chose blast prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: blast protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 blast protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 blast protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.BLAST_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 blast protection");
            }
        }

        else if (reforgeChoiceChance1 <= 27) {//chose proj prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: projectile protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 projectile protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 projectile protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PROJECTILE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 projectile protection");
            }
        }

        else if (reforgeChoiceChance1 <= 36) {//choose fire prot
            p.sendMessage(ChatColor.GREEN + "Common reforge: fire protection increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 1);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 1 fire protection");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 2);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 2 fire protection");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_PROTECTION, 3);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 3 fire protection");
            }
        }

        else if (reforgeChoiceChance1 <= 45) {//choose depth strider
            p.sendMessage(ChatColor.GREEN + "Common reforge: depth strider increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.DEPTH_STRIDER, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 depth stider");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.DEPTH_STRIDER, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 depth stider");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.DEPTH_STRIDER, 3);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 3 depth stider");
            }
        }

        else if (reforgeChoiceChance1 <= 54) {//choose feather falling
            p.sendMessage(ChatColor.GREEN + "Common reforge: feather falling increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FEATHER_FALLING, 1);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 1 feather falling");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FEATHER_FALLING, 2);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 2 feather falling");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FEATHER_FALLING, 3);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 3 feather falling");
            }
        }

        else if (reforgeChoiceChance1 <= 63) {//choose max hp
            p.sendMessage(ChatColor.GREEN + "Common reforge: max health increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatHealth(item, 1, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 max health");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatHealth(item, 2, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 max health");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatHealth(item, 3, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 max health");
            }
        }

        else if (reforgeChoiceChance1 <= 72) { //choose armor
            p.sendMessage(ChatColor.GREEN + "Common reforge: armor increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmor(item, 1, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 armor");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmor(item, 2, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 armor");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmor(item, 3, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 armor");
            }
        }

        else if (reforgeChoiceChance1 <= 81) { //choose move speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: move speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.05, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5% move speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.1, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 10% move speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.15, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 8);
                item = setReforgeList(item, "+ 15% move speed");
            }
        }

        else if (reforgeChoiceChance1 <= 90) { //choose safe fall
            p.sendMessage(ChatColor.GREEN + "Common reforge: max fall height increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatSafeFallDistance(item, 5, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5 safe fall distance");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatSafeFallDistance(item, 10, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 10 safe fall distance");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatSafeFallDistance(item, 15, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 8);
                item = setReforgeList(item, "+ 15 safe fall distance");
            }
        }

        else if (reforgeChoiceChance1 <= 95) { //choose armor toughness
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: armor toughness increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.3, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ .3 armor toughness");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.6, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ .6 armor toughness");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatArmorToughness(item, 0.9, EquipmentSlotGroup.FEET);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ .9 armor toughness");
            }
        }

        else if (reforgeChoiceChance1 <= 100) {//choose frost walker
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: frost walker increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FROST_WALKER, 1);
                item = ModifyAtribute.ModifyGearScore(item, 1);
                item = setReforgeList(item, "+ 1 frost walker");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FROST_WALKER, 2);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 2 frost walker");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FROST_WALKER, 3);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 3 frost walker");
            }
        }

        return item;
    }

    public ItemStack PickaxeReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 30){ //choose fortune
            if(item.containsEnchantment(Enchantment.SILK_TOUCH)){// dont put fortune on silk pick, reroll
                item = PickaxeReforge(item, p);
                return item;
            }
            p.sendMessage(ChatColor.GREEN + "Common reforge: fortune increased!");
            if(reforgeChoiceChance2 <= 70){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FORTUNE, 1);
                item = setReforgeList(item, "+ 1 fortune");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FORTUNE, 2);
                item = setReforgeList(item, "+ 2 fortune");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FORTUNE, 3);
                item = setReforgeList(item, "+ 3 fortune");
            }

        }

        else if (reforgeChoiceChance1 <= 60) {//chose efficiency
            p.sendMessage(ChatColor.GREEN + "Common reforge: efficiency increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.EFFICIENCY, 1);
                item = setReforgeList(item, "+ 1 efficiency");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.EFFICIENCY, 2);
                item = setReforgeList(item, "+ 2 efficiency");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.EFFICIENCY, 3);
                item = setReforgeList(item, "+ 3 efficiency");
            }
        }

        else if (reforgeChoiceChance1 <= 90) { //choose attack damage
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack damage increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.07, EquipmentSlotGroup.OFFHAND);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 7% off hand attack damage");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.09, EquipmentSlotGroup.OFFHAND);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 9% off hand attack damage");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.11, EquipmentSlotGroup.OFFHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 11% off hand attack damage");
            }
        }

        else if (reforgeChoiceChance1 <= 95) { //choose waterMine eff
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: submerged mining speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentWaterMineSpeed(item, 0.4, EquipmentSlotGroup.MAINHAND);
                item = setReforgeList(item, "+ 40% water mining speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentWaterMineSpeed(item, 0.8, EquipmentSlotGroup.MAINHAND);
                item = setReforgeList(item, "+ 80% water mining speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentWaterMineSpeed(item, 1.2, EquipmentSlotGroup.MAINHAND);
                item = setReforgeList(item, "+ 120% water mining speed");
            }
        }

        else if (reforgeChoiceChance1 <= 100) { //choose block reach
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: block reach increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatBlockReach(item, 0.5, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ .5 block reach");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatBlockReach(item, 1, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 1 block reach");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatBlockReach(item, 1.5, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 1.5 block reach");
            }
        }

        return item;
    }

    public ItemStack AxeReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 22){ //choose looting
            p.sendMessage(ChatColor.GREEN + "Common reforge: looting increased!");
            if(reforgeChoiceChance2 <= 50){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOOTING, 1);
                item = setReforgeList(item, "+ 1 looting");
            } else if (reforgeChoiceChance2 <= 80) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOOTING, 2);
                item = setReforgeList(item, "+ 2 looting");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOOTING, 3);
                item = setReforgeList(item, "+ 3 looting");
            }

        }

        else if (reforgeChoiceChance1 <= 44) {//chose fire aspect
            p.sendMessage(ChatColor.GREEN + "Common reforge: fire aspect increased!");
            if(reforgeChoiceChance2 <= 70){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_ASPECT, 1);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 1 fire aspect");
            } else if (reforgeChoiceChance2 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_ASPECT, 2);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 2 fire aspect");
            }
        }

        else if (reforgeChoiceChance1 <= 66) {//choose attack damage
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack damage increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackDmg(item, 2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 2 attack damage");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackDmg(item, 3, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 attack damage");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackDmg(item, 4, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ 4 attack damage");
            }
        }

        else if (reforgeChoiceChance1 <= 88) {//choose attack speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.1, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ .1 attack speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ .2 attack speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.3, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ .3 attack speed");
            }
        }

        else if (reforgeChoiceChance1 <= 98) {//chose knockback
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: knockback increased!");
            p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
            ModifyAtribute.modifyEnchant(item, Enchantment.KNOCKBACK, 1);
            item = ModifyAtribute.ModifyGearScore(item, 5);
            item = setReforgeList(item, "+ 1 knockback");
        }

        else if (reforgeChoiceChance1 <= 100) { //choose reach (legendary)
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Legendary reforge: attack reach increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ .2 reach");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.4, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "+ .4 reach");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.6, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 15);
                item = setReforgeList(item, "+ .6 reach");
            }
        }

        return item;
    }

    public ItemStack TridentReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 20){ //choose impailing
            p.sendMessage(ChatColor.GREEN + "Common reforge: impailing increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.IMPALING, 1);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 1 impailing");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.IMPALING, 2);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 2 impailing");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.IMPALING, 3);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 3 impailing");
            }

        }

        else if (reforgeChoiceChance1 <= 40) {//choose loyalty
            p.sendMessage(ChatColor.GREEN + "Common reforge: fire aspect increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOYALTY, 1);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 1 loyalty");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOYALTY, 2);
                item = ModifyAtribute.ModifyGearScore(item, 8);
                item = setReforgeList(item, "+ 2 loyalty");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOYALTY, 3);
                item = ModifyAtribute.ModifyGearScore(item, 12);
                item = setReforgeList(item, "+ 3 loyalty");
            }
        }

        else if (reforgeChoiceChance1 <= 58) {//chose knockback
            p.sendMessage(ChatColor.GREEN + "Common reforge: knockback increased!");
            p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
            ModifyAtribute.modifyEnchant(item, Enchantment.KNOCKBACK, 1);
            item = ModifyAtribute.ModifyGearScore(item, 5);
            item = setReforgeList(item, "+ 1 knockback");
        }

        else if (reforgeChoiceChance1 <= 78) {//choose attack speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ .2 attack speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.3, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ .3 attack speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.4, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ .4 attack speed");
            }
        }

        else if (reforgeChoiceChance1 <= 98) {//choose attack damage
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack damage increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackDmg(item, 2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 2 attack damage");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackDmg(item, 3, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 attack damage");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackDmg(item, 4, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ 4 attack damage");
            }
        }

        else if (reforgeChoiceChance1 <= 100) { //choose reach (legendary)
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Legendary reforge: attack reach increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.3, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ .3 reach");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.6, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "+ .6 reach");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.9, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 15);
                item = setReforgeList(item, "+ .9 reach");
            }
        }

        return item;
    }

    public ItemStack BowReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 30){ //choose power
            p.sendMessage(ChatColor.GREEN + "Common reforge: power increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.POWER, 1);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 1 power");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.POWER, 2);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 2 power");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.POWER, 3);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "+ 3 power");
            }

        }

        else if (reforgeChoiceChance1 <= 60) {//choose flame
            p.sendMessage(ChatColor.GREEN + "Common reforge: flame increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FLAME, 1);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 1 flame");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FLAME, 2);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ 2 flame");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.FLAME, 3);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 3 flame");
            }
        }

        else if (reforgeChoiceChance1 <= 90) { //choose move speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: move speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.05, EquipmentSlotGroup.HAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5% move speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.08, EquipmentSlotGroup.HAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 8% move speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.1, EquipmentSlotGroup.HAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ 10% move speed");
            }
        }

        else if (reforgeChoiceChance1 <= 100) {//choose punch
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: punch increased!");
            p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
            ModifyAtribute.modifyEnchant(item, Enchantment.PUNCH, 1);
            item = ModifyAtribute.ModifyGearScore(item, 5);
            item = setReforgeList(item, "+ 1 punch");
        }

        return item;
    }

    public ItemStack CrossBowReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 33){ //choose pierce
            p.sendMessage(ChatColor.GREEN + "Common reforge: pierce increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.PIERCING, 1);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 1 pierce");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.PIERCING, 2);
                item = ModifyAtribute.ModifyGearScore(item, 7);
                item = setReforgeList(item, "+ 2 pierce");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.PIERCING, 3);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "+ 3 pierce");
            }

        }

        else if (reforgeChoiceChance1 <= 66) { //choose attack damage
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack damageincreased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.06, EquipmentSlotGroup.OFFHAND);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 6% attack damage");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.8, EquipmentSlotGroup.OFFHAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ 8% attack damage");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentAttackDmg(item, 0.10, EquipmentSlotGroup.OFFHAND);
                item = ModifyAtribute.ModifyGearScore(item, 14);
                item = setReforgeList(item, "+ 10% attack damage");
            }
        }

        else if (reforgeChoiceChance1 <= 100) { //choose move speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: move speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.05, EquipmentSlotGroup.HAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 5% move speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.08, EquipmentSlotGroup.HAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 8% move speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyPercentMoveSpeed(item, 0.10, EquipmentSlotGroup.HAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ 10% move speed");
            }
        }

        return item;
    }

    public ItemStack SwordReforge(ItemStack item, Player p){
        Random random = new Random();

        int reforgeChoiceChance1 = random.nextInt(100) + 1;
        int reforgeChoiceChance2 = random.nextInt(100) + 1;


        if(reforgeChoiceChance1 <= 18){ //choose looting
            p.sendMessage(ChatColor.GREEN + "Common reforge: looting increased!");
            if(reforgeChoiceChance2 <= 50){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOOTING, 1);
                item = setReforgeList(item, "+ 1 looting");
            } else if (reforgeChoiceChance2 <= 80) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOOTING, 2);
                item = setReforgeList(item, "+ 2 looting");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.LOOTING, 3);
                item = setReforgeList(item, "+ 3 looting");
            }
        }

        else if (reforgeChoiceChance1 <= 36) {//chose fire aspect
            p.sendMessage(ChatColor.GREEN + "Common reforge: fire aspect increased!");
            if(reforgeChoiceChance2 <= 70){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_ASPECT, 1);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 1 fire aspect");
            } else if (reforgeChoiceChance2 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.FIRE_ASPECT, 2);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 2 fire aspect");
            }
        }

        else if (reforgeChoiceChance1 <= 54) {//choose attack damage
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack damage increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackDmg(item, 1, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ 1 attack damage");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackDmg(item, 2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 2 attack damage");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackDmg(item, 3, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ 3 attack damage");
            }
        }

        else if (reforgeChoiceChance1 <= 72) {//choose sweeping edge
            if (!item.getType().name().endsWith("_SWORD")) {
                item = SwordReforge(item, p);
                return item;
            }
            p.sendMessage(ChatColor.GREEN + "Common reforge: sweeping edge increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.modifyEnchant(item, Enchantment.SWEEPING_EDGE, 1);
                item = ModifyAtribute.ModifyGearScore(item, 2);
                item = setReforgeList(item, "+ 1 sweeping edge");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.modifyEnchant(item, Enchantment.SWEEPING_EDGE, 2);
                item = ModifyAtribute.ModifyGearScore(item, 4);
                item = setReforgeList(item, "+ 2 sweeping edge");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.modifyEnchant(item, Enchantment.SWEEPING_EDGE, 3);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ 3 sweeping edge");
            }
        }

        else if (reforgeChoiceChance1 <= 90) {//choose attack speed
            p.sendMessage(ChatColor.GREEN + "Common reforge: attack speed increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.1, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 3);
                item = setReforgeList(item, "+ .1 attack speed");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 6);
                item = setReforgeList(item, "+ .2 attack speed");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackSpeed(item, 0.3, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 9);
                item = setReforgeList(item, "+ .3 attack speed");
            }
        }

        else if (reforgeChoiceChance1 <= 98) {//chose knockback
            p.sendMessage(ChatColor.DARK_PURPLE + "Epic reforge: knockback increased!");
            p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
            ModifyAtribute.modifyEnchant(item, Enchantment.KNOCKBACK, 1);
            item = ModifyAtribute.ModifyGearScore(item, 5);
            item = setReforgeList(item, "+ 1 knockback");
        }

        else if (reforgeChoiceChance1 <= 100) { //choose reach (legendary)
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Legendary reforge: attack reach increased!");
            if(reforgeChoiceChance2 <= 60){
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier I");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.2, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 5);
                item = setReforgeList(item, "+ .2 reach");
            } else if (reforgeChoiceChance2 <= 90) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier II");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.4, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 10);
                item = setReforgeList(item, "+ .4 reach");
            } else if (reforgeChoiceChance1 <= 100) {
                p.sendMessage(ChatColor.DARK_AQUA + "Obtained tier III");
                ModifyAtribute.ModifyFlatAttackReach(item, 0.6, EquipmentSlotGroup.MAINHAND);
                item = ModifyAtribute.ModifyGearScore(item, 15);
                item = setReforgeList(item, "+ .6 reach");
            }
        }

        return item;
    }

}
