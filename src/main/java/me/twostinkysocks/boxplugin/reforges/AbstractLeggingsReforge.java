package me.twostinkysocks.boxplugin.reforges;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.manager.ReforgeManager.Reforgable;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractLeggingsReforge extends AbstractReforge {

    public AbstractLeggingsReforge(double level) {
        super(level);
    }

    @Override
    public boolean canReforge(ItemStack item) {
        Reforgable type = BoxPlugin.instance.getReforgeManager().getReforgableType(item);
        return type == Reforgable.LEGGINGS;
    }
}
