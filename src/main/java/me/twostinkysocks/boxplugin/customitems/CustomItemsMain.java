package me.twostinkysocks.boxplugin.customitems;


import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.customitems.items.CustomItem;
import me.twostinkysocks.boxplugin.customitems.items.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CustomItemsMain implements CommandExecutor, TabCompleter {

    private ArrayList<CustomItem> items;

    public static CustomItemsMain instance;

    public CustomItem getItem(String key) {
        for(CustomItem item : items) {
            if(item.getItemId().equals(key)) {
                return item;
            }
        }
        return null;
    }

    public void onEnable() {
        items = new ArrayList<>();
        BoxPlugin.instance.getCommand("cgive").setExecutor(this);
        BoxPlugin.instance.getCommand("cgive").setTabCompleter(this);
        BoxPlugin.instance.getCommand("setcustomitemid").setExecutor(this);
        BoxPlugin.instance.getCommand("setcustomitemid").setTabCompleter(this);
        BoxPlugin.instance.getServer().getPluginManager().registerEvents(new Listeners(items), BoxPlugin.instance);

        registerItem(new SexShovel(this));
        registerItem(new SuperSexShovel(this));
        registerItem(new WitherSkullSword(this));
        registerItem(new SpaceHelmet(this));
        registerItem(new AxeOfTheShredded(this));
        registerItem(new AugmentedRailgun(this));
        registerItem(new PulseBow(this));
        registerItem(new Tutorial(this));
        registerItem(new TalismanOfEnergy(this));
        registerItem(new ClusterGrenade(this));
        registerItem(new HealSpear(this));
        registerItem(new Hyperion(this));
        registerItem(new MilkPotion(this));
        registerItem(new ThrowableLava(this));
        registerItem(new WolfPack(this));
        registerItem(new CageStaff(this));
        registerItem(new GhostToken(this));
        registerItem(new DragonEgg(this));
        registerItem(new SuperDragonEgg(this));
        BoxPlugin.instance.getLogger().info("Loaded custom items!");
        instance = this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandSender p = sender;
            if(command.getName().equals("cgive")) {
                if(!sender.isOp() && !p.hasPermission("customitems.give")) {
                    p.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return true;
                }
                if(args.length == 0) {
                    p.sendMessage(ChatColor.RED + "You need to enter an item to give!");
                    return true;
                }
                boolean foundItem = false;
                for(CustomItem i : items) {
                    if(args[0].equals(i.getItemId())) {
                        foundItem = true;
                        if(args.length == 1) {
                            if(p instanceof Player) {
                                ((Player) p).getInventory().addItem(i.getItemStack());
                                p.sendMessage(ChatColor.GREEN + "Gave 1x " + i.getItemId());
                            }
                        } else {
                            if(Bukkit.getPlayer(args[1]) == null) {
                                p.sendMessage(ChatColor.RED + "Invalid player!");
                                return true;
                            }
                            Player togive = Bukkit.getPlayer(args[1]);
                            HashMap<Integer, ItemStack> toDrop = togive.getInventory().addItem(i.getItemStack());
                            toDrop.forEach((index, item) -> {
                                Item entity = (Item) togive.getWorld().spawnEntity(togive.getLocation(), EntityType.ITEM);
                                entity.setItemStack(i.getItemStack());
                            });
                            p.sendMessage(ChatColor.GREEN + "Gave " + togive.getName() + " 1x " + i.getItemId());
                        }
                    }
                }
                if(!foundItem) {
                    p.sendMessage(ChatColor.RED + "That item doesn't exist!");
                }
            } else if(command.getName().equals("setcustomitemid")) {
                if(!p.hasPermission("customitems.give") || !(p instanceof Player)) {
                    p.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return true;
                }
                if(args.length == 0) {
                    p.sendMessage(ChatColor.RED + "Usage: /setcustomitemid <id>");
                    return true;
                }
                Player player = (Player) p;
                if(player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta meta = item.getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(BoxPlugin.instance, "ITEM_ID"), PersistentDataType.STRING, args[0]);
                    item.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(item);
                    p.sendMessage(ChatColor.GREEN + "ID set!");
                }
            }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender instanceof Player) {
            if(cmd.getName().equals("cgive")) {
                if(args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], items.stream().map(i -> i.getItemId()).collect(Collectors.toList()), completions);
                    return completions;
                } else if(args.length == 2) {
                    StringUtil.copyPartialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList()), completions);
                }
            } else if(cmd.getName().equals("setcustomitemid")) {
                if(args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], items.stream().map(i -> i.getItemId()).collect(Collectors.toList()), completions);
                    return completions;
                }
            }
        }
        return completions;
    }

    public void registerItem(CustomItem i) {
        items.add(i);
    }


}
