package me.twostinkysocks.boxplugin.manager;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import com.google.common.collect.Maps;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.reforges.AbstractReforge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RubyManager {

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

    public enum Reforge {
        reforge1(null),
        reforge2(null);

        public final AbstractReforge instance;

        private static final Map<String, Reforge> BY_NAME = Maps.newHashMap();

        private Reforge(AbstractReforge instance) {
            this.instance = instance;
        }

        public static Reforge getByName(String name) {
            return BY_NAME.get(name);
        }

        public static List<String> getKeys() {
            return new ArrayList<String>(BY_NAME.keySet());
        }

        static {
            for (Reforge reforge : values()) {
                BY_NAME.put(reforge.name(), reforge);
            }
        }
    }

    public void openConversionGui(Player p) {
        // create gui
        // contains button for depositing all rubies in inventory
        // contains buttons for withdrawing 1x, 16x, 64x rubies into items
        // contains button to buy ruby
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
            e.getPlayer().getInventory().addItem(e.getInventory().getItem(22));
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

        pane.addItem(new GuiItem(new ItemStack(Material.END_PORTAL_FRAME), e -> {
            ItemStack toReforge = e.getClickedInventory().getItem(22);
            reforgeItem(toReforge);
        }), 4, 5);

        pane.fillWith(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        pane.removeItem(4,2);
        gui.addPane(pane);
        gui.copy().show(p);
    }

    public void reforgeItem(ItemStack item) {
        // if null
        // if count > 1
        // if not reforgable
        // if has max reforges
        // if not enough money
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

}
