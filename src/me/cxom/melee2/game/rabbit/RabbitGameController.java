package me.cxom.melee2.game.rabbit;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.trinoxtion.movement.MovementPlusPlus;

import me.cxom.melee2.arena.RabbitArena;
import me.cxom.melee2.common.model.GameState;
import me.cxom.melee2.game.lobby.Lobby;
import me.cxom.melee2.gui.rabbit.RabbitGUI;
import net.punchtree.minigames.utility.player.PlayerProfile;

public class RabbitGameController /*extends PvpGameController */implements RabbitGameObserver {
	
	private final RabbitGame game;
	private final RabbitGUI gui;
	
	private final Lobby lobby;
	
	public RabbitGameController(RabbitArena arena) {
		// Initialize game, gui, lobby, and event listeners
		game = new RabbitGame(arena, MovementPlusPlus.CXOMS_MOVEMENT);
		gui = new RabbitGUI(game);
		lobby = new Lobby(game, this::startGame);
		new RabbitEventListeners(this, game, gui);
	}
	
	public RabbitGame getGame() {
		return game;
	}
	
	public Lobby getLobby() {
		return lobby;
	}
	
	/*
	 * Methods in this class should be restricted to only tasks
	 * dealing outside the scope of regular game objects
	 */

	public void stopGame(){
		
		gui.playStop();
		
		game.resetGame();
		lobby.removeAndRestoreAll();
		
		game.setState(GameState.STOPPED);
		
	}
	
	void startGame(Set<Player> startingPlayers) {
		game.startGame(startingPlayers);
		
		gui.addPlayers(game.getPlayers());
		
		gui.playStart();
	}
	
	boolean removePlayerFromGame(Player player) {
		
		//Is the remove request valid?
		if (!game.hasPlayer(player.getUniqueId())) return false;
		
		//Remove
		game.removePlayer(player);
		gui.removePlayer(player);
		
		// RESTORE STATS & LOCATION
		PlayerProfile.restore(player);
		
		// End the game if it gets down to one left.
		if (game.getPlayers().size() == 1){
			gui.playTooManyLeft();
			game.resetGame();
//			resetGame();
		}
	
		return true;
	}
	
	boolean removePlayerFromLobby(Player player) {
		
		//Is the remove request valid?
		if (!lobby.hasPlayer(player)) return false;
		
		lobby.removeAndRestorePlayer(player);
		return true;
		
	}
	
	public void debug(Player player) {
		player.sendMessage("Game Players: " + game.getPlayers().stream()
													  .map(mp -> mp.getPlayer().getName())
													  .collect(Collectors.toList()));
		player.sendMessage("Game State: " + game.getGameState());
		player.sendMessage("Lobby players: " + lobby.getPlayers().stream()
													  .map(Player::getName)
													  .collect(Collectors.toList()));
	}

}