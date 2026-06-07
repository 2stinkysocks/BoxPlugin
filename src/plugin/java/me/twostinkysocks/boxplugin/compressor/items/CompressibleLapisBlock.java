package me.twostinkysocks.boxplugin.compressor.items;

import me.twostinkysocks.boxplugin.compressor.Compressible;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CompressibleLapisBlock extends Compressible {
    @Override
    public boolean equals(ItemStack item) {
        return item != null && item.getType() == Material.LAPIS_LAZULI && !item.hasItemMeta();
    }

    @Override
    public int getInput() {
        return 9;
    }

    @Override
    public int getOutput() {
        return 1;
    }

    @Override
    public ItemStack getCompressedItemStack(int count) {
        return new ItemStack(Material.LAPIS_BLOCK, count);
    }
}
