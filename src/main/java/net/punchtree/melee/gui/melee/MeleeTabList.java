package net.punchtree.melee.gui.melee;

import java.util.HashSet;
import java.util.Set;

import net.punchtree.melee.game.melee.MeleeGame;
import net.punchtree.melee.game.rabbit.RabbitGameObserver;
import net.punchtree.melee.player.MeleePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MeleeTabList implements RabbitGameObserver {

	private Set<Player> players = new HashSet<>();
	
	private final MeleeGame game;
	
	public MeleeTabList(MeleeGame game) {
		this.game = game;
	}
	
	public void addPlayer(MeleePlayer meleePlayer) {
		Player player = meleePlayer.getPlayer();
		players.add(player);
		sendToPlayer(meleePlayer);
	}

	public void removePlayer(Player player) {
		resetPlayerListName(player);
		players.remove(player);
	}

	public void addKill(MeleePlayer killer) {
		game.getPlayers().forEach(this::sendToPlayer);
	}

	private void sendToPlayer(MeleePlayer meleePlayer) {
		Player player = meleePlayer.getPlayer();
		player.setPlayerListName(meleePlayer.getColor().getChatColor() + player.getName() + " " + ChatColor.GRAY + meleePlayer.getKills());
		if (game != null && game.getLeader() != null) {
			// TODO create a state manager that tracks tier/leader (like how bossbar calculation is made in MeleeGUI) \
			// and use that string to update this
			// or just remove this since it's redundant with the bossbar
			player.setPlayerListFooter(game.getKillsNeededToWin() + " kills to win\n" + game.getLeader().getColoredName() + " leads with " + game.getLeader().getKills());
		}
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
