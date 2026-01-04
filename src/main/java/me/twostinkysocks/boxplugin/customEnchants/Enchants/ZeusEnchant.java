package me.twostinkysocks.boxplugin.customEnchants.Enchants;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ZeusEnchant extends AbstractEnchant{
    private String enchantName;
    private NamespacedKey enchantKey;
    private final double MAXHP_DMG_PERLVL = 0.2;
    private final double CHANCE_PER_LVL = 0.04;

    public ZeusEnchant() {
        setEnchantName("Aspect of the Gods");
        setEnchantKey(new NamespacedKey(BoxPlugin.instance, "Zeus_Enchant"));
    }

    @Override
    public String getEnchantRGB(int lvl) {
        return "§x§E§2§D§8§8§7" + getEnchantName() + " " + getlvlToRoman(lvl);
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
    public int getLevel(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        if (itemMeta.getPersistentDataContainer().has(getEnchantKey())) {
            return itemMeta.getPersistentDataContainer().get(getEnchantKey(), PersistentDataType.INTEGER);
        }
        return 0;
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
    public String getlvlToRoman(int level) {
        String numeral = Integer.toString(level);
        if (level == 1) {
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

    public double getMXPHDamageFromTotalLevel(int totalLvl) {
        return (totalLvl * MAXHP_DMG_PERLVL);
    }
    public double getChanceFromTotalLevel(int totalLvl) {
        return (totalLvl * CHANCE_PER_LVL);
    }
}
