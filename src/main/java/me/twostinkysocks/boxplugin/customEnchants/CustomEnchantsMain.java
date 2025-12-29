package me.twostinkysocks.boxplugin.customEnchants;

import com.github.sirblobman.api.shaded.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.google.common.collect.Maps;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customEnchants.Enchants.AbstractEnchant;
import me.twostinkysocks.boxplugin.customEnchants.Enchants.BrambleEnchant;
import me.twostinkysocks.boxplugin.customEnchants.Enchants.OverGrowthEnchant;
import me.twostinkysocks.boxplugin.customEnchants.Enchants.PrickleEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomEnchantsMain {

    public void onEnable(){
        BoxPlugin.instance.getServer().getPluginManager().registerEvents(new Listeners(), BoxPlugin.instance);
    }
    public final NamespacedKey hasCustomEnchKey = new NamespacedKey(BoxPlugin.instance, "HasCustomEnch");
    public enum Enchant {
        Bramble(new BrambleEnchant()),
        Overgrowth(new OverGrowthEnchant()),
        Prickle(new PrickleEnchant());

        private static final Map<String, CustomEnchantsMain.Enchant> BY_NAME = Maps.newHashMap();
        public final AbstractEnchant instance;
        private Enchant(AbstractEnchant instance) {
            this.instance = instance;
        }

        public static Enchant getByName(String name) {
            return BY_NAME.get(name);
        }

        public static List<String> getKeys() {
            return new ArrayList<String>(BY_NAME.keySet());
        }

        static {
            for (CustomEnchantsMain.Enchant enchant : values()) {
                BY_NAME.put(enchant.instance.getEnchantName(), enchant);
            }
        }
    }

    public boolean hasCustomEnchants(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) return false;
        if(itemMeta.getPersistentDataContainer().has(hasCustomEnchKey, PersistentDataType.BOOLEAN)){
            return (Boolean.TRUE.equals(itemMeta.getPersistentDataContainer().get(hasCustomEnchKey, PersistentDataType.BOOLEAN)));
        }
        return false;
    }
    public ItemStack setCustomEnchantStatus(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) return item;
        if(itemMeta.getPersistentDataContainer().has(hasCustomEnchKey)){ //reset key on each call, only add it back if a custom enchant is present
            itemMeta.getPersistentDataContainer().remove(hasCustomEnchKey);
        }
        //int numEnchants = 0;
        for(String enchName : Enchant.getKeys()){
            if(itemMeta.getPersistentDataContainer().has(Enchant.getByName(enchName).instance.getEnchantKey())){
                //numEnchants++;
                itemMeta.getPersistentDataContainer().set(hasCustomEnchKey, PersistentDataType.BOOLEAN, true);
                //itemMeta.getPersistentDataContainer().set(hasCustomEnchKey, PersistentDataType.INTEGER, numEnchants);
            }
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    public ArrayList<Enchant> getCustomEnchantsOnItem(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) return null;
        ArrayList<Enchant> enchantsArr = new ArrayList<>();
        for(String enchName : Enchant.getKeys()){
            if(itemMeta.getPersistentDataContainer().has(Enchant.getByName(enchName).instance.getEnchantKey())){
                enchantsArr.add(Enchant.getByName(enchName));
            }
        }
        return enchantsArr;
    }

    public ItemStack setCustomEnchant(ItemStack item, String enchName, int lvl){
        Enchant enchant = Enchant.getByName(enchName);
        if(enchant == null) return item;

        if(lvl > 0){
            item = enchant.instance.setLevel(item, lvl);
        }
        else {
            item = enchant.instance.removeEnchant(item);
        }
        item = setCustomEnchantStatus(item);
//        if(!hasCustomEnchants(item)){
//            return;
//        }
        //lore part
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) return item;
        List<String> lore = itemMeta.hasLore() ? new ArrayList<>(itemMeta.getLore()) : new ArrayList<>();

        // Remove old lore lines matching any custom enchant
        List<String> keys = Enchant.getKeys();
        lore.removeIf(line -> {
            for(String key : keys){
                if(line.contains(key)) return true;
            }
            return false;
        });

        //re add all lines
        int enchLevel;
        for(String key : keys){
            enchant = Enchant.getByName(key);
            if(enchant.instance.hasEnchant(item)){
                enchLevel = enchant.instance.getLevel(item);
                lore.add(0, enchant.instance.getEnchantRGB(enchLevel)); // insert as first line
            }
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public int getCombinedEnchLevel(Player p, Enchant enchant ){
        ItemStack item;
        int totalEnchantLevel = 0;

        item = p.getInventory().getHelmet();//next for are amor slots
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                totalEnchantLevel += enchant.instance.getLevel(item);
            }
        }

        item = p.getInventory().getChestplate();
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                totalEnchantLevel += enchant.instance.getLevel(item);
            }
        }

        item = p.getInventory().getLeggings();
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                totalEnchantLevel += enchant.instance.getLevel(item);
            }
        }

        item = p.getInventory().getBoots();
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                totalEnchantLevel += enchant.instance.getLevel(item);
            }
        }
        return totalEnchantLevel;
    }

    public boolean hasFullSetBonus(Player p, Enchant enchant){
        ItemStack item;
        int numGearItemsWithEnchant = 0;

        item = p.getInventory().getHelmet();//next for are amor slots
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                numGearItemsWithEnchant++;
            }
        }

        item = p.getInventory().getChestplate();
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                numGearItemsWithEnchant++;
            }
        }

        item = p.getInventory().getLeggings();
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                numGearItemsWithEnchant++;
            }
        }

        item = p.getInventory().getBoots();
        if(item != null && item.hasItemMeta()){

            if (enchant.instance.hasEnchant(item)){
                numGearItemsWithEnchant++;
            }
        }
        if(numGearItemsWithEnchant == 4){
            return true;
        } else return false;
    }

}
