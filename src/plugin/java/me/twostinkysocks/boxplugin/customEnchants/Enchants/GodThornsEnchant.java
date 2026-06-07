package me.twostinkysocks.boxplugin.customEnchants.Enchants;


import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GodThornsEnchant extends AbstractEnchant {
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double DMG_PERCENT_PERLVL = 0.07;//7*4 = 28
    private final int CHANCE_PER_LVL = 16;
    public GodThornsEnchant() {
        setEnchantName("Godly Thorns");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "GodThorns_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§E§2§C§E§2§7G§x§E§1§D§2§3§5o§x§E§1§D§5§4§2d§x§E§0§D§9§5§0l§x§D§F§D§C§5§Ey §x§D§E§E§3§7§9T§x§D§E§E§3§7§9h§x§D§E§E§3§7§9o§x§D§E§E§3§7§9r§x§D§E§E§3§7§9n§x§D§E§E§3§7§9s" + " " + getlvlToRoman(lvl);
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
    public double getDamageFromTotalLevel(int totalLvl) {
        return (totalLvl * DMG_PERCENT_PERLVL);
    }
    @Override
    public double getChanceFromTotalLevel(int totalLvl){
        return (totalLvl * CHANCE_PER_LVL);
    }
}
