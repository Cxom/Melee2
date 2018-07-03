package me.cxom.melee2.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.util.CirculatingList;
import me.cxom.melee2.util.PlayerUtils;

/**
 * Represents an instance of a Melee game on one arena. 
 * The game object is persistent across games
 * @author Cxom
 *
 */
public class MeleeGame implements Listener {
	
	private String gameName;
	
	//Arena Fields
	private final MeleeArena arena; //Model
	private final CirculatingList<Location> spawns;
	
	private GameState gamestate = GameState.WAITING;
	
	private Map<UUID, MeleePlayer> players = new HashMap<>();
	
	private MeleePlayer leader = null;
	
	//Ctors
	
	public MeleeGame(MeleeArena arena){
		this(arena.getName(), arena);
	}
	
	/**
	 * Creates a new MeleeGame on the passed arena
	 * @param arena
	 */
	public MeleeGame(String gameName, MeleeArena arena){
		this.gameName = gameName;
		this.arena = arena;
		this.spawns = new CirculatingList<Location>(arena.getSpawns(), true);
		
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
		
	}
	


	
	
	// -------------------------- //
	//-------- GETTERS ---------- //
	// -------------------------- //
	public MeleeArena getArena(){
		return arena;
	}
	
	public GameState getGameState(){
		return gamestate; 
	}
	
	public int getKillsToEnd() {
		return arena.getKillsToEnd();
	}
	
	public MeleePlayer getLeader() {
		return leader;
	}
	
	public String getName() {
		return gameName;
	}
	
	public MeleePlayer getPlayer(UUID uniqueId) {
		return players.get(uniqueId);
	}
	
	public Collection<MeleePlayer> getPlayers(){
		return players.values();
	}
	
	public CirculatingList<Location> getSpawns(){
		return spawns;
	}
	
	public boolean hasPlayer(UUID uniqueId) { return players.containsKey(uniqueId); }
	public boolean hasPlayer(Player player) { 
		return hasPlayer(player.getUniqueId()); 
	}
	
	public boolean isAvailable() {
		return gamestate == GameState.WAITING || gamestate == GameState.STARTING; 
	}

	// -------end-getters-------- //
	
	
	
	
	// -------------------------- //
	// ------- SETTERS ---------- //
	// -------------------------- //
	void addPlayer(MeleePlayer mp) {
		players.put(mp.getPlayer().getUniqueId(), mp);
	}
	
	/**
	 * Removes a player from the game
	 * @param player
	 * @return The player removed (or null, if the player was not in the game)
	 */
	MeleePlayer removePlayer(Player player){
		//TODO maybe send message if player still in 
		
		// STOP TRACKING
		return players.remove(player.getUniqueId());
	}
	
	void setLeader(MeleePlayer mp) {
		leader = mp;
	}
	
	public void setState(GameState gamestate) {
		this.gamestate = gamestate;
	}
	// --------end-setters------- //
	
	
	/**
	 * Resets a player's stats and teleports them to a spawn
	 * @param mp The player
	 */
	void spawnPlayer(MeleePlayer mp){
		Player player = mp.getPlayer();
		
		PlayerUtils.perfectStats(player);
		player.teleport(getSpawns().next());
	}
	
	// -------------------------- //
	// ------- LISTENERS -------- //
	
	// Listeners belong here if they reflect changes to the model that we want
	// to make be different changes to the model (relationship strictly between
	// domain objects)
	
	// Cancelling entity-explosion prevents damage from fireworks
	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player
		 && hasPlayer(e.getEntity().getUniqueId()) 
    	 && (e.getCause() == DamageCause.FALL || e.getCause() == DamageCause.ENTITY_EXPLOSION)){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player && this.hasPlayer(e.getEntity().getUniqueId())){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerRegainHealth(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player && this.hasPlayer(e.getEntity().getUniqueId())){
			e.setCancelled(true);
		}
	}
	
	
}
