package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class MilkPotion extends CustomItem {

    public MilkPotion(CustomItemsMain plugin) {
        super(
                ChatColor.WHITE + "Splash Potion of Milk",
                "MILK_POTION",
                Material.SPLASH_POTION,
                plugin
        );
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack item = super.getItemStack();
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(Color.WHITE);
        meta.setBasePotionData(new PotionData(PotionType.AWKWARD));
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

}
