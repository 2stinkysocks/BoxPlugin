package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class StormBornEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    public StormBornEnchant() {
        setEnchantName("Storm Born");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "StormBorn_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§1§1§8§A§A§2S§x§2§4§8§F§9§6t§x§3§7§9§5§8§Bo§x§4§B§9§A§7§Fr§x§5§E§9§F§7§4m §x§8§4§A§A§5§DB§x§9§8§A§F§5§1o§x§A§B§B§5§4§6r§x§B§E§B§A§3§An " + getlvlToRoman(lvl);
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
