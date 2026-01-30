package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class IceBornEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double DAMAGE_AMP_PERLVL = 0.06;
    private final double STACKING_SPEED_PER_LVL = 0.09;
    public IceBornEnchant() {
        setEnchantName("Ice Born");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "IceBorn_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§3§0§D§7§B§5" + getEnchantName() + " " + getlvlToRoman(lvl);
    }
    @Override
    public NamespacedKey getEnchantKey() {
        return enchantKey;
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
    public int getLevel(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if(itemMeta.getPersistentDataContainer().has(getEnchantKey())){
            return itemMeta.getPersistentDataContainer().get(getEnchantKey(), PersistentDataType.INTEGER);
        }
        return 0;
    }
    @Override
    public ItemStack removeEnchant(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if(hasEnchant(item)){
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

    public void setEnchantName(String name){
        this.enchantName = name;
    }
    @Override
    public String getEnchantName(){
        return enchantName;
    }
    @Override
    public boolean hasEnchant(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if(itemMeta.getPersistentDataContainer().has(getEnchantKey())){
            return true;
        }
        return false;
    }
    @Override
    public String getlvlToRoman(int level){
        String numeral = Integer.toString(level);
        if(level == 1){
            numeral = "I";
        } else if (level == 2) {
            numeral = "II";
        } else if (level == 3) {
            numeral = "III";
        } else if (level == 4) {
            numeral = "IV";
        } else if (level == 5) {
            numeral = "V";
        } else if (level == 6) {
            numeral = "VI";
        } else if (level == 7) {
            numeral = "VII";
        } else if (level == 8) {
            numeral = "VIII";
        } else if (level == 9) {
            numeral = "IX";
        } else if (level == 10) {
            numeral = "X";
        }
        return numeral;
    }
    @Override
    public double getDamageAmpFromTotalLevel(int totalLvl){
        return ((totalLvl * DAMAGE_AMP_PERLVL) + 1);
    }
    public double getStackingSpeedFromTotalLevel(int totalLvl){
        return ((totalLvl * STACKING_SPEED_PER_LVL) + 1);
    }
}
