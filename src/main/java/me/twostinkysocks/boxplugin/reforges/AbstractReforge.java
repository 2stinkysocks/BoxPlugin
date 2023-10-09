package me.twostinkysocks.boxplugin.reforges;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents any reforge, instances contain a level
 * Children of this class implement functionality for canReforge, apply
 * apply will always check/create a pdc for reforges if it doesn't exist, and create the new reforge data structure
 * It will also apply the stats
 */
public abstract class AbstractReforge {

    private double level;

    protected double weight;

    // level, weight
    protected HashMap<Double, Double> possibleLevels;

    public abstract double getChance();

    public AbstractReforge(double level) {
        possibleLevels = new HashMap<>();
        weight = 0;
        this.level = level;
    }

    // reforged items contain the following:
    // pdc REFORGES: TAG_CONTAINER (pdc.getAdapterContext().newPersistentDataContainer())
    //    (id of reforge): TAG_CONTAINER
    //        STAT: (name of stat, can look up stat by name and get object in code)
    //        AMOUNT: (new stat number double)
    //        ORIGINAL: (original stat number double)
    public void apply(ItemStack item) {
        if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "REFORGES"), PersistentDataType.TAG_CONTAINER)) {
        } else {
            item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "REFORGES"), PersistentDataType.TAG_CONTAINER, item.getItemMeta().getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        }
    }

    public void remove(ItemStack item) {
        // idk remove this specific reforge
    }

    public double getLevel() {
        return level;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * HashMap Level, Chance
     * @return
     */
    public HashMap<Double, Double> getPossibleLevels() {
        return possibleLevels;
    }

    public abstract boolean canReforge(ItemStack item);

}
