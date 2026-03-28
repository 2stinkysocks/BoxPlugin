package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SlipperyEnchant extends ArcticEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    public SlipperyEnchant() {
        setEnchantName("Slippery");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "Slippery_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§3§2§4§1§B§7S§x§2§D§4§B§B§4l§x§2§9§5§6§B§1i§x§2§4§6§0§A§Ep§x§1§F§6§B§A§Bp§x§1§A§7§5§A§8e§x§1§6§8§0§A§5r§x§1§1§8§A§A§2y " + getlvlToRoman(lvl);
    }
    @Override
    public ItemStack removeEnchant(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if (hasEnchant(item)) {
            itemMeta.getPersistentDataContainer().remove(getEnchantKey());
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    @Override
    public void setEnchantKey(NamespacedKey key) {
        this.enchantKey = key;
    }
    @Override
    public NamespacedKey getEnchantKey() {
        return enchantKey;
    }
    @Override
    public void setEnchantName(String name) {
        this.enchantName = name;
    }
    @Override
    public String getEnchantName() {
        return enchantName;
    }
    @Override
    public boolean hasEnchant(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if (itemMeta.getPersistentDataContainer().has(getEnchantKey())) {
            return true;
        }
        return false;
    }
    @Override
    public ItemStack setLevel(ItemStack item, int lvl) {
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.getPersistentDataContainer().set(getEnchantKey(), PersistentDataType.INTEGER, lvl);
        item.setItemMeta(itemMeta);
        return item;
    }
    @Override
    public int getLevel(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if (itemMeta.getPersistentDataContainer().has(getEnchantKey())) {
            return itemMeta.getPersistentDataContainer().get(getEnchantKey(), PersistentDataType.INTEGER);
        }
        return 0;
    }
}
