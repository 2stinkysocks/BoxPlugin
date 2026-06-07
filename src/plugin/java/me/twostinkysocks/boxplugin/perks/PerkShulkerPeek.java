package me.twostinkysocks.boxplugin.perks;

import me.twostinkysocks.boxplugin.BoxPlugin;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PerkShulkerPeek extends AbstractPerk {
    public PerkShulkerPeek() {
        ItemStack guiItem = new ItemStack(Material.SHULKER_BOX);
        ItemMeta itemMeta = guiItem.getItemMeta();
        itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Shulker Peek");
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemMeta.setLore(List.of(
                "",
                ChatColor.GRAY + "Open shulkerboxes anywhere",
                ChatColor.GRAY + "like a backpack."
        ));
        guiItem.setItemMeta(itemMeta);

        setGuiItem(guiItem);

        setCost(36);

        setKey("perk_shulker");
    }

    @Override
    public void onEquip(Player p) {
        User user = BoxPlugin.instance.getLuckPerms().getUserManager().getUser(p.getUniqueId());
        InheritanceNode node = InheritanceNode.builder("shulkerpeek").value(true).build();
        user.data().add(node);
    }

    @Override
    public void onUnequip(Player p) {
        User user = BoxPlugin.instance.getLuckPerms().getUserManager().getUser(p.getUniqueId());
        InheritanceNode node = InheritanceNode.builder("shulkerpeek").value(true).build();
        user.data().remove(node);
    }
}
