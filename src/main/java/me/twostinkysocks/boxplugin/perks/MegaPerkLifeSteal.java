package me.twostinkysocks.boxplugin.perks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MegaPerkLifeSteal extends AbstractPerk {
    public MegaPerkLifeSteal() {
        ItemStack guiItem = new ItemStack(Material.REDSTONE);
        ItemMeta meta = guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Lifesteal");
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Heal for 15% of the damage enemies take from your melee hits"
        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(1);

        setKey("mega_perk_lifesteal");
    }
}
