package me.cxom.melee2.game.melee;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import me.cxom.melee2.game.MeleeLikeEventListeners;
import me.cxom.melee2.game.MeleeLikeGame;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.gui.melee.MeleeGUI;
import me.cxom.melee2.player.MeleePlayer;
import net.punchtree.minigames.game.GameState;
import net.punchtree.minigames.utility.collections.CirculatingList;
import net.punchtree.minigames.utility.player.PlayerUtils;
import net.punchtree.util.color.PunchTreeColor;

/**
 * Represents an instance of a Melee game on one arena. 
 * The game object is persistent across games
 * @author Cxom
 */
public class MeleeGame implements MeleeLikeGame {
	
	// Class constants
	public static final int POSTGAME_DURATION_SECONDS = 10;
	
	// Persistent properties
	private final MeleeArena arena; 
	private final CirculatingList<Location> spawns;
	private final MeleeGUI gui;
	private final MovementSystem movement = MovementPlusPlus.CXOMS_MOVEMENT;
	
	// State fields
	private GameState gamestate = GameState.WAITING;
	private Map<UUID, MeleePlayer> players = new HashMap<>();
	private MeleePlayer leader = null;
	
	private Consumer<Player> onPlayerLeaveGame;
	
	public MeleeGame(MeleeArena arena){
		this.arena = arena;
		this.spawns = new CirculatingList<Location>(arena.getSpawns(), true);
		gui = new MeleeGUI(this);
		new MeleeLikeEventListeners(this);
	}

	// -------------------------- //
	//-------- GETTERS ---------- //
	// -------------------------- //
	@Override
	public MeleeArena getArena(){
		return arena;
	}

	@Override
	public GameState getGameState(){
		return gamestate; 
	}

	@Override
	public String getName() {
		return "Melee";
	}

	public int getKillsNeededToWin() {
		return arena.getKillsToEnd();
	}

	public MeleePlayer getLeader() {
		return leader;
	}

	public MeleePlayer getPlayer(UUID uniqueId) {
		return players.get(uniqueId);
	}
	
	public Collection<MeleePlayer> getPlayers(){
		return Collections.unmodifiableCollection(players.values());
	}
	
	public CirculatingList<Location> getSpawns(){
		return spawns;
	}
	
	public boolean hasPlayer(UUID uniqueId) {
		return players.containsKey(uniqueId); 
	}

	// -------end-getters-------- //
	
	public void startGame(Set<Player> startingPlayers, Consumer<Player> onPlayerLeaveGame) {
		this.onPlayerLeaveGame = onPlayerLeaveGame;
		
		CirculatingList<PunchTreeColor> colors = getPlayerColorList();
		
		for (Player player : startingPlayers){
			
			MeleePlayer mp = new MeleePlayer(player, colors.next());
			
			this.addPlayer(mp);
			this.spawnPlayer(mp); //Spawn player before adding to movement to prevent conflicts when changing worlds
			movement.addPlayer(player);
			player.setInvulnerable(false);
			
			
		}
		
		this.setGameState(GameState.RUNNING);
		
		gui.addPlayers(players.values());
		
		gui.playStart();
	}
	
	private CirculatingList<PunchTreeColor> getPlayerColorList(){
		return new CirculatingList<>(PunchTreeColor.getDefaults(), true);
	}
	
	void resetGame() {
		gui.reset();

		// Remove all players (same as removePlayer)
		this.players.keySet().forEach(movement::removePlayer);

		// DON'T forEach THIS, it will throw a ConcurrentModificationException? Not sure why
		for (MeleePlayer mp : this.players.values()) {
			onPlayerLeaveGame.accept(mp.getPlayer());
		}
		this.players.clear();

		
		// Circulating list already reshuffles and reiterates, but this prevents a double spawn on game start
		this.spawns.shuffle();
		this.spawns.resetIterator();
		
		// Reset other state
		this.setLeader(null);
		this.setGameState(GameState.WAITING);
	}
	
	/**
	 * Used for *force* stopping a game (not regular game ending by a win)
	 */
	public void interruptAndShutdown(){
		gui.playStop();
		
		resetGame();
		
		setGameState(GameState.STOPPED);
	}
	
	private void addPlayer(MeleePlayer mp) {
		players.put(mp.getPlayer().getUniqueId(), mp);
		movement.addPlayer(mp.getPlayer());
	}
	
	public boolean removePlayerFromGame(Player player) {
		
		//Is the remove request valid?
		if (!hasPlayer(player.getUniqueId())) return false;
		
		//Remove
		movement.removePlayer(player);
		players.remove(player.getUniqueId());
		gui.removePlayer(player);
		
		// RESTORE STATS & LOCATION
		onPlayerLeaveGame.accept(player);
		
		// End the game if it gets down to one player left.
		if (getPlayers().size() == 1){
			gui.playTooManyLeft();
			resetGame();
		}
	
		return true;
	}
	
	void setLeader(MeleePlayer mp) {
		leader = mp;
	}
	
	void setGameState(GameState gamestate) {
		this.gamestate = gamestate;
	}
	
	// -------------------------- //
	// ---- EVENT RESPONDERS ---- //
	// -------------------------- //
	
	
	public void handleKill(Player killer, Player killed, EntityDamageByEntityEvent e){
		
		if (getGameState() != GameState.RUNNING) return;
		
		MeleePlayer mpKiller = getPlayer(killer.getUniqueId());
		MeleePlayer mpKilled = getPlayer(killed.getUniqueId());
		Location killLocation = killed.getLocation(); // GUI is updated after model, but the game will respawn the player, so we save the kill location
		
		if (mpKiller.equals(mpKilled)){ //if suicide, not a kill
			handleDeath(killed, e);
			return;
		}
		
		e.setCancelled(true); //TODO Maybe refactor into cancelDamage method in MeleeKillEvent?
		
		mpKiller.incrementKills();
		if (isNewLeader(mpKiller)){
			this.setLeader(mpKiller);
		}
		
		gui.playKill(mpKiller, mpKilled, e, killLocation);
		
		this.spawnPlayer(mpKilled);
		
		checkForWinner();
	}
	
	private void checkForWinner() {
		if (leader != null && leader.getKills() >= this.getKillsNeededToWin()) {
			runPostgameWithWinner(leader);
		}
	}
	
	private boolean isNewLeader(MeleePlayer killer) {
		return this.getLeader() == null 
		    || killer.getKills() > this.getLeader().getKills();
	}
	
	public void handleDeath(Player killed, EntityDamageEvent e){
		MeleePlayer mpKilled = getPlayer(killed.getUniqueId());
		Location deathLocation = killed.getLocation();
		
		e.setCancelled(true);
		//Let player take non-fatal damage that isn't caused by a fall or firework explosion
		assert(!damageCauseIsProtected(e.getCause()));
		killed.setHealth(1);

		gui.playDeath(mpKilled, e, deathLocation);
	}
	
	public static boolean damageCauseIsProtected(DamageCause cause) {
		//Fall Damage is off so movement system is non lethal
		//Entity Explosion is off to prevent firework damage from kills
		return cause == DamageCause.FALL || cause == DamageCause.ENTITY_EXPLOSION;
	}

	void runPostgameWithWinner(MeleePlayer winner) {
		gui.playPostgame(winner, MeleeGame.POSTGAME_DURATION_SECONDS);
		
		setGameState(GameState.ENDING);
		players.values().forEach(mp -> {
			mp.getPlayer().setGameMode(GameMode.ADVENTURE);
			mp.getPlayer().setAllowFlight(true);
			mp.getPlayer().setInvulnerable(true);
		});
		
		new BukkitRunnable() {
			@Override
			public void run() {
				resetGame();
			}
		}.runTaskLater(Melee.getPlugin(Melee.class), MeleeGame.POSTGAME_DURATION_SECONDS * 20);
	}
	
	private final double SPAWNING_DISTANCE = 12;
	private final double SPAWNING_DISTANCE_SQUARED = SPAWNING_DISTANCE * SPAWNING_DISTANCE;
	/**
	 * Resets a player's stats and teleports them to a spawn
	 * @param mp The player
	 */
	public void spawnPlayer(MeleePlayer mp){
		Player player = mp.getPlayer();
		
		PlayerUtils.perfectStats(player);
	
		MovementPlayer movementPlayer = MovementPlusPlus.getMovementPlayer(player);
		movementPlayer.setStamina(movementPlayer.getMaxStamina());
		
		// TODO FINISH BEST SPAWN LOGIC
		
//		Location bestSpawn = getSpawns().next();
//		double bestDistance = SPAWNING_DISTANCE_SQUARED;
//		for ()
//		for (MeleePlayer otherPlayer : players.values()) {
//			Location otherLoc = otherPlayer.getPlayer().getLocation();
//			if (otherLoc.getWorld() != bestSpawn.getWorld()) continue;
//			if (bestSpawn.distanceSquared(otherLoc) < SPAWNING_DISTANCE_SQUARED);
//		}
		player.teleport(getSpawns().next());
		
		gui.playSpawn(mp);
	}
	
	public void debug(Player player) {
		player.sendMessage("Game Players: " + getPlayers().stream()
													  .map(mp -> mp.getPlayer().getName())
													  .collect(Collectors.toList()));
		player.sendMessage("Game State: " + getGameState());
	}
	
}
