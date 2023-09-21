package me.twostinkysocks.boxplugin.reforges;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractReforge {

    private ItemStack guiItem;

    private double level;

    public AbstractReforge(double level) {
        this.guiItem = null;
    }

    public ItemStack getGuiItem(Player p) {
        return guiItem;
    }

    public void setGuiItem(ItemStack guiItem) {
        this.guiItem = guiItem;
    }

    public void applyReforge(ItemStack itemStack) {

    }

    public double getLevel() {
        return level;
    }

    public void stripReforge(ItemStack itemStack) {
        // get REFORGES attribute, loop through each one and set the value back to the original then remove the pdc
    }

}
