package net.punchtree.melee.gui.rabbit;

import java.util.HashSet;
import java.util.Set;

import net.punchtree.melee.player.MeleePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.punchtree.melee.game.rabbit.RabbitGame;

public class RabbitTabList {

private Set<Player> players = new HashSet<>();
	
	private final RabbitGame game;
	
	public RabbitTabList(RabbitGame game) {
		this.game = game;
	}
	
	public void addPlayer(MeleePlayer meleePlayer) {
		Player player = meleePlayer.getPlayer();
		players.add(player);
		updatePlayer(meleePlayer);
	}
	
	public void updatePlayer(MeleePlayer meleePlayer) {
		Player player = meleePlayer.getPlayer();
		player.setPlayerListName(meleePlayer.getColor().getChatColor() + player.getName() + " " + ChatColor.GRAY + meleePlayer.getKills());
		if (game != null && game.getFlagLeader() != null) {			
			// TODO fix the magic number 4 at the end
			player.setPlayerListFooter(game.getTimeToWinInSeconds() + " seconds to win\n" + game.getFlagLeader().getColoredName() + " leads with " + game.getFlagLeader().getFlagCounter() / 4 + "seconds!");
		}
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
		player.setPlayerListHeaderFooter("", "");
	}
	
//	@Override
//	public void onGameReset(Collection<RabbitPlayer> rPlayers) {
//		
//	}
	
}
