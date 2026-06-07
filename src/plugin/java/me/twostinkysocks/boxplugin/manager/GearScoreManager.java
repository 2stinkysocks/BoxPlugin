package me.twostinkysocks.boxplugin.manager;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class GearScoreManager {

    public static NamespacedKey gearScoreKey = new NamespacedKey(BoxPlugin.instance, "gearScore");

    public static ItemStack setGearScore(ItemStack item, int score){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null){
            return  item;
        }
        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();

        if(HasGearScore(item)){
            itemMeta.getPersistentDataContainer().set(gearScoreKey, PersistentDataType.INTEGER, score);

            for (int i = 0; i < lore.size(); i++) {
                String strippedTxt = ChatColor.stripColor(lore.get(i));
                if (strippedTxt != null && strippedTxt.startsWith("Gear Score:")) {
                    // replace the existing line with the new value
                    lore.set(i, ChatColor.LIGHT_PURPLE + "Gear Score: " + score);
                    break;
                }
            }
        }
        else{
            itemMeta.getPersistentDataContainer().set(gearScoreKey, PersistentDataType.INTEGER, score);
            lore.addAll(List.of("", ChatColor.LIGHT_PURPLE + "" + "Gear Score: " + score));
        }

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public static boolean HasGearScore(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();

        PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

        if(itemData.has(gearScoreKey)){
            return true;
        }
        return false;//if no gear score was found
    }

    public static int GetGearScore(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        int itemGearScore = 0;
        int numItems = 1;

        PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

        if(!itemData.has(gearScoreKey)){
            return itemGearScore;
        }
        itemGearScore = itemData.get(gearScoreKey, PersistentDataType.INTEGER);
        if(item.getAmount() > 1){
            numItems = item.getAmount();
        }
        return (itemGearScore * numItems);
    }

    public static ItemMeta RemoveGearScore(ItemMeta item){
        item.getPersistentDataContainer().remove(gearScoreKey);

        List<String> lore = item.getLore();
        if (lore == null || lore.isEmpty()) {
            return item;
        }

        for (int i = 0; i < lore.size(); i++) {
            String strippedTxt = ChatColor.stripColor(lore.get(i));
            if (strippedTxt != null && strippedTxt.startsWith("Gear Score:")) {
                // remove the Gear Score line
                lore.remove(i);

                if (i - 1 >= 0 && ChatColor.stripColor(lore.get(i - 1)).isEmpty()) {
                    lore.remove(i - 1);
                }

                break;
            }
        }

        item.setLore(lore);
        return item;
    }

    public static void SetPlayerGearscore(Player p, int amount) {
        p.getPersistentDataContainer().set(gearScoreKey, PersistentDataType.INTEGER, amount);
    }

    public static boolean HasPlayerGearScore(Player p){

        if(p.getPersistentDataContainer().has(gearScoreKey, PersistentDataType.INTEGER)){
            return true;
        }
        return false;//if no gear score was found
    }

    public static int GetPlayerGearscore(Player p){
        int gearscoreAmount = 0;
        if(HasPlayerGearScore(p)){
            gearscoreAmount = p.getPersistentDataContainer().get(gearScoreKey, PersistentDataType.INTEGER);
        }
        return gearscoreAmount;
    }

    public static void UpdatePlayerGearscore(Player p){
        int totalGearscore = 0;
        //not needed to check armror slots and offhand each
//        ItemStack item;
//
//        item = p.getInventory().getHelmet();
//        if(item != null && item.hasItemMeta() && HasGearScore(item)){//next for are amor slots
//            totalGearscore += GetGearScore(item);
//        }
//        item = p.getInventory().getChestplate();
//        if(item != null && item.hasItemMeta() && HasGearScore(item)){
//            totalGearscore += GetGearScore(item);
//        }
//        item = p.getInventory().getLeggings();
//        if(item != null && item.hasItemMeta() && HasGearScore(item)){
//            totalGearscore += GetGearScore(item);
//        }
//        item = p.getInventory().getBoots();
//        if(item != null && item.hasItemMeta() && HasGearScore(item)){
//            totalGearscore += GetGearScore(item);
//        }
//
//        item = p.getInventory().getItemInOffHand();
//        if(item != null && item.hasItemMeta() && HasGearScore(item)){//off hand
//            totalGearscore += GetGearScore(item);
//        }

        for(ItemStack inventoryItem : p.getInventory().getContents()) {//everything else
            if(inventoryItem != null && inventoryItem.hasItemMeta() && HasGearScore(inventoryItem)){
                totalGearscore += GetGearScore(inventoryItem);
            }
        }
        SetPlayerGearscore(p, totalGearscore);
    }
}
