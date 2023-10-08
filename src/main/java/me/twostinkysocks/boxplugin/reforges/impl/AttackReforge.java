package me.twostinkysocks.boxplugin.reforges.impl;

import me.twostinkysocks.boxplugin.reforges.AbstractSwordReforge;
import org.bukkit.inventory.ItemStack;

public class AttackReforge extends AbstractSwordReforge {

    public AttackReforge(double level) {
        super(level);
        super.possibleLevels.put(1.0, 0.6);
        super.possibleLevels.put(2.0, 0.3);
        super.possibleLevels.put(3.0, 0.1);
    }

    @Override
    public void apply(ItemStack item) {
        super.apply(item);

    }

    @Override
    public double getChance() {
        return 0;
    }
}
