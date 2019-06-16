package me.cxom.melee2.game.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.game.PvpGame;
import me.cxom.melee2.messaging.Messaging;
import net.punchtree.minigames.utility.player.PlayerProfile;
import net.punchtree.minigames.utility.player.PlayerUtils;

public class Lobby {
	
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
	
	private static final int COUNTDOWN_SECONDS = 5; //10
	
	private final String lobbyName;
	private final PvpGame game;
	private final Location lobbySpawn;
	private final int playersNeededToStart;
	private final Consumer<Set<Player>> startMethod;

	private Map<Player, Boolean> waitingPlayers = new HashMap<>();
	private int playersReady = 0;
	private boolean isCountingDown = false;
	
	private final LobbyControls lobbyControls;
	
//	private LobbyState lobbyState = LobbyState.UNAVAILABLE;
	
	public Lobby(PvpGame game, Consumer<Set<Player>> startMethod) {
		this(game.getName() + " on " + game.getArena().getName(),
			 game,
			 game.getArena().getPregameLobby(),
			 game.getArena().getPlayersNeededToStart(),
			 startMethod);
	}
	
	private Lobby(String lobbyName, PvpGame game, Location lobbySpawn, int playersToStart, Consumer<Set<Player>> startMethod){
		this.lobbyName = lobbyName;
		this.game = game;
		this.lobbySpawn = lobbySpawn;
		this.playersNeededToStart = playersToStart;
		this.startMethod = startMethod;
		
		this.lobbyControls = new LobbyControls(this);
	}
	
	// ---------- GETTERS ----------- //
	public PvpGame getGame() {
		return game;
	}
	
	public Location getSpawn() {
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
	
	public boolean isCountingDown() {
		return isCountingDown;
	}
	
//	public boolean isAvailable() {
//		return lobbyState != LobbyState.UNAVAILABLE;
//	}
	// --------end-getters----------- //
	
	
	public boolean addPlayerIfPossible(Player player){
		if (game.isStopped()) {
			Messaging.send(player, Messaging.ERROR_GAME_STOPPED);
			return false;
		} else if (hasPlayer(player)) {
			Messaging.send(player, Messaging.ERROR_ALREADY_IN_LOBBY);
			return false;
		}
		//Otherwise possible
		addPlayerAndInitialize(player);
		return true;
	}
	
	private void addPlayerAndInitialize(Player player) {
		addPlayerToModel(player);
		teleportPlayerAndSetStats(player);
		lobbyControls.giveLobbyControls(player);
	}
	
	private void addPlayerToModel(Player player) {
		waitingPlayers.put(player, false);
		
		if (enoughPlayersToStartAndWaitingAndNotReady()){
			Messaging.broadcast(getPlayers(), Messaging.LOBBY_ENOUGH_PLAYERS_READY_UP);
		}
	}
	
	private void teleportPlayerAndSetStats(Player player){
		PlayerProfile.save(player);
		
		player.teleport(getSpawn());


		player.setInvulnerable(true); //No PvP in the lobby
		
		//Runnable prevents world settings overriding gamemode change after teleport
		new BukkitRunnable() {
			public void run() {
				player.setGameMode(GameMode.SURVIVAL);
			}
		}.runTaskLater(Melee.getPlugin(), 20);
		player.setFlying(false);
		player.setLevel(0);
		player.setExp(0);
		PlayerUtils.perfectStats(player);
	}
	
	private boolean enoughPlayersToStartAndWaitingAndNotReady() {
		return game.isWaiting() && !isCountingDown()
		 && enoughPlayersToStart()
	     && !halfOfPlayersReady();
	}
	
	private boolean enoughPlayersToStart() {
		return getPlayers().size() >= getPlayersNeededToStart();
	}
	
	private boolean halfOfPlayersReady() {
		return getPlayersReady() > getPlayers().size() / 2d;
	}
	
	private boolean startConditionsMet() {
		return game.isWaiting() && enoughPlayersToStart() && halfOfPlayersReady();
	}
	
	public void startCountdownIfStartConditionsMet() {
		if (startConditionsMet() && !isCountingDown()){
			startCountdown();
		}
	}
	
	public void startCountdown(){
		
		//	TODO refigure how lobby states work for menus
		isCountingDown = true;
		
		new BukkitRunnable(){
			
			int i = COUNTDOWN_SECONDS; 
			
			@Override
			public void run(){
				if (i <= 0){
					attemptStart();
					return;
				}
				countOff();
				i--;
			}
			
			private void countOff() {
				for (Player player : getPlayers()){
					Messaging.MATCH_STARTING_IN(player, i, game.getArena().getName());
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .25f, .9f);
				}
			} 
			
			private void attemptStart() {
				this.cancel();
				
				if (startConditionsMet()) {
					startNow();
				} else {
					Messaging.broadcast(getPlayers(), Messaging.LOBBY_START_CONDITIONS_NO_LONGER_MET);
				}
				
				isCountingDown = false;
			}
			
		}.runTaskTimerAsynchronously(Melee.getPlugin(), 20, 20);
	}
	
	public void startNow(){
		startMethod.accept(getPlayers());
		removeAll();
	}
	
	public void setPlayerReadiness(Player player, boolean ready){
		
		if (! hasPlayer(player)) throw new IllegalArgumentException("Can't set the readiness of a player not in the lobby!");
		
		if (waitingPlayers.get(player) != ready) {
			if (ready) {
				incrementPlayersReady();
			} else {
				decrementPlayersReady();
			}
		}
		
		waitingPlayers.put(player, ready);
	}

	public void removeAndRestorePlayer(Player player){
		if (attemptRemovePlayer(player)){		
			PlayerProfile.restore(player);
		}
	}

	private boolean attemptRemovePlayer(Player player) {
		if (hasReadyPlayer(player)) {
			playersReady--;
		}
		
		Messaging.send(player, Messaging.LOBBY_REMOVING_YOU_FROM, getName());
		
		boolean hp = hasPlayer(player);
		
		waitingPlayers.remove(player);
		return hp;
	}
	
	public void removeAndRestoreAll() {
		getPlayers().forEach(PlayerProfile::restore);
		removeAll();
	}
	
	private void removeAll() {
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
