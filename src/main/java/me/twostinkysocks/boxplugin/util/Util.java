package me.twostinkysocks.boxplugin.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.twostinkysocks.boxplugin.BoxPlugin;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.world.entity.EntityLiving;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static boolean isRuby(ItemStack item) {
        if(item != null && item.getType() == Material.PLAYER_HEAD && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().hasLore()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("Ruby");
        } else {
            return false;
        }
    }

    public static boolean isCoin(ItemStack item) {
        if(item != null && item.getType() == Material.SUNFLOWER && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().hasEnchants() && item.getItemMeta().hasLore()) {
            return item.getItemMeta().getDisplayName().contains("Xanatos Coin");
        } else {
            return false;
        }
    }

    public static boolean isGigaCoin(ItemStack item) {
        if(item != null && item.getType() == Material.GOLD_BLOCK && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().hasEnchants() && item.getItemMeta().hasLore()) {
           return item.getItemMeta().getDisplayName().contains("Giga Coin");
        } else {
            return false;
        }
    }

    public static boolean isTeraCube(ItemStack item) {
        if(item != null && item.getType() == Material.DIAMOND_BLOCK && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().hasEnchants() && item.getItemMeta().hasLore()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("TeraCube");
        } else {
            return false;
        }
    }

    public static boolean isHexidium(ItemStack item) {
        if(item != null && item.getType() == Material.EMERALD_BLOCK && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().hasEnchants() && item.getItemMeta().hasLore()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("HEXIDIUM");
        } else {
            return false;
        }
    }

    public static boolean isCurrency(ItemStack item) {
        return isCoin(item) || isGigaCoin(item) || isTeraCube(item) || isHexidium(item);
    }

    public static boolean isGhastlyHerb(ItemStack item) {
        if(item != null && item.getType() == Material.WITHER_ROSE && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().hasEnchants() && item.getItemMeta().hasLore()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("Ghastly Herb");
        } else {
            return false;
        }
    }

    public static boolean isPerkItem(ItemStack item) {
        return item != null && item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) && item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(BoxPlugin.instance, "perk_item"), PersistentDataType.INTEGER) == 1;
    }

    public static ItemStack coin(int amount) {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(List.of("spend with the traders in spawn"));
        meta.setDisplayName("§6Xanatos Coin");
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    public static ItemStack gigaCoin(int amount) {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(List.of("64x Xanatos Coins"));
        meta.setDisplayName("§6§lGiga Coin");
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    public static ItemStack teraCube(int amount) {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(List.of("§6§l64x Giga Coin", "§64096x Xanatos Coin"));
        meta.setDisplayName("§x§1§F§7§0§F§B§lT§x§1§D§8§3§F§B§le§x§1§C§9§6§F§C§lr§x§1§A§A§9§F§C§la§x§1§8§B§D§F§C§lC§x§1§6§D§0§F§C§lu§x§1§5§E§3§F§D§lb§x§1§3§F§6§F§D§le");
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    public static ItemStack hexidium(int amount) {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(List.of("§1§l64x §x§0§0§5§E§F§D§lT§x§0§0§7§1§F§D§le§x§0§0§8§5§F§D§lr§x§0§0§9§8§F§D§la§x§0§0§A§C§F§D§lC§x§0§0§B§F§F§D§lu§x§0§0§D§3§F§D§lb§x§0§0§E§6§F§D§le", "§6262,144 Xanatos Coin"));
        meta.setDisplayName("§x§2§7§F§D§2§9§lH§x§2§1§F§D§3§5§lE§x§1§C§F§D§4§1§lX§x§1§6§F§D§4§D§lI§x§1§1§F§D§5§8§lD§x§0§B§F§D§6§4§lI§x§0§6§F§D§7§0§lU§x§0§0§F§D§7§C§lM");
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    public static ItemStack[] itemArray(int amount, Function<Integer, ItemStack> itemstack) {
        int stacks = amount/64;
        int remaining = amount%64;
        ItemStack[] coinStacks = new ItemStack[stacks+1];
        for(int i = 0; i < stacks; i++) {
            coinStacks[i] = itemstack.apply(64);
        }
        coinStacks[coinStacks.length-1] = itemstack.apply(remaining);
        return coinStacks;
    }

    /**
     * Returns an array of combined currency stacks to produce as few items as possible worth the amount
     * @param amount amount of coins
     */
    public static ItemStack[] reducedCurrencyArray(int amount) {
        int hexidium = amount / 262144;
        amount = amount % 262144;
        int tera = amount / 4096;
        amount = amount % 4096;
        int giga = amount / 64;
        amount = amount % 64;
        int coin = amount;
        ItemStack[] h = itemArray(hexidium, Util::hexidium);
        ItemStack[] t = itemArray(tera, Util::teraCube);
        ItemStack[] g = itemArray(giga, Util::gigaCoin);
        ItemStack[] c = itemArray(coin, Util::coin);
        return arrayConcat(h, t, g, c);
    }

    public static <T> T[] arrayConcat(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static void spawnTallCircle(World w, Location circleLocation, Vector direction, double radius, int points, Particle.DustOptions dustOptions) {
        double interval = 2*Math.PI/points;
        for(int i = 0; i < points; i++) {
            double t = i*interval;
            double x = radius * Math.cos(t);
            double y = radius * Math.sin(t);
            double z = 0;
            Vector v = new Vector(x,y,z);
            v = MathUtil.rotateFunction(v, new Location(w, 0,0,0).setDirection(direction));
            w.spawnParticle(Particle.SCULK_SOUL, new Location(w, circleLocation.getX() + v.getX(), circleLocation.getY() + v.getY(), circleLocation.getZ() + v.getZ()), 4, 0, 0.8, 0, 0);
        }


        //
//        // this works
//        int points = 50;
//        double radius = 0.5;
//        double interval = 2*Math.PI/points;
//        Location circleLocation = startLoc.clone();
//        for(int i = 0; i < points; i++) {
//            double t = i*interval;
//            double x = radius * Math.cos(t);
//            double y = radius * Math.sin(t);
//            double z = 0;
//            Vector v = new Vector(x,y,z);
//            v = MathUtil.rotateFunction(v, new Location(p.getWorld(), 0,0,0).setDirection(direction));
//            p.getWorld().spawnParticle(Particle.DRIP_LAVA, new Location(p.getWorld(), circleLocation.getX() + v.getX(), circleLocation.getY() + v.getY(), circleLocation.getZ() + v.getZ()), 0, 0, 0, 0);
//        }
        //
    }

    public static void spawnCircle(World w, Location circleLocation, Vector direction, double radius, int points) {
        double interval = 2*Math.PI/points;
        for(int i = 0; i < points; i++) {
            double t = i*interval;
            double x = radius * Math.cos(t);
            double y = radius * Math.sin(t);
            double z = 0;
            Vector v = new Vector(x,y,z);
            v = MathUtil.rotateFunction(v, new Location(w, 0,0,0).setDirection(direction));
            w.spawnParticle(Particle.END_ROD, new Location(w, circleLocation.getX() + v.getX(), circleLocation.getY() + v.getY(), circleLocation.getZ() + v.getZ()), 1, 0, 0.1, 0, 0);
        }
    }

    public static String colorize(String string) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        for (Matcher matcher = pattern.matcher(string); matcher.find(); matcher = pattern.matcher(string)) {
            String color = string.substring(matcher.start(), matcher.end());
            try {
                string = string.replace(color, ChatColor.of(color) + ""); // You're missing this replacing
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        string = ChatColor.translateAlternateColorCodes('&', string); // Translates any & codes too
        return string;
    }

    public static void debug(Player p, String message) {
        if(BoxPlugin.instance.getDebugEnabled().containsKey(p.getUniqueId()) && BoxPlugin.instance.getDebugEnabled().get(p.getUniqueId())) {
            p.sendMessage(ChatColor.GRAY + "[DEBUG] " + message);
        }
    }

    public static <T> T randomFromList(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()) % list.size());
    }

    public static boolean isInteger(String s) {
        int i;
        try {
            i = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String s) {
        double i;
        try {
            i = Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int getCurrencyAmount(ItemStack item) {
        int total = 0;
        if(isHexidium(item)) total += item.getAmount() * 262144;
        else if(isTeraCube(item)) total += item.getAmount() * 4096;
        else if(isGigaCoin(item)) total += item.getAmount() * 64;
        else if(isCoin(item)) total += item.getAmount();
        return total;
    }

    /**
     * check if a player has a certian amount of currency in inventory
     * @param amount amount of coins
     * @return if player has enough
     */
    public static boolean hasCurrency(Player p, int amount) {
        int total = 0;
        for(ItemStack item : p.getInventory().getContents()) {
            total += getCurrencyAmount(item);
        }
        return total >= amount;
    }

    /**
     * deduct a certain amount of currency from inventory
     * Will attempt to automatically convert currency if it's uneven
     * @param amount amount of coins
     * @return if it was successful
     */
    public static boolean deductCurrency(Player p, int amount) {
        if(!hasCurrency(p, amount)) return false;
        for(int i = 0; i < p.getInventory().getContents().length; i++) {
            if(isCurrency(p.getInventory().getContents()[i])) {
                int value = getCurrencyAmount(p.getInventory().getItem(i));
                if(value <= amount) {
                    p.getInventory().setItem(i, null);
                    amount -= value;
                } else {
                    ItemStack[] newItems = reducedCurrencyArray(value-amount);
                    p.getInventory().setItem(i, null);
                    p.getInventory().addItem(newItems);
                    break;
                }
            }
        }
        return true;
    }

    public static void hitThroughShield(Entity source, HumanEntity target, double damage, int cooldownticks) {
        if(target.isBlocking()) {
            target.setCooldown(Material.SHIELD, cooldownticks);
            EntityLiving nms = ((CraftHumanEntity) target).getHandle();
            // EntityLiving#clearActiveItem()  -  actually disables it
            nms.eZ();
        }
        target.damage(damage, source);
    }

    /**
     *
     * @param e The event
     * @param dropChancePerSlot drop chance per slot (0.5 is 50% chance to drop that item)
     */
    public static void dropPercent(PlayerDeathEvent e, double dropChancePerSlot) {
        Player target = e.getEntity();
        int outof = (int)(Math.round(1/dropChancePerSlot));
        Util.debug(target, "Losing " + (int)(dropChancePerSlot*100) + "% of items");
        e.setKeepInventory(true);
        e.getDrops().clear();
        for(int i = 0; i < e.getEntity().getInventory().getSize(); i++) {
            int rand = (int)(Math.random() * (outof) + 1);
            if(rand == 1) {
                if(e.getEntity().getInventory().getItem(i) != null) {
                    Util.debug(target, "Lost " + e.getEntity().getInventory().getItem(i).getType());
                    e.getDrops().add(e.getEntity().getInventory().getItem(i));
                    e.getEntity().getInventory().setItem(i, null);
                }
            }
        }
        try {
            ArrayList<ItemStack> armor = new ArrayList<>(List.of(e.getEntity().getInventory().getArmorContents()));
            for(int i = 0; i < e.getEntity().getInventory().getArmorContents().length; i++) {
                int rand = (int)(Math.random() * (outof) + 1);
                if(rand == 1) {
                    if(e.getEntity().getInventory().getArmorContents()[i] != null) {
                        Util.debug(target, "Lost " + e.getEntity().getInventory().getArmorContents()[i].getType());
                        e.getDrops().add(e.getEntity().getInventory().getArmorContents()[i]);
                        armor.set(i, null);
                    }
                }
            }
            e.getEntity().getInventory().setArmorContents(armor.toArray(new ItemStack[4]));
        } catch (NullPointerException ex) {
            // don't care if armor is null
        }
    }
    public static ItemStack getSkull(String url) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        if (url == null || url.isEmpty())
            return skull;
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
