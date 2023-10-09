package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.reforges.*;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * If anyone reads this in the future, i'm so sorry
 * the design patterns i used sounded like good ideas but they were actually pretty bad
 * so don't use this as an example please
 *
 * This class manages the entire reforge system
 *
 * The data is broken down like this:
 *
 * me.twostinkysocks.boxplugin.reforges.* contains abstract classes that represent a reforge at a certain level ... TODO
 */
public class ReforgeManager {

    // reforged items contain the following:
    // pdc REFORGES: TAG_CONTAINER (pdc.getAdapterContext().newPersistentDataContainer())
    //    (id of reforge): TAG_CONTAINER
    //        STAT: (name of stat, can look up stat by name and get object in code)
    //        AMOUNT: (new stat number double)
    //        ORIGINAL: (original stat number double)

    public enum Reforgable {
        SWORD(AbstractSwordReforge.class),
        BOW(AbstractBowReforge.class),
        AXE(AbstractAxeReforge.class),
        CROSSBOW(AbstractCrossbowReforge.class),
        TRIDENT(AbstractTridentReforge.class),
        PICKAXE(AbstractPickaxeReforge.class),
        HELMET(AbstractHelmetReforge.class),
        CHESTPLATE(AbstractChestplateReforge.class),
        LEGGINGS(AbstractLeggingsReforge.class),
        BOOTS(AbstractBootsReforge.class);

        public final Class<? extends AbstractReforge> type;

        private Reforgable(Class<? extends AbstractReforge> type) {
            this.type = type;
        }
    }

    public void openReforgeGui(Player p) {
        // create gui
        // button for reforging item in a slot, tells you how many rubies you have
        // on close, make sure the item in that slot gets re added to player inventory
        // pick a random reforge, and run

        ChestGui gui = new ChestGui(6, "Reforge");
        gui.setOnGlobalClick(e -> {
            if(e.getClickedInventory().getType() == InventoryType.CHEST && e.getSlot() != 22) {
                e.setCancelled(true);
            }
        });
        gui.setOnClose(e -> {
            // test?
            if(e.getInventory().getItem(22) != null) {
                e.getPlayer().getInventory().addItem(e.getInventory().getItem(22));
            }
        });
        StaticPane pane = new StaticPane(0, 0, 9, 6);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 3, 1);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 4, 1);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 5, 1);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 3, 2);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 5, 2);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 3, 3);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 4, 3);
        pane.addItem(new GuiItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE)), 5, 3);

        pane.addItem(new GuiItem(new ItemStack(Material.ANVIL), e -> {
            ItemStack toReforge = e.getClickedInventory().getItem(22);
            reforgeItem(toReforge, p);
        }), 4, 5);
        // stupid workaround
        GuiItem dirt = new GuiItem(new ItemStack(Material.DIRT), e -> {});
        pane.addItem(dirt, 4, 2);
        pane.fillWith(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        pane.removeItem(dirt);
        gui.addPane(pane);
        gui.copy().show(p);
    }

    public boolean reforgeItem(ItemStack item, Player p) {
        boolean ret = false;
        if(item == null) return false;
        if(getReforges(item) != null && getReforges(item).size() > 8) {
            ret = reforgeItemOnce(item, p);
        } else {
            if(Util.percentChance(0.1)) {
                p.sendMessage(ChatColor.GREEN + "Your item was reforged twice!");
                ret = reforgeItemOnce(item, p);
            }
            // uh idk \/
            ret = ret && reforgeItemOnce(item, p);
        }
        return ret;
    }

    public boolean reforgeItemOnce(ItemStack item, Player p) {
        // if null
        // if count > 1
        // if not reforgable
        // if has max reforges
        // if not enough money
        if(item == null) {
            p.sendMessage(ChatColor.RED + "No item found!");
            return false;
        }
        if(item.getAmount() > 1) {
            p.sendMessage(ChatColor.RED + "You can't reforge a stack of items!");
            return false;
        }
        if(getReforgableType(item) == null) {
            p.sendMessage(ChatColor.RED + "That item is not reforgable!");
            return false;
        }
        if(getReforges(item) != null && getReforges(item).size() >= 10) {
            p.sendMessage(ChatColor.RED + "That item already has the max amount of reforges!");
            return false;
        }
        if(BoxPlugin.instance.getMarketManager().getRubies(p) < 5) {
            p.sendMessage(ChatColor.RED + "You don't have enough rubies!");
            return false;
        }
        Reforgable type = getReforgableType(item);
        // a list of reforges that can be applied to the item
        ArrayList<Class<? extends AbstractReforge>> possibleReforges = new ArrayList<>();
        for(String name : Reforge.getKeys()) {
            Class<? extends AbstractReforge> reforge = Reforge.getByName(name);
            // instanceof the type of abstract reforge, so if it's an abstractswordreforge, abstractbowreforge, etc
            if(reforge.isAssignableFrom(type.type)) {
                possibleReforges.add(reforge);
            }
        }
        double totalWeight = 0;
        for(Class<? extends AbstractReforge> c : possibleReforges) {
            totalWeight += Reforge.ofLevel(c, 0).getWeight();
        }
        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < possibleReforges.size() - 1; ++idx) {
            r -= Reforge.ofLevel(possibleReforges.get(idx), 0).getWeight();
            if (r <= 0.0) break;
        }
        Class<? extends AbstractReforge> chosenReforgeType = possibleReforges.get(idx);

        // there has to be a better way to do this
        totalWeight = 0;
        // level, chance
        HashMap<Double, Double> possibleLevels = Reforge.ofLevel(chosenReforgeType, 0).getPossibleLevels();
        ArrayList<Double> levels = new ArrayList<>(possibleLevels.keySet());
        ArrayList<Double> chance = new ArrayList<>(possibleLevels.values());
        for(double weight : possibleLevels.values()) {
            totalWeight += weight;
        }
        idx = 0;
        for (double r = Math.random() * totalWeight; idx < possibleLevels.size() - 1; ++idx) {
            r -= chance.get(idx);
            if (r <= 0.0) break;
        }
        double chosenLevel = levels.get(idx);

        AbstractReforge chosenReforge = Reforge.ofLevel(chosenReforgeType, chosenLevel);
        Bukkit.broadcastMessage(chosenReforge.getClass().getName() + " " + chosenReforge.getLevel());
        chosenReforge.apply(item);
        return true;
    }


    /**
     *
     * @param item The item stack
     * @return If item is reforged
     */
    public boolean hasReforges(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "REFORGES"), PersistentDataType.TAG_CONTAINER);
    }

    public void stripReforge(ItemStack itemStack) {
        // get REFORGES attribute, loop through each one and set the value back to the original then remove the pdc
    }

    public boolean isSword(ItemStack item) {
        return item != null && ((
                item.getType().name().contains("SWORD")
        ) || (
                !item.getType().name().contains("AXE") && item.hasItemMeta() && item.getItemMeta().hasAttributeModifiers() && item.getItemMeta().getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED).size() > 0
                ));
    }

    public boolean isBow(ItemStack item) {
        return item != null && item.getType() == Material.BOW;
    }

    public boolean isAxe(ItemStack item) {
        return item != null && (item.getType() == Material.WOODEN_AXE || item.getType() == Material.STONE_AXE || item.getType() == Material.GOLDEN_AXE || item.getType() == Material.IRON_AXE || item.getType() == Material.DIAMOND_AXE || item.getType() == Material.NETHERITE_AXE);
    }

    public boolean isTrident(ItemStack item) {
        return item != null && item.getType() == Material.TRIDENT;
    }

    public boolean isPickaxe(ItemStack item) {
        return item != null && item.getType().name().contains("PICKAXE");
    }

    public boolean isBoots(ItemStack item) {
        return item != null && item.getType().name().contains("BOOTS");
    }

    public boolean isLeggings(ItemStack item) {
        return item != null && item.getType().name().contains("LEGGINGS");
    }

    public boolean isChestplate(ItemStack item) {
        return item != null && item.getType().name().contains("CHESTPLATE");
    }

    public boolean isHelmet(ItemStack item) {
        return item != null && (item.getType().name().contains("HELMET") || item.getType().name().contains("HEAD") || item.getType() == Material.SKELETON_SKULL || item.getType() == Material.WITHER_SKELETON_SKULL);
    }

    public boolean isCrossbow(ItemStack item) {
        return item != null & item.getType() == Material.CROSSBOW;
    }

    public Reforgable getReforgableType(ItemStack item) {
        return isHelmet(item) ? Reforgable.HELMET : isChestplate(item) ? Reforgable.CHESTPLATE : isLeggings(item) ? Reforgable.LEGGINGS : isBoots(item) ? Reforgable.BOOTS : isSword(item) ? Reforgable.SWORD : isBow(item) ? Reforgable.BOW : isAxe(item) ? Reforgable.AXE : isCrossbow(item) ? Reforgable.CROSSBOW : isTrident(item) ? Reforgable.TRIDENT : isPickaxe(item) ? Reforgable.PICKAXE : null;
    }

    public ArrayList<AbstractReforge> getReforges(ItemStack item) {
        if(item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(new NamespacedKey(BoxPlugin.instance, "REFORGES"), PersistentDataType.TAG_CONTAINER)) return null;
        PersistentDataContainer reforges = pdc.get(new NamespacedKey(BoxPlugin.instance, "REFORGES"), PersistentDataType.TAG_CONTAINER);
        ArrayList<AbstractReforge> rf = new ArrayList<>();
        for(NamespacedKey key : reforges.getKeys()) {
            PersistentDataContainer reforge = reforges.get(key, PersistentDataType.TAG_CONTAINER);
            String stat = reforge.get(new NamespacedKey(BoxPlugin.instance, "STAT"), PersistentDataType.STRING);
            double amount = reforge.get(new NamespacedKey(BoxPlugin.instance, "AMOUNT"), PersistentDataType.DOUBLE);
            double original = reforge.get(new NamespacedKey(BoxPlugin.instance, "ORIGINAL"), PersistentDataType.DOUBLE);
            rf.add(Reforge.ofLevel(Reforge.getByName(stat), amount));
        }
        return rf;
    }

    // reforged items contain the following:
    // pdc REFORGES: TAG_CONTAINER (pdc.getAdapterContext().newPersistentDataContainer())
    //    (id of reforge): TAG_CONTAINER
    //        STAT: (name of stat, can look up stat by name and get object in code)
    //        AMOUNT: (new stat number double)
    //        ORIGINAL: (original stat number double)

}
