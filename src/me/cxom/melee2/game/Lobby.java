package me.cxom.melee2.game;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.PlayerUtils;

public class Lobby {
	
	private GameInstance game;
	
	public Lobby(GameInstance game){
		this.game = game;
	}
	
	Set<Player> waitingPlayers = new HashSet<>();
	
	public void addPlayer(Player player){
		if (game.getGameState() == GameState.STOPPED){
			player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "This game has been stopped, you cannot join.");
			return;
		}
		waitingPlayers.add(player);
		PlayerProfile.save(player);
		player.teleport(game.getArena().getPregameLobby());
		player.setInvulnerable(true);
		player.setGameMode(GameMode.SURVIVAL);
		player.setFlying(false);
		PlayerUtils.perfectStats(player);
		player.getInventory().clear();
		if (game.getGameState() == GameState.WAITING && waitingPlayers.size() >= game.getArena().getPlayersToStart()){
			game.setGameState(GameState.STARTING);
			startCountdown();
		}
	}
	
	public void removePlayer(Player player){
		if (waitingPlayers.remove(player)){
			PlayerProfile.restore(player);
		}
	}
	
	public void removeAll(){
		for (Player player : waitingPlayers){
			PlayerProfile.restore(player);
		}
		waitingPlayers.clear();
	}
	
	public Set<Player> getWaitingPlayers(){
		return waitingPlayers;
	}
	
	public void startCountdown(){
		new BukkitRunnable(){
			int i = 10;
			@Override
			public void run(){
				if (i <= 0){
					this.cancel();
					if (game.getGameState() != GameState.STARTING || waitingPlayers.size() < game.getArena().getPlayersToStart()){
						for (Player player : waitingPlayers){
							player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "Not enough players, start aborted!");
						}
						game.setGameState(GameState.WAITING);
					} else {
						startNow();
					}
					return;
				}
				for (Player player : waitingPlayers){
					player.sendMessage(Melee.CHAT_PREFIX + ChatColor.GOLD + "Match starting in " + i + " second(s) on " + game.getArena().getName() + "!");
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .25f, .9f);
				}
				i--;
			}
		}.runTaskTimerAsynchronously(Melee.getPlugin(), 20, 20);
	}
	
	public void startNow(){
		game.start(waitingPlayers);
		waitingPlayers.clear();
	}
	
}
