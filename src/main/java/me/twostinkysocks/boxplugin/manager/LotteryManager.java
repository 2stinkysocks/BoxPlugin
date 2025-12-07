package me.twostinkysocks.boxplugin.manager;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentcrates.key.CrateKey;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class LotteryManager {

    private HashMap<UUID, Integer> purchasedTickets;

    private long nextEndTimestamp;

    public final NamespacedKey inLotteryKey = new NamespacedKey(BoxPlugin.instance, "inLottery");

    public LotteryManager() {
        purchasedTickets = new HashMap<>();
        nextEndTimestamp = -1;
    }

    public int getTicketPrice() {
        if(purchasedTickets.size() == 0) {
            return 8000;
        } else {
            int players = purchasedTickets.size();
            return (int) (8000.0/((0.5*players)+1));
        }
    }

    public boolean hasTickets(Player p) {
        return purchasedTickets.containsKey(p.getUniqueId());
    }

    public void onLotteryEnd() {
        Bukkit.broadcastMessage(ChatColor.GREEN + "Rolling lottery winner...");
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
            HashMap<UUID, Integer> onlineTicketHolders = new HashMap<>();
            purchasedTickets.forEach((u, i) -> {
                if(Bukkit.getPlayer(u) != null) {
                    onlineTicketHolders.put(u, i);
                }
            });
            purchasedTickets = onlineTicketHolders;
            int finalPrice = getTicketPrice();
            for(UUID uuid : purchasedTickets.keySet()) {
                Player p = Bukkit.getPlayer(uuid);
                BoxPlugin.instance.getMarketManager().removeCoinsBalance(p, finalPrice);
                BoxPlugin.instance.getScoreboardManager().queueUpdate(p);
                p.sendMessage(ChatColor.GREEN + "You paid " + finalPrice + " for your lottery ticket.");
                p.getPersistentDataContainer().remove(inLotteryKey);
            }
            Player winner = Bukkit.getPlayer(Util.randomFromList(purchasedTickets.keySet().stream().toList()));
            if(winner != null) {
                CrateKey key = BoxPlugin.instance.getKeyManager().getKeyById("lottery");
                BoxPlugin.instance.getKeyManager().giveKey(winner, key, 1);
                Bukkit.broadcastMessage(ChatColor.GREEN + winner.getName() + " wins the lottery!");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "The lottery ran into an error.");
            }
            purchasedTickets = new HashMap<>();
        }, 20*2);
    }

    public long getRemainingTimeMs() {
        long diff = nextEndTimestamp - System.currentTimeMillis();
        if(diff < 0) return 0;
        return diff;
    }

    /**
     * Adds pdc to prevent withdraws, and adds ticket to player
     * @param p The player buying the ticket
     * @return if it was successfully purchased
     */
    public boolean buyTicket(Player p) {
        int bal = BoxPlugin.instance.getMarketManager().getCoinsBalance(p);
        int ticketPrice = getTicketPrice();
        if(p.getPersistentDataContainer().has(inLotteryKey, PersistentDataType.INTEGER)) return false;
        if(bal < ticketPrice) return false;
        p.getPersistentDataContainer().set(inLotteryKey, PersistentDataType.INTEGER, 1);
        purchasedTickets.put(p.getUniqueId(), 1);
        if(ticketPrice == 8000) {
            nextEndTimestamp = System.currentTimeMillis() + (1000*60*15);
            Bukkit.broadcastMessage(ChatColor.GREEN + "The lottery just started! It will end in 15 minutes, buy tickets at the Banker in spawn!");
            Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                Bukkit.broadcastMessage(ChatColor.GREEN + "The lottery will end in 10 minutes, buy tickets at the Banker in spawn! Current price: " + getTicketPrice());
            }, 20*60*5);
            Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                Bukkit.broadcastMessage(ChatColor.GREEN + "The lottery will end in 5 minutes, buy tickets at the Banker in spawn! Current price: " + getTicketPrice());
            }, 20*60*10);
            Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> {
                Bukkit.broadcastMessage(ChatColor.GREEN + "The lottery will end in 1 minute, buy tickets at the Banker in spawn! Current price: " + getTicketPrice());
            }, 20*60*14);
            Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, this::onLotteryEnd, 20*60*15);
        }
        return true;
    }
}
