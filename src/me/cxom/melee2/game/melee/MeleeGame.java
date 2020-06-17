package me.cxom.melee2.game.melee;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.trinoxtion.movement.MovementSystem;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.player.MeleePlayer;
import net.punchtree.minigames.game.GameState;
import net.punchtree.minigames.game.PvpGame;
import net.punchtree.minigames.utility.collections.CirculatingList;
import net.punchtree.minigames.utility.color.MinigameColor;
import net.punchtree.minigames.utility.player.PlayerUtils;

/**
 * Represents an instance of a Melee game on one arena. 
 * The game object is persistent across games
 * @author Cxom
 */
public class MeleeGame implements PvpGame, Listener {
	
	// Class constants
	public static final int POSTGAME_DURATION_SECONDS = 10;
	
	// Persistent properties
	private final MeleeArena arena; 
	private final CirculatingList<Location> spawns;
	private final MovementSystem movement;
	
	// State fields
	private GameState gamestate = GameState.WAITING;
	private Map<UUID, MeleePlayer> players = new HashMap<>();
	private MeleePlayer leader = null;
	
	
	public MeleeGame(MeleeArena arena, MovementSystem movement){
		this.arena = arena;
		this.spawns = new CirculatingList<Location>(arena.getSpawns(), true);
		this.movement = movement;
		
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
	
	public int getKillsNeededToWin() {
		return arena.getKillsToEnd();
	}
	
	public MeleePlayer getLeader() {
		return leader;
	}
	
	public String getName() {
		return "Melee";
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
	
	public boolean hasPlayer(UUID uniqueId) {
		return players.containsKey(uniqueId); 
	}
	public boolean hasPlayer(Player player) { 
		return hasPlayer(player.getUniqueId()); 
	}
	
	public boolean hasWinner() {
		return leader != null && leader.getKills() >= arena.getKillsToEnd();
	}

	// -------end-getters-------- //
	
	void startGame(Set<Player> startingPlayers) {
		
		CirculatingList<MinigameColor> colors = getPlayerColorList();
		
		for (Player player : startingPlayers){
			
			MeleePlayer mp = new MeleePlayer(player, colors.next());
			
			this.addPlayer(mp);
			this.spawnPlayer(mp); //Spawn player before adding to movement to prevent conflicts when changing worlds
			movement.addPlayer(player);
			player.setInvulnerable(false);
		}
		
		this.setGameState(GameState.RUNNING);
		
	}
	
	private CirculatingList<MinigameColor> getPlayerColorList(){
		return new CirculatingList<>(MinigameColor.getDefaults(), true);
	}
	
	void resetGame() {
		
		// Remove all players (same as removePlayer)
		this.players.keySet().forEach(movement::removePlayer);
		this.players.clear();
		
		// Reset other state
		this.setLeader(null);
		this.setGameState(GameState.WAITING);
	}
	
	private void addPlayer(MeleePlayer mp) {
		players.put(mp.getPlayer().getUniqueId(), mp);
		movement.addPlayer(mp.getPlayer());
	}
	
	MeleePlayer removePlayer(Player player){
		movement.removePlayer(player);
		return players.remove(player.getUniqueId());
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
	
	
	void handleKill(MeleePlayer killer, MeleePlayer killed, EntityDamageByEntityEvent e){
		
		if (killer.equals(killed)){ //if suicide, not a kill
			handleDeath(killed, e);
			return;
		}
		
		e.setCancelled(true); //TODO Maybe refactor into cancelDamage method in MeleeKillEvent?
		
		killer.incrementKills();
		if (isNewLeader(killer)){
			this.setLeader(killer);
		}
		
		this.spawnPlayer(killed);
		
		if (hasWon(killer)){
			runPostgameWithWinner(killer);
		}
		
	}
	
	private boolean isNewLeader(MeleePlayer killer) {
		return this.getLeader() == null 
		    || killer.getKills() > this.getLeader().getKills();
	}
	
	private boolean hasWon(MeleePlayer player) {
		return player.getKills() >= this.getKillsNeededToWin();
	}
	
	void handleDeath(MeleePlayer killed, EntityDamageEvent e){
		e.setCancelled(true);
		
		if (!damageCauseIsProtected(e.getCause())){
			((Player) e.getEntity()).setHealth(1);
		}
	}
	
	static boolean damageCauseIsProtected(DamageCause cause) {
		//Fall Damage is off so movement system is non lethal
		//Entity Explosion is off to prevent firework damage from kills
		return cause == DamageCause.FALL || cause == DamageCause.ENTITY_EXPLOSION;
	}
	
	// Runs the end of the game
	private void runPostgameWithWinner(MeleePlayer winner) {
		
		setGameState(GameState.ENDING);
		players.values().forEach(mp -> {
			mp.getPlayer().setGameMode(GameMode.ADVENTURE);
			mp.getPlayer().setAllowFlight(true);
			mp.getPlayer().setInvulnerable(true);
		});
		
	}
	
	
	/**
	 * Resets a player's stats and teleports them to a spawn
	 * @param mp The player
	 */
	public void spawnPlayer(MeleePlayer mp){
		Player player = mp.getPlayer();
		
		PlayerUtils.perfectStats(player);
		player.teleport(getSpawns().next());
	}
	
}
