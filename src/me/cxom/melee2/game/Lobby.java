package me.cxom.melee2.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import me.cxom.melee2.Melee;

public class Lobby implements Listener {
	
//	public enum LobbyState {
//		WAITING(ChatColor.GREEN, new ItemStack(Material.CONCRETE, 1 , (short) 5)),
//		STARTING(ChatColor.YELLOW, new ItemStack(Material.CONCRETE, 1 , (short) 4)),
//		UNAVAILABLE(ChatColor.RED, new ItemStack(Material.CONCRETE, 1 , (short) 14));
//		
//		private final ChatColor chatColor;
//		private final ItemStack menuItem;
//		
//		private LobbyState(ChatColor chatColor, ItemStack menuItem){
//			this.chatColor = chatColor;
//			this.menuItem = menuItem;
//		}
//		
//		public ChatColor getChatColor(){
//			return chatColor;
//		}
//		
//		public ItemStack getMenuItem(){
//			return menuItem.clone();
//		}
//	}
	
	
	
	//static variables

	
	
	//instance variables
	private final String lobbyName;
	private MeleeGame game;
	private final Location lobbySpawn;
	private final int playersNeededToStart;

	
	private Map<Player, Boolean> waitingPlayers = new HashMap<>();
	private int playersReady = 0;
	
//	private LobbyState lobbyState = LobbyState.UNAVAILABLE;
	
	
	//Ctors
	public Lobby(MeleeGame game) {
		this(game.getArena().getName(),
			 game,
			 game.getArena().getPregameLobby(),
			 game.getArena().getPlayersNeededToStart());
	}
	
	public Lobby(String lobbyName, MeleeGame game, Location lobbySpawn, int playersToStart){
		this.lobbyName = lobbyName;
		this.game = game;
		this.lobbySpawn = lobbySpawn;
		this.playersNeededToStart = playersToStart;
		
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	// ---------- GETTERS ----------- //
	public MeleeGame getGame() {
		return game;
	}
	
	Location getSpawn() {
		return lobbySpawn;
	}
	
//	public LobbyState getLobbyState() {
//		return lobbyState;
//	}
	
	public String getName() {
		return lobbyName;
	}
	
	public int getPlayersNeededToStart() {
		return playersNeededToStart;
	}
	
	public int getPlayersReady() {
		return playersReady;
	}
	
	public int getPlayersWaiting() {
		return waitingPlayers.size();
	}
	
	public Set<Player> getPlayers(){
		return waitingPlayers.keySet();
	}
	
	public boolean hasPlayer(Player player) {
		return waitingPlayers.containsKey(player);
	}
	
	public boolean hasReadyPlayer(Player player) {
		if ( ! hasPlayer(player)) return false;
		return waitingPlayers.get(player);
	}
	
//	public boolean isAvailable() {
//		return lobbyState != LobbyState.UNAVAILABLE;
//	}
	// --------end-getters----------- //
	
	
	
	/**
	 * Adds a player to the lobby if possible
	 * 
	 * @param player The player to be added
	 */
	void addPlayer(Player player){
		
		//Add to a list, teleport to lobby, and (re)set inventory
		waitingPlayers.put(player, false);
		
	}
	
	boolean setPlayerReadiness(Player player, boolean ready){
		
		if (! hasPlayer(player)) throw new IllegalArgumentException("Can't set the readiness of a player not in the lobby!");
		
		if (waitingPlayers.get(player) != ready) {
			if (ready) {
				incrementPlayersReady();
			} else {
				decrementPlayersReady();
			}
		}
		
		return waitingPlayers.put(player, ready);
	}
	
	/**
	 * Removes a player from the lobby
	 * 
	 * @param player The player to be removed
	 * 
	 * @return True if the player was in the lobby
	 */
	void removePlayer(Player player){
		if (hasReadyPlayer(player)) {
			playersReady--;
		}
		waitingPlayers.remove(player);
	}
	
	/**
	 * Removes all players from the Lobby
	 */
	//TODO Rename this to removeAll sometime when Eclipse isn't a piece of shit.
	void clear() {
		waitingPlayers.clear();
		playersReady = 0;
	}

	private void incrementPlayersReady() {
		playersReady = Math.min(getPlayersWaiting(), playersReady + 1);
	}
	
	private void decrementPlayersReady() {
		playersReady = Math.max(0, playersReady - 1);
	}
	
}
