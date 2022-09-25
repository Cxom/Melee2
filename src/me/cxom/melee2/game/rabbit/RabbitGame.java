package me.cxom.melee2.game.rabbit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.RabbitArena;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.gui.rabbit.RabbitGUI;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.player.RabbitPlayer;
import net.punchtree.minigames.game.GameState;
import net.punchtree.minigames.game.PvpGame;
import net.punchtree.minigames.utility.collections.CirculatingList;
import net.punchtree.minigames.utility.player.InventoryUtils;
import net.punchtree.minigames.utility.player.PlayerUtils;
import net.punchtree.util.color.PunchTreeColor;

/**
 * Represents an instance of a Rabbit game on one arena. 
 * The game object is persistent across games
 * @author Cxom
 *
 */
public class RabbitGame implements PvpGame, Listener {

	// Class constants
	public static final int POSTGAME_DURATION_SECONDS = 10;
	
	// Persistent properties
	private final RabbitArena arena; //Model
	private final CirculatingList<Location> spawns;
	private final Location flagSpawnLocation;
	private final RabbitGUI gui;
	private final MovementSystem movement = MovementPlusPlus.CXOMS_MOVEMENT;
	
	// Flag constants
	private final int firstFlagSpawnDelay = 15; // seconds
	private final double flagPickupRadius = 1.5;
	private final int flagTaskRate = 5;
	private final int timeToWin;
	
	// State fields
	private GameState gamestate = GameState.WAITING;

	private Consumer<Player> onPlayerLeaveGame;
	
	//TODO does TreeMap do what I want it to?
	private SortedMap<UUID, RabbitPlayer> players = new TreeMap<>();

	//private Map<UUID, Integer> progress = new HashMap<>();
	private RabbitPlayer flagLeader = null;
	
	// Flag state
	public enum FlagStatus {
		NOT_IN_PLAY,
		SPAWNED,
		HELD,
		DROPPED,
	}
	
	private FlagStatus flagStatus = FlagStatus.NOT_IN_PLAY;
	
	//-----------------
	// Only one of the following groups should be non-null at a time
	//-----------------
	private RabbitPlayer flagHolder = null;
	
	private Location droppedFlagLocation = null;
	//-----------------
	
	public RabbitGame(RabbitArena arena){
		this.arena = arena;
		this.spawns = new CirculatingList<>(arena.getSpawns(), true);
		this.flagSpawnLocation = arena.getCenterpoint();
		this.timeToWin = Math.round(arena.getFlagTimeToWin() * (20f / flagTaskRate));
		this.gui = new RabbitGUI(this);
		new RabbitEventListeners(this);
		
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	// Flag task
	private BukkitTask flagTask;
	
	private class FlagTimer extends BukkitRunnable {
		@Override
		public void run() {
			if (RabbitGame.this.getFlagStatus() == FlagStatus.HELD) {
				
				flagHolder.decrementFlagCounter();
				checkForWinner();
				
//				} else if (flagHolder.getFlagCounter() < getFlagLeader().getFlagCounter()) {
//					setLeader(flagHolder);
//				}
				
			} else if (RabbitGame.this.getFlagStatus() == FlagStatus.DROPPED) {
				
			}
		}
		
		private void checkForWinner(){
			if (flagHolder.getFlagCounter() == 0) {
				this.cancel();
				
				runPostgameWithWinner(flagHolder);
				
			}
		}
	}

	private void cancelFlagTask() {
		if (flagTask != null) {
			flagTask.cancel();
			flagTask = null;
		}
	}


	
	// -------------------------- //
	//-------- GETTERS ---------- //
	// -------------------------- //
	public RabbitArena getArena(){
		return arena;
	}
	
	public int getInitialFlagSpawnDelay() {
		return firstFlagSpawnDelay;
	}
	
	public Location getFlagSpawnLocation() {
		return flagSpawnLocation;
	}
	
	public FlagStatus getFlagStatus() {
		return flagStatus;
	}
	
	public int getFlagTaskRate() {
		return flagTaskRate;
	}
	
	public GameState getGameState(){
		return gamestate; 
	}

	public int getTimeToWin() {
		return timeToWin;
	}
	
	public int getTimeToWinInSeconds() {
		return arena.getFlagTimeToWin();
	}
	
	public RabbitPlayer getFlagHolder() {
		return flagHolder;
	}
	
	public RabbitPlayer getFlagLeader() {
		return flagLeader;
	}
	
	public String getName() {
		return "Rabbit";
	}
	
	public RabbitPlayer getPlayer(UUID uniqueId) {
		return players.get(uniqueId);
	}
	
	public Collection<RabbitPlayer> getPlayers(){
		return players.values();
	}
	
	public CirculatingList<Location> getSpawns(){
		return spawns;
	}
	
	public boolean hasPlayer(UUID uniqueId) { return players.containsKey(uniqueId); }
	public boolean hasPlayer(Player player) { 
		return hasPlayer(player.getUniqueId()); 
	}
	
//	public boolean hasWinner() {
//		return leader != null && leader.getFlagCounter() == 0;
//	}

	// -------end-getters-------- //
	
	
	
	
	// -------------------------- //
	// ------- SETTERS ---------- //
	// -------------------------- //
	
	public void startGame(Set<Player> startingPlayers, Consumer<Player> onPlayerLeaveGame) {
		this.onPlayerLeaveGame = onPlayerLeaveGame;
		
		CirculatingList<PunchTreeColor> colors = new CirculatingList<>(PunchTreeColor.getDefaults(), true);
		
		for (Player player : startingPlayers){
			
			RabbitPlayer rp = new RabbitPlayer(player, colors.next(), timeToWin);
			
			this.addPlayer(rp);
			this.spawnPlayer(rp); //Spawn player before adding to movement to prevent conflicts with world settings
			movement.addPlayer(player);
			player.setInvulnerable(false);
		}
		
		this.setState(GameState.RUNNING);
		
		notifyGameStart();
		
		doInitialFlagSpawn();
		
		gui.addPlayers(getPlayers());
		
		gui.playStart();
	}
	
	private void doInitialFlagSpawn() {
		new BukkitRunnable() {
			public void run() {
				spawnFlagAtCenter();
				initializeFlagCounterTask();
			}
		}.runTaskLater(Melee.getPlugin(Melee.class), firstFlagSpawnDelay * 20);
	}
	
	private void initializeFlagCounterTask() {
		flagTask = new FlagTimer().runTaskTimer(Melee.getPlugin(Melee.class), 0, flagTaskRate);
	}
	
	void resetGame() {

		cancelFlagTask();
		
		// Remove all players (same as removePlayer
		this.players.keySet().forEach(movement::removePlayer);
				
		// Reset other state
		this.setLeader(null);
		this.flagStatus = FlagStatus.NOT_IN_PLAY;
		this.flagHolder = null;
		
		removeDroppedFlag();
		
		removeFlagAtCenter();
		
		notifyGameReset(this.players.values());
		
		this.players.values().stream().map(MeleePlayer::getPlayer).forEach(onPlayerLeaveGame);
		
		this.setState(GameState.WAITING);
		
		this.players.clear();
		this.spawns.shuffle();
		this.spawns.resetIterator();
	}
	
	/**
	 * Used for *force* stopping a game (not regular game ending by a win)
	 */
	public void interruptAndShutdown(){
		gui.playStop();
		
		resetGame();

		setState(GameState.STOPPED);
	}
	
	void addPlayer(RabbitPlayer rp) {
		players.put(rp.getPlayer().getUniqueId(), rp);
		movement.addPlayer(rp.getPlayer());
		
		//TODO RabbitPlayer (... extends MinigamePlayer? ) 
		// I think extends ColoredPlayer - or even? Usefulness? Point of code reuse? Is it because of kill tracking?
		// Usefulness is a PvpPlayer making use of composition
//		progress.put(mp.getPlayer().getUniqueId(), flagTimeToWin);
	}
	
	/**
	 * Removes a player from the game
	 * @param player
	 * @return The player removed (or null, if the player was not in the game)
	 */
	boolean removePlayerFromGame(Player player) {
		//TODO maybe send message if player still in 
		
		//Is the remove request valid?
		if (!hasPlayer(player.getUniqueId())) return false;
		
		// Reverse order of how added
		movement.removePlayer(player);
		players.remove(player.getUniqueId());
		gui.removePlayer(player);
		
		// RESTORE STATS & LOCATION
		onPlayerLeaveGame.accept(player);
		
		// End the game if it gets down to one left.
		if (getPlayers().size() == 1){
			gui.playTooManyLeft();
			resetGame();
		}
	
		return true;
	}
	
	private void setFlagStatus(FlagStatus status) {
//		Bukkit.broadcastMessage(getFlagStatus() + " -> " + status);
//		notifyFlagStatusChange(getFlagStatus(), status);
		this.flagStatus = status;
	}
	
	void setLeader(RabbitPlayer rp) {
		this.flagLeader = rp;
	}
	
	public void setState(GameState gamestate) {
		this.gamestate = gamestate;
	}
	// --------end-setters------- //
	

	
	
	// -------------------------- //
	// ------ FLAG METHODS ------ //
	// -------------------------- //	
	
	private void spawnFlagAtCenter() {
		spawnFlagEntity(getFlagSpawnLocation());
		setFlagStatus(FlagStatus.SPAWNED);
		notifyFlagSpawn(getFlagSpawnLocation(), true);
	}
	
	private void spawnFlagEntity(Location spawnLocation) {
		spawnLocation.getBlock().setType(Material.WHITE_BANNER);
		
		Banner banner = (Banner) spawnLocation.getBlock().getState();
		banner.setBaseColor(DyeColor.LIGHT_GRAY);
		banner.update();
	}

//	private void removeFlagEntity() {
//		if (flagStatus == FlagStatus.SPAWNED) {
//			removeFlagAtCenter();
//		} else if (flagStatus == FlagStatus.DROPPED) {
//			removeDroppedFlag();
//		}
//	}
	
	private void removeFlagAtCenter() {
		flagSpawnLocation.getBlock().setType(Material.AIR);
	}
	
	private void givePlayerFlag(RabbitPlayer rp) {
		rp.getPlayer().getInventory().setHelmet(new ItemStack(Material.WHITE_BANNER));
		this.flagHolder = rp;
		rp.getPlayer().setGlowing(true);
		MovementPlayer mvrp = MovementPlusPlus.getMovementPlayer(rp.getUniqueId());
		mvrp.setMaxStamina(mvrp.getMaxStamina() * 2);
	}
	
	private void removeFlagFromPlayer(RabbitPlayer rp) {
		InventoryUtils.equipPlayer(rp.getPlayer(), rp.getColor());
	}
	
	// TODO all events outside this class?
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (! players.containsKey(e.getPlayer().getUniqueId())) return;
			
		if (this.getFlagStatus() == FlagStatus.SPAWNED && e.getTo().distance(getFlagSpawnLocation()) <= flagPickupRadius) {
			RabbitPlayer rp = players.get(e.getPlayer().getUniqueId());
			this.givePlayerFlag(rp);
			this.removeFlagAtCenter();
			this.setFlagStatus(FlagStatus.HELD);
			this.notifyFlagPickUp(rp, false);
		} else if (this.getFlagStatus() == FlagStatus.DROPPED && e.getTo().distance(droppedFlagLocation) <= flagPickupRadius) {
			RabbitPlayer rp = players.get(e.getPlayer().getUniqueId());
			this.givePlayerFlag(rp);
			this.removeDroppedFlag();
			this.setFlagStatus(FlagStatus.HELD);
			this.notifyFlagPickUp(rp, true);
		}
	}
	
	private void dropFlag(Location flagHolderDeathLocation) {
		
		RabbitPlayer flagDropper = getFlagHolder();
		
		MovementPlayer mvrp = MovementPlusPlus.getMovementPlayer(flagDropper.getUniqueId());
		mvrp.setMaxStamina(mvrp.getMaxStamina() / 2);
		
		removeFlagFromPlayer(flagHolder);
		flagHolder.getPlayer().setGlowing(false);
		this.flagHolder = null;
		
		Block droppedFlagBlock = flagHolderDeathLocation.getBlock();
		while(droppedFlagBlock.getType() != Material.AIR) {
			droppedFlagBlock = droppedFlagBlock.getRelative(BlockFace.UP);
			if (droppedFlagBlock.getY() >= 255) {
				//TODO do this properly
				Bukkit.broadcastMessage("Could not spawn dropped flag!! Respawning at spawn immediately");
				spawnFlagAtCenter();
			}
		}
		
		this.droppedFlagLocation = droppedFlagBlock.getLocation();
		
		spawnFlagEntity(this.droppedFlagLocation);

		setFlagStatus(FlagStatus.DROPPED);
		
		notifyFlagDrop(flagDropper, this.droppedFlagLocation);
	}
	
	private void removeDroppedFlag() {
		if (this.droppedFlagLocation != null) {
			this.droppedFlagLocation.getBlock().setType(Material.AIR);
			this.droppedFlagLocation = null;
		}
	}
	
	// ------end-flag-methods------ //
	
	
	
	
	
	
	
	
	
	
	
	// -------------------------- //
	// ---- EVENT RESPONDERS ---- //
	// -------------------------- //
	
	void handleKill(Player killer, Player killed, EntityDamageByEntityEvent e){
		
		if (getGameState() != GameState.RUNNING) return;
		
		RabbitPlayer rpKiller = getPlayer(killer.getUniqueId());
		RabbitPlayer rpKilled = getPlayer(killed.getUniqueId());
		Location killLocation = killed.getLocation(); // GUI is updated after model, but the game will respawn the player, so we save the kill location
		
		//if suicide, not a kill
		if (rpKiller.equals(rpKilled)){
			handleDeath(killed, e);
			return;
		}
		
		//cancel damage
		e.setCancelled(true); //TODO Maybe refactor into cancelDamage method in MeleeKillEvent?
		
		rpKiller.incrementKills();
		
		if (rpKilled.equals(this.flagHolder)) { //May need to move this after spawning if the dead player still
												// picks up the dropped flag
			this.dropFlag(rpKilled.getPlayer().getLocation());
			
		}
		
		this.spawnPlayer(rpKilled);
		
		gui.playKill(rpKiller, rpKilled, e, killLocation);
	}
	
	void handleDeath(Player killed, EntityDamageEvent e){
		RabbitPlayer rpKilled = getPlayer(killed.getUniqueId());
		Location deathLocation = killed.getLocation();
		
		e.setCancelled(true);
		//Let player take non-fatal damage that isn't caused by a fall or firework explosion
		if (e.getCause() != DamageCause.FALL && e.getCause() != DamageCause.ENTITY_EXPLOSION){
			((Player) e.getEntity()).setHealth(1);
		}

		gui.playDeath(rpKilled, e, deathLocation);
	}
	
	// Runs the end of the game
	private void runPostgameWithWinner(RabbitPlayer winner) {
		notifyGameWin(flagHolder);
		
		setState(GameState.ENDING);
		
		players.values().forEach(mp -> {
			mp.getPlayer().setGameMode(GameMode.ADVENTURE);
			mp.getPlayer().setAllowFlight(true);
			mp.getPlayer().setInvulnerable(true);
		});
		
		resetAfterPostgame();
	}
	
	private void resetAfterPostgame() {
		new BukkitRunnable() {
			@Override
			public void run() {
				RabbitGame.this.resetGame();
			}
		}.runTaskLater(Melee.getPlugin(Melee.class), MeleeGame.POSTGAME_DURATION_SECONDS * 20);
	}
	
	
	/**
	 * Resets a player's stats and teleports them to a spawn
	 * @param mp The player
	 */
	void spawnPlayer(RabbitPlayer mp){
		Player player = mp.getPlayer();
		
		PlayerUtils.perfectStats(player);
		player.teleport(getSpawns().next());
	}
	
	// -------------------------- //
	// ------- LISTENERS -------- //
	
	// Listeners belong here if they reflect changes to the model that we want
	// to make be different changes to the model (relationship strictly between
	// domain objects)
	
	
	//TODO interesting idea for this. We can create a class with a whole bunch of event listeners and a set of collections for each
	//then, you can just use a builder style method chaining with an anonymous instantiation to cancel exactly the events you want
	//without repeating this code
	
	static boolean damageCauseIsProtected(DamageCause cause) {
		//Fall Damage is off so movement system is non lethal
		//Entity Explosion is off to prevent firework damage from kills
		return cause == DamageCause.FALL || cause == DamageCause.ENTITY_EXPLOSION;
	}
	
	public void debug(Player player) {
		player.sendMessage("Game Players: " + getPlayers().stream()
													  .map(mp -> mp.getPlayer().getName())
													  .collect(Collectors.toList()));
		player.sendMessage("Game State: " + getGameState());
	}
	
	// -------------------
	// OBSERVABLE METHODS
	// -------------------
	
	private Set<RabbitGameObserver> observers = new HashSet<>();
	
	public void registerObserver(RabbitGameObserver observer) {
		observers.add(observer);
	}
	
	public void deregisterObserver(RabbitGameObserver observer) {
		observers.remove(observer);
	}
	
	private void notifyGameStart() {
		observers.forEach(o -> o.onGameStart());
	}
	
	private void notifyGameWin(RabbitPlayer winner) {
		observers.forEach(o -> o.onGameWin(winner));
	}
	
	private void notifyGameReset(Collection<RabbitPlayer> rPlayers) {
		observers.forEach(o -> o.onGameReset(rPlayers));
	}
	
	private void notifyFlagSpawn(Location flagSpawnLocation, boolean isRespawned) {
		observers.forEach(o -> o.onFlagSpawn(flagSpawnLocation, isRespawned));
	}
	
	private void notifyFlagPickUp(RabbitPlayer flagHolder, boolean wasDropped) {
		observers.forEach(o -> o.onFlagPickUp(flagHolder, wasDropped));
	}
	
	private void notifyFlagDrop(RabbitPlayer flagDropper, Location flagDropLocation) {
		observers.forEach(o -> o.onFlagDrop(flagDropper, flagDropLocation));
	}
	
//	private void notifyFlagStatusChange(FlagStatus oldStatus, FlagStatus newStatus) {
//		observers.forEach(o -> o.onFlagStatusChange(oldStatus, newStatus));
//	}

}