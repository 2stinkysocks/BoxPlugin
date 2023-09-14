package me.twostinkysocks.boxplugin.util;

import me.twostinkysocks.boxplugin.BoxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MarketJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("Updating market...");
        Bukkit.getScheduler().runTaskLater(BoxPlugin.instance, () -> { // no async bugs
            double newmult = BoxPlugin.instance.getMarketManager().randomizeMarketMultiplier();
            if(newmult >= 1.05) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "The market reached a new high! Your deposit is now worth " + newmult + "x!");
            }
        }, 1L);
    }
}
