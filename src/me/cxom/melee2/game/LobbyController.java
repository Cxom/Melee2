package me.cxom.melee2.game;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.PlayerUtils;

public class LobbyController implements Listener {
		
	//TODO FIGURE OUT HOW WE WANT GAMESTATE TO WORK
	
	private final Lobby lobby;
	private final Consumer<Set<Player>> gameStart;
	private final MeleeGame game;
	
	public LobbyController(Lobby lobby, Consumer<Set<Player>> gameStart, MeleeGame game) {
		this.lobby = lobby;
		this.gameStart = gameStart;
		this.game = game;
		
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	public boolean addPlayerToLobby(Player player) {
		
		if (game.getGameState() == GameState.STOPPED) {
			player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "This game has been stopped, you cannot join.");
			return false;
		}
		
		// Update model
		lobby.addPlayer(player);
		
		// Everything else
		player.teleport(lobby.getSpawn());
		
		player.getInventory().clear();
		player.getInventory().setItem(8, NOT_READY_BLOCK);
		
		//No PvP in the lobby!
		player.setInvulnerable(true);
		
		//Set the Gamemode to survival and reset other stats
		//Runnable prevents world settings overriding gamemode after teleport
		new BukkitRunnable() {
			public void run() {
				player.setGameMode(GameMode.SURVIVAL);
			}
		}.runTaskLater(Melee.getPlugin(), 20);
		player.setFlying(false);
		player.setLevel(0);
		player.setExp(0);
		PlayerUtils.perfectStats(player);
		
		//If the lobby's still waiting and we have enough ready players, start the countdown
		if (game.getGameState() == GameState.WAITING 
				 && lobby.getPlayers().size() == lobby.getPlayersNeededToStart()
			     && lobby.getPlayersReady() <= lobby.getPlayers().size() / 2){
					
			lobby.getPlayers().forEach(p -> p.sendMessage(Melee.CHAT_PREFIX + ChatColor.GREEN + "Enough players in the lobby, ready up to start the countdown!"));
		}
		
		return true;
		
	}
	
	public void removePlayerFromLobby(Player player) {

		lobby.removePlayer(player);
		
		player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "" + ChatColor.ITALIC 
				+ "Removing you from " + lobby.getName() + " lobby . . .");
		
		PlayerProfile.restore(player);
		
	}
	
	// --------- HELPERS ---------- //
	
	private void broadcast(String message) {
		lobby.getPlayers().forEach(p -> p.sendMessage(message));
	}
	
	private boolean isStartConditionsMet() {
		return (game.isAvailable()
			 && lobby.getPlayersWaiting() >= lobby.getPlayersNeededToStart()
		   	 && lobby.getPlayersReady() > (lobby.getPlayersWaiting() / 2d));
	}
	
	//---------end-helpers--------- //
	
	
	public void startCountdown(){
		
		game.setState(GameState.STARTING);
		
		new BukkitRunnable(){
			
			int i = 5; //10
			
			@Override
			public void run(){
				if (i <= 0){
					attemptStart();
					return;
				}
				for (Player player : lobby.getPlayers()){
					player.sendMessage(Melee.CHAT_PREFIX + ChatColor.GOLD + "Match starting in " + i + " second(s) on " + game.getArena().getName() + "!");
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .25f, .9f);
				}
				i--;
			}
			
			private void attemptStart() {
				this.cancel();
				
				if (isStartConditionsMet()) {
					
					startNow();
	
				} else /*start conditions not met */ {
	
					broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "Not enough players in lobby and ready, start aborted!");
					game.setState(GameState.WAITING);
				
				}
			}
			
		}.runTaskTimerAsynchronously(Melee.getPlugin(), 20, 20);
	}
	
	public void startNow(){
		gameStart.accept(lobby.getPlayers());
		lobby.clear();
	}

	public void removeAllFromLobby() {
		lobby.getPlayers().forEach(PlayerProfile::restore);
		lobby.clear();
	}
	
	private static final ItemStack READY_BLOCK;
	private static final ItemStack NOT_READY_BLOCK;
	static {
		//Initializing the item meta for READY_BLOCK
		READY_BLOCK = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
		ItemMeta im = READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "" + ChatColor.ITALIC + "READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click if you're",
				                                  "no longer ready!"));
		READY_BLOCK.setItemMeta(im);
		
		//Initializing the item meta for NOT_READY_BLOCK
		NOT_READY_BLOCK = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
		im = NOT_READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.RED + "" + ChatColor.ITALIC + "NOT READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click when you're",
                                                  "ready to play!"));
		NOT_READY_BLOCK.setItemMeta(im);
	}
	
	
	@EventHandler
	public void onToggleReadiness(PlayerInteractEvent e){
		
		Player player = e.getPlayer();
		//TODO Fix being able to move ready block in inventory?
		if(! lobby.hasPlayer(player)) return;
		
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			if (READY_BLOCK.equals(e.getItem())){
				lobby.setPlayerReadiness(player, false);
				e.getPlayer().getInventory().setItem(8, NOT_READY_BLOCK);
				e.setCancelled(true);
			} else if (NOT_READY_BLOCK.equals(e.getItem())) {
				lobby.setPlayerReadiness(player, true);
				e.getPlayer().getInventory().setItem(8, READY_BLOCK);
				e.setCancelled(true);
				
				if (isStartConditionsMet() && game.getGameState() != GameState.STARTING){
					startCountdown();
				}
				 
			}
			
		}
	}
	
	//Cancels dropping the readiness indicator
	@EventHandler
	public void onDropReadyBlock(PlayerDropItemEvent e){
		if (READY_BLOCK.equals(e.getItemDrop().getItemStack()) || NOT_READY_BLOCK.equals(e.getItemDrop().getItemStack())){
			e.setCancelled(true);
		}
	}

	
	

	
}
