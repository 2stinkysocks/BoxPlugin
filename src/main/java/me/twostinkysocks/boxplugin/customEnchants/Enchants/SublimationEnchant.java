package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SublimationEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    public SublimationEnchant() {
        setEnchantName("Sublimation");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "Sublimation_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§2§7§E§2§A§DS§x§3§E§E§4§B§7u§x§5§4§E§6§C§1b§x§6§B§E§8§C§Bl§x§8§1§E§A§D§5i§x§9§8§E§C§D§Fm§x§9§3§E§A§D§Aa§x§8§E§E§8§D§5t§x§8§8§E§7§D§1i§x§8§3§E§5§C§Co§x§7§E§E§3§C§7n" + " " + getlvlToRoman(lvl);
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
