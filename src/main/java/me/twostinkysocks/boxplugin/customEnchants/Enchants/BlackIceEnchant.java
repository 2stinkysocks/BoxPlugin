package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BlackIceEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double DAMAGE_AMP_PERLVL = 0.04;
    public BlackIceEnchant() {
        setEnchantName("Black Ice");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "BlackIce_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§0§D§2§4§3§CB§x§1§5§3§4§5§2l§x§1§C§4§5§6§8a§x§2§4§5§5§7§Ec§x§2§B§6§5§9§4k §x§2§B§4§C§9§4I§x§2§B§3§F§9§4c§x§2§B§3§2§9§4e " + getlvlToRoman(lvl);//titan with RGB
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
