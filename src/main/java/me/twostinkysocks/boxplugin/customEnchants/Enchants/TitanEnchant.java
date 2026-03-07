package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class TitanEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double DAMAGE_AMP_PERLVL = 0.02;
    public TitanEnchant() {
        setEnchantName("Titan");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "Titan_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§B§4§0§B§B§E§lT§x§C§0§1§7§A§3§li§x§C§D§2§4§8§7§lt§x§D§9§3§0§6§C§la§x§E§5§3§C§5§0§ln" + " " + getlvlToRoman(lvl);//titan with RGB
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
    public double getDamageAmpFromTotalLevel(int totalLvl){
        return (totalLvl * DAMAGE_AMP_PERLVL);
    }
}
