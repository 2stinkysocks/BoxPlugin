package me.twostinkysocks.boxplugin.compressor.items;

import me.twostinkysocks.boxplugin.compressor.Compressible;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CompressibleRedstoneBlock extends Compressible {
    @Override
    public boolean equals(ItemStack item) {
        return item != null && item.getType() == Material.REDSTONE && !item.hasItemMeta();
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
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK, count);
        return item;
    }
}
