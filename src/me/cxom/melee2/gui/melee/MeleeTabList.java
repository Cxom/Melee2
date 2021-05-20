package me.cxom.melee2.gui.melee;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.cxom.melee2.game.rabbit.RabbitGameObserver;
import me.cxom.melee2.player.MeleePlayer;

public class MeleeTabList implements RabbitGameObserver {

	private Set<Player> players = new HashSet<>();
	
	public void addPlayer(MeleePlayer meleePlayer) {
		Player player = meleePlayer.getPlayer();
		players.add(player);
		updatePlayer(meleePlayer);
	}
	
	public void updatePlayer(MeleePlayer meleePlayer) {
		Player player = meleePlayer.getPlayer();
		player.setPlayerListName(meleePlayer.getColor().getChatColor() + player.getName() + " " + ChatColor.GRAY + meleePlayer.getKills());
	}
	
	public void removePlayer(Player player) {
		resetPlayerListName(player);
		players.remove(player);
	}
	
	public void removeAll() {
		players.forEach(this::resetPlayerListName);
		players.clear();
	}
	
	/* We don't save and restore what was in the tablist before this plugin
	 * because it's too hard to predict the way other plugins use the tablist
	 */
	private void resetPlayerListName(Player player) {
		player.setPlayerListName(player.getName());
	}
	
//	@Override
//	public void onGameReset(Collection<RabbitPlayer> rPlayers) {
//		
//	}
	
}
