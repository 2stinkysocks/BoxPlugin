package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BlackLightEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    public BlackLightEnchant() {
        setEnchantName("Black Light");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "BlackLight_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§5§A§1§1§A§2B§x§6§D§2§7§8§Cl§x§7§F§3§C§7§5a§x§9§2§5§2§5§Fc§x§A§4§6§7§4§8k §x§A§4§6§7§4§8L§x§9§2§5§2§5§Fi§x§7§F§3§C§7§5g§x§6§D§2§7§8§Ch§x§5§A§1§1§A§2t " + getlvlToRoman(lvl);
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
