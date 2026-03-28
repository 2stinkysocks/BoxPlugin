package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class IceThornsEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double DAMAGE_PER_LEVEL = 2;
    private final int CHANCE_PER_LVL = 12;
    public IceThornsEnchant() {
        setEnchantName("Ice Thorns");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "IceThorns_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§3§1§B§1§8§DI§x§3§4§B§8§9§7c§x§3§7§C§0§A§1e §x§3§C§C§F§B§4T§x§3§F§D§6§B§Eh§x§3§F§D§6§B§Eo§x§3§F§D§6§B§Er§x§3§F§D§6§B§En§x§3§F§D§6§B§Es" + " " + getlvlToRoman(lvl);
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
    public double getDamageFromTotalLevel(int totalLvl){
        return (totalLvl * DAMAGE_PER_LEVEL);
    }
    @Override
    public double getChanceFromTotalLevel(int totalLvl){
        return (totalLvl * CHANCE_PER_LVL);
    }
}
