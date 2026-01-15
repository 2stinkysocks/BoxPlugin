package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import com.github.sirblobman.api.shaded.adventure.text.Component;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;
import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class OverGrowthEnchant extends AbstractEnchant {
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double PERCENT_HEAL_PER_LEVEL = 0.035;
    private final int CHANCE_PER_LVL = 4;
    public OverGrowthEnchant() {
        setEnchantName("Overgrowth");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "OverGrowth_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§3§6§C§8§2§E" + getEnchantName() + " " + getlvlToRoman(lvl);
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

    public double getHealFromTotalLevel(int totalLvl){
        return (totalLvl * PERCENT_HEAL_PER_LEVEL);
    }

    public int getChanceFromTotalLevel(int totalLvl){
        return (totalLvl * CHANCE_PER_LVL);
    }
}
