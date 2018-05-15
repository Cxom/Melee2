package me.cxom.melee2.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.game.combat.AttackMethod;
import me.cxom.melee2.game.gui.MeleeBossBar;
import me.cxom.melee2.game.gui.ScrollingScoreboard;
import me.cxom.melee2.player.MeleeColor;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.CirculatingList;
import me.cxom.melee2.util.FireworkUtils;
import me.cxom.melee2.util.PlayerUtils;

/**
 * Represents an instance of a Melee game on one arena. 
 * The game object is persistent across games
 * 
 * @author Cxom
 *
 */
public class GameInstance {

	private final MeleeArena arena;
	private final Lobby lobby;
	
	private final CirculatingList<Location> spawns;
	
	private final MovementSystem movement = MovementPlusPlus.CXOMS_MOVEMENT;
	private final MeleeBossBar bossbar;
	private final ScrollingScoreboard killfeed = new ScrollingScoreboard(Melee.CHAT_PREFIX);	

	GameState gamestate = GameState.STOPPED;
	
	Map<UUID, MeleePlayer> players = new HashMap<>();
	
	private int mostKills = 0;
	
	//Ctor
	/**
	 * Creates a new MeleeGame on the passed arena
	 * @param arena
	 */
	public GameInstance(MeleeArena arena){
		this.arena = arena;
		this.lobby = new Lobby(this);
		this.spawns = new CirculatingList<Location>(arena.getSpawns(), true);
		
		this.bossbar = new MeleeBossBar();
		
		new MeleeEventListeners(this);
		
		this.gamestate = GameState.WAITING;
	}
	
	// ----------------------------------- //
	// -------- PLAYER METHODS -------- //
	// ----------------------------------- //
	
	/**
	 * Adds a player to the game's lobby
	 * @param player
	 */
	public void addPlayer(Player player) {
		PlayerProfile.save(player);
		lobby.addPlayer(player);
	}
	
	/**
	 * Sends a message to all players in the game
	 * @param message
	 */
	private void broadcast(String message){
		players.values().forEach(mp -> mp.getPlayer().sendMessage(message));
	}
	
	/**
	 * Resets a player's stats and teleports them to a spawn
	 * @param mp The player
	 */
	private void spawnPlayer(MeleePlayer mp){
		Player player = mp.getPlayer();
		
		PlayerUtils.perfectStats(player);
		player.teleport(spawns.next());
	}
	
	/**
	 * Removes a player from the game or the game's lobby
	 * @param player
	 * @return True if the player was removed from the game or lobby
	 */
	public boolean removePlayer(Player player){
		if (players.containsKey(player.getUniqueId())){
			
			//TODO maybe send message if player still in 
			
			//caching? TODO If you leave and rejoin while the match is still in progress, it saves your stats
			killfeed.removePlayer(player);
			bossbar.removePlayer(player);
			
			movement.removePlayer(player);
			
			players.remove(player.getUniqueId());
			
			PlayerProfile.restore(player); //This restores location
			
			if (gamestate == GameState.RUNNING && players.size() == 1){
				broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "Too many people have left. Shutting down the game :/");
				reset();
			}
			
			return true;
		
		} else {
			
			return lobby.removePlayer(player);
			
		}
	}
	
	// ----------------------------------- //
	// -------- GAMESTATE METHODS -------- //
	// ----------------------------------- //
	
	/**
	 * Starts a new game with a set of starting players.
	 * @param startingPlayers
	 */
	void start(Set<Player> startingPlayers){
		
		if (players.size() > 0) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/mail Cxomtdoh a MeleeInstance attempted to start with players still in the set!");
			throw new AssertionError("There should be no players in MeleeInstance before game start!"
					+ " Something is not clearing properly.");
		}
		
		CirculatingList<MeleeColor> colors = new CirculatingList<>(MeleeColor.getDefaults(), true);
		for (Player player : startingPlayers){
			
			MeleePlayer mp = new MeleePlayer(player, colors.next());
			players.put(player.getUniqueId(), mp);
			
			//Spawn player first to prevent conflicts with world settings
			spawnPlayer(mp);
			
			movement.addPlayer(player);
			bossbar.addPlayer(player);
			killfeed.addPlayer(player);
			
			player.setInvulnerable(false);
			
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			player.sendMessage(mp.getColor().getChatColor() + "You are " + mp.getColor().getChatColor().name().replace('_', ' ') + "!");
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
		
		gamestate = GameState.RUNNING;
		
	}
	
	private void runPostgame(MeleePlayer winner) {
		
		this.gamestate = GameState.ENDING; 
		
		String winMessage = winner.getColor().getChatColor() + winner.getPlayer().getName() + ChatColor.WHITE + " has won the game!";
		bossbar.setMessage(winMessage);
		broadcast(Melee.CHAT_PREFIX + winMessage);
		
		Color winnersColor = winner.getColor().getBukkitColor();
		new BukkitRunnable(){
			int i = 10; //10 seconds
			Random r = new Random();
			@Override
			public void run(){
				if (i <= 0){
					this.cancel();
					reset();
					return;
				}
				FireworkUtils.spawnFirework(spawns.next(), winnersColor, r.nextInt(2) + 1);
				i--;
			}
		}.runTaskTimer(Melee.getPlugin(), 10, 20);
	}
	
	private void reset(){
		
		//lobby.removeAll(); What was this doing here?????
		killfeed.removeAll();
		bossbar.removeAll();
		//movement.removeAll(); See below line (maybe change API)
		players.keySet().forEach(movement::removePlayer);
		
		players.keySet().forEach(PlayerProfile::restore);
		
		bossbar.setMessage(ChatColor.RESET + "Now playing on " + ChatColor.ITALIC + arena.getName() + ChatColor.RESET + "!");
		
		players.clear();
		
		mostKills = 0;
		
		gamestate = GameState.WAITING;
	}
	
	public void stop(){
		broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "The game has been interrupted. Stopping . . .");
		reset();
		lobby.removeAll();
		gamestate = GameState.STOPPED;
	}
	
	// Melee Kills and Deaths
	void onMeleeKill(MeleePlayer killer, MeleePlayer killed, EntityDamageByEntityEvent e){
			
		//if suicide, not a kill
		if (killer.equals(killed)){
			onMeleeDeath(killed.getPlayer(), e);
			return;
		}
		
		//cancel damage
		e.setCancelled(true); //TODO Maybe refactor into cancelDamage method in MeleeKillEvent?
		
		FireworkUtils.detontateInstantly(FireworkUtils.spawnFirework(killed.getPlayer().getLocation().add(0, 1.1, 0), killed.getColor(), killer.getColor(), 0));
		
		spawnPlayer(killed);
		
		killer.incrementKills();
		if (killer.getKills() > mostKills){
			bossbar.setLeader(killer);
			mostKills = killer.getKills();
		} else if (killer.getKills() == mostKills){
			bossbar.setTier(killer);
		}
		
		//Send chat messages
		killer.getPlayer().sendMessage(Melee.CHAT_PREFIX + ChatColor.GRAY + "You now have "
												+ ChatColor.AQUA + killer.getKills()
												+ ChatColor.GRAY + " kill(s).");
		killed.getPlayer().sendMessage(Melee.CHAT_PREFIX + killer.getColor().getChatColor() + killer.getPlayer().getName()
											 	+ " (" + killer.getKills() + ") killed you!");
		
		//Send killfeed message
		String killfeedMessage = String.format("%s%s:%d %s %s%s",
				killer.getColor().getChatColor(), killer.getPlayer().getName(), killer.getKills(),
				ChatColor.WHITE + AttackMethod.getAttackMethod(e.getDamager()).getIcon(),
				killed.getColor().getChatColor(), killed.getPlayer().getName());
		int length = 39 - String.valueOf(killed.getKills()).length(); //40 chars max - 1 for color = 39
		killfeedMessage = killfeedMessage.length() > length ? killfeedMessage.substring(0, length) : killfeedMessage;
		killfeedMessage += ":" + killed.getKills();
		killfeed.sendMessage(killfeedMessage);
		
		
		if (killer.getKills() == arena.getKillsToEnd()){
			runPostgame(killer);
		}
		
	}
	
	void onMeleeDeath(Player killed, EntityDamageEvent e){
		e.setCancelled(true);
		//Let player take non-fatal damage that isn't caused by fall damage
		if (e.getCause() != DamageCause.FALL){
			((Player) e.getEntity()).setHealth(1);
		}
	}
	
	//----Getters----//
	
	public GameState getGameState(){
		return gamestate; 
	}
	
	public Lobby getLobby(){
		return lobby;
	}
	
	public MeleeArena getArena(){
		return arena;
	}
	
	//----Debug Methods----//
	
	public void debug(Player player) {
		player.sendMessage("Game Players: " + players.keySet().stream()
													  .map(u -> Bukkit.getPlayer(u).getName())
													  .collect(Collectors.toList()));
		player.sendMessage("Game State: " + gamestate);
		player.sendMessage("Lobby players: " + lobby.getWaitingPlayers().stream().map(Player::getName).collect(Collectors.toList()));
	}
	
}
