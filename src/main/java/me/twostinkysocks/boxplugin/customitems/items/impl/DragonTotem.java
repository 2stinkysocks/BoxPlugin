package me.twostinkysocks.boxplugin.customitems.items.impl;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.manager.PerksManager;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class DragonTotem extends CustomItem {
    private HashMap<UUID, Long> cooldown;

    public DragonTotem(CustomItemsMain plugin) {
        super(
                ChatColor.GOLD + "Dragon Totem",
                "Dragon_TOTEM",
                Material.TOTEM_OF_UNDYING,
                plugin,
                true
        );
        cooldown = new HashMap<>();

        setTotemUse((e) -> {
            Player p = (Player) e.getEntity();
            // allow shield blocking to take priority
            if(p.hasPermission("customitems.cooldownbypass") || !cooldown.containsKey(p.getUniqueId()) || cooldown.get(p.getUniqueId()) < System.currentTimeMillis()) {
                cooldown.put(p.getUniqueId(), System.currentTimeMillis() + (long)(1000*360 * (BoxPlugin.instance.getPerksManager().getSelectedMegaPerks(p).contains(PerksManager.MegaPerk.MEGA_COOLDOWN_REDUCTION) ? 0.5 : 1)));
                totemPop(p);
                NamespacedKey itemIdKey = new NamespacedKey(BoxPlugin.instance, "ITEM_ID");
                ItemStack totem = p.getInventory().getItemInOffHand();
                if(totem != null && totem.getType() == Material.TOTEM_OF_UNDYING && totem.getItemMeta().getPersistentDataContainer().has(itemIdKey, PersistentDataType.STRING) && totem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING).equals(this.getItemId())){
                    p.getInventory().setItemInOffHand(totem);
                } else {
                    totem = p.getInventory().getItemInMainHand();
                    if(totem != null && totem.getType() == Material.TOTEM_OF_UNDYING && totem.getItemMeta().getPersistentDataContainer().has(itemIdKey, PersistentDataType.STRING) && totem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING).equals(this.getItemId())){
                        p.getInventory().setItemInMainHand(totem);
                    }
                }
            } else {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "Your totem did not go off because it was on cooldown");
            }
        });
    }

    public void totemPop(Player p){
        p.setHealth(Math.min(p.getAttribute(Attribute.MAX_HEALTH).getValue(), 30));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 160, 5, true, false));
    }
}
