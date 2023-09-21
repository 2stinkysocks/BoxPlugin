package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import com.google.common.collect.Maps;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.reforges.AbstractReforge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ReforgeManager {

    // reforged items contain the following:
    // pdc REFORGES: TAG_CONTAINER (pdc.getAdapterContext().newPersistentDataContainer())
    //    (id of reforge): TAG_CONTAINER
    //        STAT: (name of stat, can look up stat by name and get object in code)
    //        AMOUNT: (new stat number double)
    //        ORIGINAL: (original stat number double)

    public enum Reforgable {
        SWORD,
        BOW,
        AXE,
        CROSSBOW,
        TRIDENT,
        PICKAXE,
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS;
    }

    public class Reforge {
        // This needs to be the type of the reforge
        private static final Class<? extends AbstractReforge> reforge1 = null;

        private static final Map<String, Class<? extends AbstractReforge>> BY_NAME = Maps.newHashMap();

        public static Class<? extends AbstractReforge> getByName(String name) {
            return BY_NAME.get(name);
        }

        public static List<String> getKeys() {
            return new ArrayList<String>(BY_NAME.keySet());
        }

        public static AbstractReforge ofLevel(Class<? extends AbstractReforge> reforge, double level) {
            try {
                return reforge.getDeclaredConstructor(Double.class).newInstance(level);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        static {
            try {
                for (Field reforge : Arrays.stream(Reforge.class.getDeclaredFields()).filter(f -> f.getType().equals(Class.class)).collect(Collectors.toList())) {
                    BY_NAME.put(reforge.getName(), (Class) reforge.get(null));
                }
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }
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

    public void reforgeItem(ItemStack item, Player p) {
        // if null
        // if count > 1
        // if not reforgable
        // if has max reforges
        // if not enough money
        if(item == null) {
            p.sendMessage(ChatColor.RED + "No item found!");
            return;
        }
        if(item.getAmount() > 1) {
            p.sendMessage(ChatColor.RED + "You can't reforge a stack of items!");
            return;
        }
        if(getReforgableType(item) == null) {
            p.sendMessage(ChatColor.RED + "That item is not reforgable!");
            return;
        }
        if()
    }

    /**
     *
     * @param item The item stack
     * @return If item is reforged
     */
    public boolean hasReforges(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "REFORGES"), PersistentDataType.TAG_CONTAINER);
    }

    public void setReforge(Reforge reforge, double amount) {
        // get reforge instance
        // call
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
