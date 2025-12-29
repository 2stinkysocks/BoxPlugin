package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import com.github.sirblobman.api.shaded.adventure.text.Component;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;
import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AbstractEnchant {
    private int level;
    private String enchantName;
    private NamespacedKey enchantKey;

    public AbstractEnchant() {
        this.enchantName = null;
        this.level = 0;
        this.enchantKey = new NamespacedKey(BoxPlugin.instance, "Abstract_Enchant");
    }

    public String  getEnchantRGB(int lvl){
        return "§x§3§6§C§8§2§E" + getEnchantName() + " " + getlvlToRoman(lvl);
    }

    public NamespacedKey getEnchantKey() {
        return enchantKey;
    }

    public ItemStack setLevel(ItemStack item, int lvl) {
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.getPersistentDataContainer().set(getEnchantKey(), PersistentDataType.INTEGER, lvl);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack removeEnchant(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if(hasEnchant(item)){
            itemMeta.getPersistentDataContainer().remove(getEnchantKey());
        }
        item.setItemMeta(itemMeta);
        return item;
    }
    public int getLevel(ItemStack item){
        return level;
    }
    public boolean hasEnchant(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if(itemMeta.getPersistentDataContainer().has(getEnchantKey())){
            return true;
        }
        return false;
    }

    public void setEnchantKey(NamespacedKey key) {
        this.enchantKey = key;
    }

    public void setEnchantName(String name){
        this.enchantName = name;
    }

    public String getEnchantName(){
        return enchantName;
    }

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

}
