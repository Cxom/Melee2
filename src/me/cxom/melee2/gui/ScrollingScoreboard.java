package me.cxom.melee2.gui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import me.cxom.melee2.Melee;

public class ScrollingScoreboard {

	private Set<UUID> players = new HashSet<>();
	private Queue<String> activeMessages = new LinkedList<>();
	
	private final Scoreboard scoreboard;
	private Objective sidebar;
	
	public ScrollingScoreboard(String title){
		scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		sidebar = scoreboard.registerNewObjective("scroller", "dummy");
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		sidebar.setDisplayName(title);
	}

	public void sendMessage(final String newMessage){
		if (activeMessages.contains(newMessage)){
			this.sendMessage("*" + newMessage);
			return;
		}
		
		Iterator<String> itr = activeMessages.iterator();
		while(itr.hasNext()){
			String message = itr.next();
			Score score = sidebar.getScore(message);
			if(score.getScore() - 1 <= 0){
				itr.remove();
				clearMessage(message);
			}else{
				score.setScore(score.getScore() - 1);
			}
		}
		final Score scroller = sidebar.getScore(ChatColor.RED + newMessage);
		scroller.setScore(10);
		activeMessages.offer(newMessage);
		new BukkitRunnable(){
			public void run(){
				if(activeMessages.contains(newMessage)){
					activeMessages.remove(newMessage);	
					clearMessage(newMessage);
				}
			}
		}.runTaskLater(Melee.getPlugin(), 120);
	}
	
	private void clearMessage(String msg){
		scoreboard.resetScores(ChatColor.RED + msg);
		for(UUID uuid : players){
			Bukkit.getPlayer(uuid).setScoreboard(scoreboard);
		}
	}
	
	public void addPlayer(Player player){
		players.add(player.getUniqueId());
		player.setScoreboard(scoreboard);
	}
	
	public void removePlayer(Player player){
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		players.remove(player.getUniqueId());
	}
	
	public void removeAll(){
		for (UUID uuid : players){
			removePlayer(Bukkit.getPlayer(uuid));
		}
		players.clear();
	}
	
	
}
