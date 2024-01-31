package me.twostinkysocks.boxplugin.manager;

import com.google.common.collect.Maps;
import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.CustomItemsMain;
import me.twostinkysocks.boxplugin.util.ListDataType;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GhostTokenManager {

    // there has to be a better way but i think this works
    public enum Reclaimable {
        ENTERPRISE(64),
        XANATOS(100),
        LEAFY(148),
        STONE_GOLEM(250),
        MOLTEN_IRON(340),
        SPOOKY(680),
        HELLFIRE(980),
        OBSIDIAN(1200),
        SUPER_GOLDEN(1600),
        AXOLOTL(2200, "TASTY_SNACK"),
        AXOLOTL_SAMURAI(2750, "PUFFERMACE"),
        MODERN(3450, "FUTURISTIC_DEVICE"),
        SPEED(3950),
        GLASS_CANNON(4250),
        TANK(4250),
        SHINY_DIAMOND(10000),
        CHEF(13800, "LET_ME_COOK"),
        GUARDIAN(18600, "OCEAN_GLASS"),
        EMERALD(26000),
        ANUBIS(34000),
        ZEUS(50000),
        DRAGON(107000, "FIREBALL"),
        SUPREME(850000, "XANATOS_STAR"),
        NEPTUNE(30000),
        CANNON(5000),
        SCYTHE(17000),
        SOUL_SMITE(32000),
        BANE_OF_THE_DEAD(2000),
        FIRE_FLOWER(1800),
        PACK(4000),
        XANATOASTER(500),
        SCEPTER(1200),
        VOID_STAFF(9000),
        GOD_SLAYER(100000),
        JAVELIN(999999),
        CAGE(999999),
        GIGA(2300),
        BIGGER(5000),
        DRILL(9000),
        EXCAVATOR(27000),
        CROSSBOW(800),
        DREADNOUGHT(7500),
        RIPTIDE(3400),
        PLUTONIUM(110000),
        EXOTIC_PICKAXE(17000),
        EXOTIC_BOW(870000);

        public final int cost;
        public final String altKey;

        private static final Map<List<String>, Reclaimable> BY_NAME = Maps.newHashMap();

        private Reclaimable(int cost) {
            this.cost = cost;
            this.altKey = null;
        }
        private Reclaimable(int cost, String altKey) {
            this.cost = cost;
            this.altKey = altKey;
        }

        /**
         *
         * @return a list of strings that items of this type can have in their custom names
         */
        public List<String> getPossibleNames() {
            if(this.altKey != null) {
                return List.of(this.name().toLowerCase().replaceAll("_", " "), this.altKey.toLowerCase().replaceAll("_", " "));
            } else {
                return List.of(this.name().toLowerCase().replaceAll("_", " "));
            }
        }

        /**
         *
         * @param name The name of the item being searched
         * @return the highest priced reclaimable that matches the name entered
         */
        @Nullable
        public static Reclaimable getByName(String name) {
            int highest = 0;
            Reclaimable reclaimable = null;
            for(List<String> names : BY_NAME.keySet()) {
                for(String possibleName : names) {
                    if(ChatColor.stripColor(name).toLowerCase().contains(possibleName)) {
                        if(BY_NAME.get(names).cost > highest) {
                            highest = BY_NAME.get(names).cost;
                            reclaimable = BY_NAME.get(names);
                        }
                    }
                }
            }
            return reclaimable;
        }

        static {
            for (Reclaimable reclaimable : values()) {
                BY_NAME.put(reclaimable.getPossibleNames(), reclaimable);
            }
        }
    }

    private static final NamespacedKey reclaimablesKey = new NamespacedKey(BoxPlugin.instance, "reclaimables");


    private void storeReclaimablesInPDC(Player p, List<ItemStack> items) {
        p.getPersistentDataContainer().set(reclaimablesKey, new ListDataType(), items);
    }

    public void onDeath(List<ItemStack> drops, Player p) {
        List<ItemStack> reclaimables = new ArrayList<>();
        for(ItemStack item : drops) {
            if(item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && Reclaimable.getByName(item.getItemMeta().getDisplayName()) != null) {
                reclaimables.add(item.clone()); // not sure if this needs deep copy but I'll be safe
            }
        }
        storeReclaimablesInPDC(p, reclaimables);
    }

    public void giveGhostToken(Player p) {
        CustomItemsMain.instance.getItem("")
    }
}
