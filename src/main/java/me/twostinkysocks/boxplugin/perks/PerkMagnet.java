package me.twostinkysocks.boxplugin.perks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PerkMagnet extends AbstractPerk {
    public PerkMagnet() {
        ItemStack guiItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Magnet");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Teleports block and mob drops to your inventory"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(16);

        setKey("perk_magnet");
    }
}
