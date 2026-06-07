package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import com.github.sirblobman.api.shaded.adventure.text.Component;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;
import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PrickleEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final int DAMAGE_PER_LEVEL = 1;
    public PrickleEnchant() {
        setEnchantName("Prickle");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "Prickle_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl){
        return "§x§2§1§9§8§1§A" + getEnchantName() + " " + getlvlToRoman(lvl);
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
    public double getDamageFromTotalLevel(int totalLvl){
        return (totalLvl * DAMAGE_PER_LEVEL);
    }

}
