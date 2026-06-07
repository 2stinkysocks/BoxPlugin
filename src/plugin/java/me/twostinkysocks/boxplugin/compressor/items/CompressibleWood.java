package me.twostinkysocks.boxplugin.compressor.items;

import me.twostinkysocks.boxplugin.compressor.Compressible;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

public class CompressibleWood extends Compressible {
    @Override
    public boolean equals(ItemStack item) {
        return item != null && item.getType() == Material.OAK_LOG && !item.hasItemMeta();
    }

    @Override
    public int getInput() {
        return 64;
    }

    @Override
    public int getOutput() {
        return 1;
    }

    @Override
    public ItemStack getCompressedItemStack(int count) {
        ItemStack item = new ItemStack(Material.DARK_OAK_WOOD, count);
        Repairable meta = (Repairable) item.getItemMeta();
        meta.setRepairCost(1);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.AQUA + "Compressed Wood");
        item.setItemMeta(meta);
        return item;
    }
}
