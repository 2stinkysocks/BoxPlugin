package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EventHorizonEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    public EventHorizonEnchant() {
        setEnchantName("Event Horizon");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "EventHorizon_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§A§2§4§0§1§1E§x§8§9§3§9§1§7v§x§6§F§3§3§1§De§x§5§6§2§C§2§3n§x§3§C§2§5§2§9t §x§0§9§1§8§3§5H§x§1§D§1§C§4§Bo§x§3§0§2§1§6§0r§x§4§4§2§5§7§6i§x§5§8§2§9§8§Cz§x§6§B§2§E§A§1o§x§7§F§3§2§B§7n " + getlvlToRoman(lvl);
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
