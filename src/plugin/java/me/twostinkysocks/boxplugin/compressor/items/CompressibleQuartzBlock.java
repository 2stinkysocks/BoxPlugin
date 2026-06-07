package me.twostinkysocks.boxplugin.compressor.items;

import me.twostinkysocks.boxplugin.compressor.Compressible;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CompressibleQuartzBlock extends Compressible {
    @Override
    public boolean equals(ItemStack item) {
        return item != null && item.getType() == Material.QUARTZ && !item.hasItemMeta();
    }

    @Override
    public int getInput() {
        return 4;
    }

    @Override
    public int getOutput() {
        return 1;
    }

    @Override
    public ItemStack getCompressedItemStack(int count) {
        ItemStack item = new ItemStack(Material.QUARTZ_BLOCK, count);
        return item;
    }
}
