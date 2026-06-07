package me.twostinkysocks.boxplugin.compressor;

import org.bukkit.inventory.ItemStack;

public abstract class Compressible {

    public abstract boolean equals(ItemStack item);

    public abstract int getInput();

    public abstract int getOutput();

    public abstract ItemStack getCompressedItemStack(int count);

}
