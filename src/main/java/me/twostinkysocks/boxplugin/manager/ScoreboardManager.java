package me.twostinkysocks.boxplugin.manager;

import me.twostinkysocks.boxplugin.BoxPlugin;
import me.twostinkysocks.boxplugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScoreboardManager {

    private ArrayList<Player> queuedScoreboardUpdates;

    public ScoreboardManager() {
        queuedScoreboardUpdates = new ArrayList<>();
        Bukkit.getScheduler().runTaskTimer(BoxPlugin.instance, () -> {
            for(Player p : BoxPlugin.instance.getScoreboardManager().getQueuedUpdates()) {
                if(p.isValid() && p.isOnline()) {
                    updatePlayerScoreboard(p);
                }
            }
            BoxPlugin.instance.getScoreboardManager().clearQueuedUpdates();
        }, 20L, 20L);
    }

    public void queueUpdate(Player p) {
        if(!queuedScoreboardUpdates.contains(p)) {
            queuedScoreboardUpdates.add(p);
        }
    }

    public ArrayList<Player> getQueuedUpdates() {
        return queuedScoreboardUpdates;
    }

    public void clearQueuedUpdates() {
        queuedScoreboardUpdates = new ArrayList<>();
    }


    public void updatePlayerScoreboard(Player p) {
        List<String> list = Objects.requireNonNull(BoxPlugin.instance.getConfig().getList("scoreboard")).stream().map(s -> Util.colorize((String) s)).collect(Collectors.toList());

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard scoreboard = manager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("scoreboard", "dummy", list.remove(0));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for(int i = 0; i < list.size(); i++) {
            String emptyName = new String(new char[i+1]).replace("\0", "§r");
            Team team = scoreboard.registerNewTeam(emptyName);
            String text = Util.colorize(list.get(i).replaceAll("%level%", "" + BoxPlugin.instance.getXpManager().getLevel(p)).replaceAll("%needed-xp%","" + BoxPlugin.instance.getXpManager().getNeededXp(p)).replaceAll("%streak%", "" + BoxPlugin.instance.getPvpManager().getStreak(p)).replaceAll("%bounty%", "" + BoxPlugin.instance.getPvpManager().getBounty(p))).replaceAll("%rubies%", "" + BoxPlugin.instance.getMarketManager().getRubies(p)).replaceAll("%coins%", "" + BoxPlugin.instance.getMarketManager().getCoinsBalance(p)).replaceAll("%gearscore%", "" + GearScoreManager.GetPlayerGearscore(p));
            team.setSuffix(text);
            team.addEntry(emptyName);
            objective.getScore(emptyName).setScore(list.size()-i);
        }
        p.setScoreboard(scoreboard);
    }
}
