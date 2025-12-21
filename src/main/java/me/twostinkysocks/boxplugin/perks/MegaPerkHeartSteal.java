package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MegaPerkHeartSteal extends AbstractPerk {
    private final NamespacedKey perkKey = new NamespacedKey(BoxPlugin.instance, "mega_perk_heartsteal_stacks");
    public MegaPerkHeartSteal(){
        ItemStack guiItem = new ItemStack(Material.RED_DYE);
        ItemMeta meta = guiItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Heart Steal");
        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Gain stacks to increase your max health",
                "",
                ChatColor.DARK_GREEN + "Caps at 50 stacks, gain 1 max health",
                ChatColor.DARK_GREEN + "per every 2 stacks. Gain stacks from",
                ChatColor.DARK_GREEN + "either mobs with >= 100hp or players.",
                ChatColor.DARK_GREEN + "Deal 1.5x your stacks as bonus damage on proc.",
                "",
                ChatColor.DARK_GREEN + "lose 50% of stacks on death"

        ));
        guiItem.setItemMeta(meta);

        setGuiItem(guiItem);

        setCost(1);

        setKey("mega_perk_heartsteal");
    }
    public NamespacedKey getNamespaceKey(){
        return perkKey;
    }

    public int getStacks(Player p){
        if(p.getPersistentDataContainer().has(perkKey)){
            return p.getPersistentDataContainer().get(perkKey, PersistentDataType.INTEGER);
        }
        return 0;
    }

    public void setStacks(Player p, int numStacks){
        if(numStacks >= 50){
            p.getPersistentDataContainer().set(perkKey, PersistentDataType.INTEGER, 50);
        } else {
            p.getPersistentDataContainer().set(perkKey, PersistentDataType.INTEGER, numStacks);
        }
    }

    public int stacksToHealth(Player p){
        int numStacks = getStacks(p);
        int bonusHP = numStacks / 2;
        return bonusHP;
    }

    public void resetBaseHP(Player p){
        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
    }

    public void updateHealth(Player p){
        resetBaseHP(p);
        double curMaxHP = p.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        int bonusHP = stacksToHealth(p);
        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(curMaxHP + bonusHP);
        p.getAttribute(Attribute.MAX_HEALTH);
    }

    @Override
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        updateHealth(p);
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        int oldStacks = getStacks(p);
        if(oldStacks > 1){
            setStacks(p, (oldStacks / 2));// lose half stacks on death
        } else {
            setStacks(p, 0);
        }
        updateHealth(p);
        Util.debug(p, "you now have: " + (getStacks(p)) + " stacks, granting you: " + stacksToHealth(p) + " bonus health");
    }

    @Override
    public void onEquip(Player p) {
        setStacks(p, getStacks(p));
        updateHealth(p);
        Util.debug(p, "you have: " + (getStacks(p)) + " stacks, granting you: " + stacksToHealth(p) + " bonus health");
    }

    @Override
    public void onUnequip(Player p) {
        resetBaseHP(p);
    }
}
