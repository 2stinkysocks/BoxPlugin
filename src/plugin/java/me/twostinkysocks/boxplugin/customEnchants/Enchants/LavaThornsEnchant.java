package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class LavaThornsEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double DAMAGE_PER_LEVEL = 3;
    private final int CHANCE_PER_LVL = 12;
    public LavaThornsEnchant() {
        setEnchantName("Lava Thorns");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "LavaThorns_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§B§1§3§1§4§6L§x§B§8§3§7§4§5a§x§C§0§3§E§4§3v§x§C§7§4§4§4§2a §x§D§6§5§1§3§FT§x§D§6§5§1§3§Fh§x§D§6§5§1§3§Fo§x§D§6§5§1§3§Fr§x§D§6§5§1§3§Fn§x§D§6§5§1§3§Fs" + " " + getlvlToRoman(lvl);
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
