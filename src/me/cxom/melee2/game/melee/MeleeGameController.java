package me.cxom.melee2.game.melee;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.trinoxtion.movement.MovementPlusPlus;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.common.model.GameState;
import me.cxom.melee2.game.lobby.Lobby;
import me.cxom.melee2.gui.melee.MeleeGUI;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.util.PlayerProfile;

/**
 * 
 * Controls a MeleeGame model and an associated GUI. Its methods are 
 * called by the series of events in MeleeEventListeners.
 * 
 */
public class MeleeGameController /*extends PvpGameController */ {
	
	/*
	 * List of things that can probably be moved into the model:
	 * 
	 *  MovementSystem and adding and removing
	 *  PlayerProfile is more of a Util class than part of the Player part of the model
	 *  GameStart code should be moved. The only point of this class is to handle things not encompassed by the model
	 *   Currently, this includes
	 *    - Player inventory, xp, etc, saving and loading
	 *    - Delegation to Model and GUI
	 *    Notably, the first one would not be necessary in a minigame server environment
	 */
	
	private final MeleeGame game;
	private final MeleeGUI gui;
	
	private final Lobby lobby;
	
	public MeleeGameController(MeleeArena arena) {
		
		//Create our model instance and our "view" (and movement)
		game = new MeleeGame(arena, MovementPlusPlus.CXOMS_MOVEMENT);
		gui = new MeleeGUI(game);
		
		lobby = new Lobby(game, this::startGame);
		
		//The game is event driven, so we need listeners to call this controller:
		new MeleeEventListeners(this, game);
		
	}
	
	
	// The controller knows the model, so this is *probably* ok since
	//  the only thing that should know about the controller is other controllers
	//  Namely, the MeleeGameManager
	public MeleeGame getGame() {
		return game;
	}
	
	public Lobby getLobby() {
		return lobby;
	}
	
	// --------------- EXTERNAL CONTROL METHODS ------------------------- //
	/*
	 *  These methods are the only things handled by direct player interaction
	 *  
	 *  Everything else is event driven, routed through the MeleeEventListeners class if they
		  deal with more than just strict domain objects
	 */
	
	public void addPlayerToLobby(Player player) {
		lobby.addPlayerIfPossible(player);
	}
	
	public void stopGame(){
		
		gui.playStop();
		
		resetGame();
		lobby.removeAndRestoreAll();
		
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
		game.removePlayer(player);
		gui.removePlayer(player);
		
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
		if (lobby.hasPlayer(player)) { 		
			lobby.removeAndRestorePlayer(player);
			return true;
		}
		return false;
	}	
	
	// ------------------ HELPER CONTROL METHODS ----------------- //
	/*
	 *  - Postgame
	 *  - Reset
	 *  
	 */
	
	void resetGame(){
		
		// Reset the GUI first because it needs to reference the player set for the tablist.
		// I think this is misguided since resetting the GUI shouldn't depend on the changed 
		// state of the game in order to undo itself, so TODO make an encapsulated tablist GUI component
		// so that order no longer matters here (also so if we want any reflection of the blank state of the game in the gui)
		gui.reset();
		
		game.getPlayers().forEach(mp -> {
			PlayerProfile.restore(mp.getPlayer());
		});
		
		game.resetGame();
	}
	
	
	void runPostgame(MeleePlayer winner) {
		gui.playPostgame(winner, MeleeGame.POSTGAME_DURATION_SECONDS);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				MeleeGameController.this.resetGame();
			}
		}.runTaskLater(Melee.getPlugin(), MeleeGame.POSTGAME_DURATION_SECONDS * 20);
	}
	
	
	void propagateKill(Player killer, Player killed, EntityDamageByEntityEvent edbee) {
		
		if (game.getGameState() != GameState.RUNNING) return;
		
		MeleePlayer mpKiller = game.getPlayer(killer.getUniqueId());
		MeleePlayer mpKilled = game.getPlayer(killed.getUniqueId());
		Location killLocation = killed.getLocation(); // GUI is updated after model, but the game will respawn the player, so we save the kill location
		game.handleKill(mpKiller, mpKilled, edbee);
		gui.playKill(mpKiller, mpKilled, edbee, killLocation);
		
		if (game.hasWinner()) { // This needs to only trigger once
			this.runPostgame(mpKiller);
		};
	}
	
	void propagateDeath(Player killed, EntityDamageEvent e) {
		MeleePlayer mpKilled = game.getPlayer(killed.getUniqueId());
		Location deathLocation = killed.getLocation();
		game.handleDeath(mpKilled, e);
		gui.playDeath(mpKilled, e, deathLocation);
	}

	//---- DEBUG METHODS ----//
	
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
