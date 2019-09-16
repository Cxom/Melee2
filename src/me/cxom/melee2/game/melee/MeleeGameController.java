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
import me.cxom.melee2.gui.melee.MeleeGUI;
import me.cxom.melee2.player.MeleePlayer;
import net.punchtree.minigames.game.GameState;
import net.punchtree.minigames.lobby.Lobby;
import net.punchtree.minigames.utility.player.PlayerProfile;

/**
 * Controls a MeleeGame model and an associated GUI. Its methods are 
 * called by the series of events in MeleeEventListeners.
 */
public class MeleeGameController /*extends PvpGameController */ {
	
	private final MeleeGame game;
	private final MeleeGUI gui;
	
	private final Lobby lobby;
	
	public MeleeGameController(MeleeArena arena) {
		game = new MeleeGame(arena, MovementPlusPlus.CXOMS_MOVEMENT);
		gui = new MeleeGUI(game);
		lobby = new Lobby(game, this::startGame, Melee.MELEE_CHAT_PREFIX);
		new MeleeEventListeners(this, game);
	}

	public MeleeGame getGame() {
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
		
		resetGame();
		lobby.removeAndRestoreAll();
		
		game.setGameState(GameState.STOPPED);
	}
	
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
	
	boolean removePlayerFromGame(Player player) {
		
		//Is the remove request valid?
		if (!game.hasPlayer(player.getUniqueId())) return false;
		
		//Remove
		game.removePlayer(player);
		gui.removePlayer(player);
		
		// RESTORE STATS & LOCATION
		PlayerProfile.restore(player);
		
		// End the game if it gets down to one player left.
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
	
	public void debug(Player player) {
		player.sendMessage("Game Players: " + game.getPlayers().stream()
													  .map(mp -> mp.getPlayer().getName())
													  .collect(Collectors.toList()));
		player.sendMessage("Game State: " + game.getGameState());
		player.sendMessage("Lobby players: " + lobby.getPlayers().stream()
													  .map(Player::getName)
													  .collect(Collectors.toList()));
	}
	
	// TODO the following methods can be refactored into the model/event listeners
	// See the way RabbitGameController accomplishes this with observers.
	
	void resetGame(){
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
	
}
