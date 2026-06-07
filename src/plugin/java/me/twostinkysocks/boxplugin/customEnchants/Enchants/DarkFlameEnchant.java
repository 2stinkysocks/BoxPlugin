package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class DarkFlameEnchant extends ArcticEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double CHANCE_PER_LVL = 80;
    public DarkFlameEnchant() {
        setEnchantName("Dark Flame");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "DarkFlame_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§A§2§1§1§1§1D§x§8§C§1§0§1§Da§x§7§7§0§F§2§9r§x§6§1§0§E§3§4k §x§3§6§0§C§4§CF§x§4§8§1§6§6§7l§x§5§B§1§F§8§2a§x§6§D§2§9§9§Cm§x§7§F§3§2§B§7e " + getlvlToRoman(lvl);
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

    @Override
    public double getChanceFromTotalLevel(int totalLvl) {
        return (totalLvl * CHANCE_PER_LVL);
    }
}
