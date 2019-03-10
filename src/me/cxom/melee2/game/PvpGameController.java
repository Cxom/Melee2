package me.cxom.melee2.game;

import java.util.Set;

import org.bukkit.entity.Player;

import me.cxom.melee2.common.model.GameState;
import me.cxom.melee2.game.lobby.Lobby;
import me.cxom.melee2.util.PlayerProfile;

public abstract class PvpGameController {
	
	
	// The controller knows the model, so this is *probably* ok since
	//  the only thing that should know about the controller is other controllers
	//  Namely, the MeleeGameManager
	public abstract PvpGame getGame();
	
	public abstract Lobby getLobby();
	
	// --------------- EXTERNAL CONTROL METHODS ------------------------- //
	/*
	 *  These methods are the only things handled by direct player interaction
	 *  
	 *  Everything else is event driven, routed through the MeleeEventListeners class if they
		  deal with more than just strict domain objects
	 */
	
	/**
	 * Adds a player to the game's lobby
	 * @param player
	 */
	public void addPlayerToLobby(Player player) {
	
		getLobby().addPlayerIfPossible(player);
		
	}
	
	
	public void stopGame(){
		
		gui.playStop();
		
		resetGame();
		lobbyController.removeAllFromLobby();
		
		game.setGameState(GameState.STOPPED);
		
	}
	
	// ----------- INTERNAL (LISTENER) CONTROL METHODS ------------------ //
	/*
	 * - Start
	 * - Remove player
	 * - Stop
	 * 
	 */
	
	
	/**
	 * Starts a new game with a set of starting players.
	 * 
	 * This should only be called by the lobby controller
	 * @param startingPlayers
	 */
	void startGame(Set<Player> startingPlayers) {
		game.startGame(startingPlayers);
		
		gui.addPlayers(game.getPlayers());
		
		gui.playStart();
	}
	
	/*
	 *  The remove methods are internal because quitting the server is handled by events
	 *   and /melee leave is handled by a preprocess event
	 *   
	 */
	
	boolean removePlayerFromGame(Player player) {
		
		//Is the remove request valid?
		if (!game.hasPlayer(player.getUniqueId())) return false;
		
		//Remove
		game.removeAndRestorePlayer(player);
		gui.removeAndRestorePlayer(player);
		
		// RESTORE STATS & LOCATION
		PlayerProfile.restore(player);
		
		// End the game if it gets down to one left.
		if (game.getPlayers().size() == 1){
			gui.playTooManyLeft();
			resetGame();
		}
	
		return true;
	}
	
	boolean removePlayerFromLobby(Player player) {
		
		//Is the remove request valid?
		if (!lobby.hasPlayer(player)) return false;
		
		lobbyController.removePlayerFromLobby(player);
		return true;
		
	}	
	
	abstract void resetGame();
	
//		private void onGameWin(MeleePlayer winner){
//			
//		}

	
}
