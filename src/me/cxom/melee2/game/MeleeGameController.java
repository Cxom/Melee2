package me.cxom.melee2.game;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.player.MeleeColor;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.CirculatingList;

public class MeleeGameController {

	// Right now we're running on a Model-Controller-Fake "View" model, but we should consider
	// It more like a Publisher-Subscriber relationship, as this is how movement operates
	
	
	private final MeleeGame game;
	private final MeleeGUI gui;
	
	private final Lobby lobby;
	private final LobbyController lobbyController;
	
	private final MovementSystem movement;
	
	public MeleeGameController(MeleeArena arena) {
		
		//Create our model instance and our "view" (and movement)
		game = new MeleeGame(arena);
		gui = new MeleeGUI(game);
		
		lobby = new Lobby(game);
		lobbyController = new LobbyController(lobby, this::startGame, game);
		
		movement = MovementPlusPlus.CXOMS_MOVEMENT;
		
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
	
	/**
	 * Adds a player to the game's lobby
	 * @param player
	 */
	void addPlayerToLobby(Player player) {
		
		PlayerProfile.save(player);
		lobbyController.addPlayerToLobby(player);
		
	}
	
	
	public void stopGame(){
		
		gui.playStop();
		
		resetGame();
		lobbyController.removeAllFromLobby();
		
		game.setState(GameState.STOPPED);
		
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
	
		CirculatingList<MeleeColor> colors = new CirculatingList<>(MeleeColor.getDefaults(), true);
		
		for (Player player : startingPlayers){
			
			//Initialize MeleePlayer object
			MeleePlayer mp = new MeleePlayer(player, colors.next());
			
			//Update things
			game.addPlayer(mp);
			gui.addPlayer(mp);
			
			//Spawn player (before adding to movement to prevent conflicts with world settings)
			game.spawnPlayer(mp);
			
			//Add to movement
			movement.addPlayer(player);
			
			//Make 'em mortal
			player.setInvulnerable(false);
		}
		
		gui.playStart();
		
		game.setState(GameState.RUNNING);
		
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
		movement.removePlayer(player);
		
		// RESTORE STATS & LOCATION
		PlayerProfile.restore(player);
		
		//
		if (game.getPlayers().size() == 1){
			gui.broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "Too many people have left. Shutting down the game :/");
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
	
	// ------------------ HELPER CONTROL METHODS ----------------- //
	/*
	 *  - Postgame
	 *  - Reset
	 *  - Player Spawn
	 *  
	 */

	// Runs the end of the game
	private void runPostgame(MeleePlayer winner) {
		
		int duration = 10;
		
		game.setState(GameState.ENDING); 
		
		gui.playPostgame(winner, duration);
	
		new BukkitRunnable() {
			@Override
			public void run() {
				MeleeGameController.this.resetGame();
			}
		}.runTaskLater(Melee.getPlugin(), duration * 20);
		
	}
	
	private void resetGame(){
		
		//TODO CLEAN UP THIS METHOD
		
		//lobby.removeAll(); What was this doing here?????
		
		gui.reset();
		
		//movement.removeAll(); See below line (maybe change API)
		game.getPlayers().forEach(mp -> {
			movement.removePlayer(mp.getPlayer().getUniqueId());
			PlayerProfile.restore(mp.getPlayer());
		});
		
		game.getPlayers().clear();	
		game.setLeader(null);
		game.setState(GameState.WAITING);
	}

	// Melee Kills and Deaths
	void handleKill(MeleePlayer killer, MeleePlayer killed, EntityDamageByEntityEvent e){
			
		//if suicide, not a kill
		if (killer.equals(killed)){
			handleDeath(killed.getPlayer(), e);
			return;
		}
		
		//cancel damage
		e.setCancelled(true); //TODO Maybe refactor into cancelDamage method in MeleeKillEvent?
		
		killer.incrementKills();
		if (game.getLeader() == null || killer.getKills() > game.getLeader().getKills()){
			game.setLeader(killer);
		}
		
		//Play GUI before respawn
		gui.playKill(killer, killed, e);
		
		game.spawnPlayer(killed);
		
		
		
		if (killer.getKills() == game.getKillsToEnd()){
			runPostgame(killer);
		}
		
	}
	
	void handleDeath(Player killed, EntityDamageEvent e){
		e.setCancelled(true);
		//Let player take non-fatal damage that isn't caused by a fall or firework explosion
		if (e.getCause() != DamageCause.FALL && e.getCause() != DamageCause.ENTITY_EXPLOSION){
			((Player) e.getEntity()).setHealth(1);
		}
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
