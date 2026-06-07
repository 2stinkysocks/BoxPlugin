package me.twostinkysocks.boxplugin.compressor.items;

import me.twostinkysocks.boxplugin.compressor.Compressible;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

public class CompressibleDiamond extends Compressible {
    @Override
    public boolean equals(ItemStack item) {
        return item != null && item.getType() == Material.DIAMOND_BLOCK && !item.hasItemMeta();
    }

    @Override
    public int getInput() {
        return 32;
    }

    @Override
    public int getOutput() {
        return 1;
    }

    @Override
    public ItemStack getCompressedItemStack(int count) {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK, count);
        Repairable meta = (Repairable) item.getItemMeta();
        meta.setRepairCost(1);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.AQUA + "Compressed Diamond");
        item.setItemMeta(meta);
        return item;
    }
}
