package me.twostinkysocks.boxplugin.compressor;

import me.twostinkysocks.boxplugin.compressor.items.*;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Compressor {

    private static final ArrayList<Compressible> compressibleItems = new ArrayList<>();

    static {
        compressibleItems.add(new CompressibleDiamondBlock());
        compressibleItems.add(new CompressibleEmerald());
        compressibleItems.add(new CompressibleEmeraldBlock());
        compressibleItems.add(new CompressibleGoldBlock());
        compressibleItems.add(new CompressibleRedstoneBlock());
        compressibleItems.add(new CompressibleNetheriteBlock());
        compressibleItems.add(new CompressibleCopperBlock());
        compressibleItems.add(new CompressibleLapisBlock());
        compressibleItems.add(new CompressibleIronBlock());
        compressibleItems.add(new CompressibleQuartzBlock());
        compressibleItems.add(new CompressibleCoalBlock());

        compressibleItems.add(new CompressibleDirt());
        compressibleItems.add(new CompressibleStone());
        compressibleItems.add(new CompressibleWood());
        compressibleItems.add(new CompressibleCoal());
        compressibleItems.add(new CompressibleIron());
        compressibleItems.add(new CompressibleCopper());
        compressibleItems.add(new CompressibleGold());
        compressibleItems.add(new CompressibleLapis());
        compressibleItems.add(new CompressibleRedstone());
        compressibleItems.add(new CompressibleQuartz());
        compressibleItems.add(new CompressibleEmerald());
        compressibleItems.add(new CompressibleDiamond());
        compressibleItems.add(new CompressibleNetherite());
        compressibleItems.add(new CompressibleEndstone());
        compressibleItems.add(new CompressibleBone());
        compressibleItems.add(new CompressibleAmethyst());

        compressibleItems.add(new CompressibleEnderPearl());
    }

    /*
        dirt 64 - 1
        stone 64 - 1
        wood 64 - 1
        coal 32 - 1
        iron 32 - 1
        copper 32 - 1
        gold 32 - 1
        redstone 64 - 1
        lapis 64 - 1
        quartz 128 - 1
        emerald 32 - 1
        diamond 32 - 1
        netherite 16 - 1
        endstone 128 - 1
        bone - 128 - 1
     */

    /*
        diamond -> diamond block
     */

    public static boolean isCompressible(ItemStack item) {
        for(Compressible ci : compressibleItems) {
            if(ci.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public Compressible getCompressibleInstance(ItemStack item) {
        for(Compressible ci : compressibleItems) {
            if(ci.equals(item)) {
                return ci;
            }
        }
        return null;
    }


    public void compressAll(Player p) {
        LinkedHashMap<Compressible, Integer> compressibleBlocksInInventory = new LinkedHashMap<>();
        Util.debug(p, "Beginning item count step");
        for(ItemStack item : p.getInventory().getContents()) {
            if(isCompressible(item)) {
                Compressible compressible = getCompressibleInstance(item);
                if(compressibleBlocksInInventory.containsKey(compressible)) {
                    compressibleBlocksInInventory.put(compressible, compressibleBlocksInInventory.get(compressible) + item.getAmount());
                } else {
                    compressibleBlocksInInventory.put(compressible, item.getAmount());
                }
            }
        }

        Util.debug(p, "Found compressible items (in order):");
        for(Compressible c : compressibleBlocksInInventory.keySet()) {
            Util.debug(p, c.getClass().getSimpleName() + " - " + compressibleBlocksInInventory.get(c));
        }

        Util.debug(p, "Beginning compression step");
        // compressibleBlocksInInventory maps compressible block types to number of that item in inventory
        for(Compressible compressible : compressibleBlocksInInventory.keySet()) {
            int inputItems = compressibleBlocksInInventory.get(compressible); // input items in inventory
            int conversions = inputItems/compressible.getInput(); // conversion count
            int outputItems = conversions * compressible.getOutput(); // item count
            int requiredInput = conversions * compressible.getInput();
            int requiredInputStacks = requiredInput/64;
            int requiredInputRemainder = requiredInput - (requiredInputStacks*64);
            Util.debug(p, "For item " + compressible.getClass().getSimpleName() + " with count " + inputItems);
            Util.debug(p, "Can perform " + conversions + " compressions for this item");
            if(conversions <= 0) {
                Util.debug(p, "Skipping since 0 compressions...");
                continue;
            }
            Util.debug(p, "Total output items for this item: " + outputItems);
            Util.debug(p, "Required input items for this item: " + requiredInput);
            Util.debug(p, "This is " + requiredInputStacks + " stacks and " + requiredInputRemainder + " remainder.");
            Util.debug(p, "Removing items...");
            for(int i = 0; i < requiredInputStacks; i++) {
                removeItems(p.getInventory(), compressible, 64);
            }
            removeItems(p.getInventory(), compressible, requiredInputRemainder);
            Util.debug(p, "Adding " + outputItems + " compressed items for " + compressible.getClass().getSimpleName() + "\n");
            p.getInventory().addItem(compressible.getCompressedItemStack(outputItems));
        }
        Util.debug(p, "Finished compression step");
    }

    private void removeItems(Inventory inventory, Compressible compressible, int amount) {
        if (amount <= 0) return;
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is == null) continue;
            if (compressible.equals(is)) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }

}
