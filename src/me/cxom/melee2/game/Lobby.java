package me.cxom.melee2.game;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

//This class is effectively a subclass of MeleeInstance, in a separate file for readability
public class Lobby implements Listener{
	
	
	//static variables
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
	
	
	//instance variables
	GameInstance game;
	private int ready = 0;
	
	Set<Player> waitingPlayers = new HashSet<>();
	
	Lobby(GameInstance game){
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
		this.game = game;
	}
	
	public void addPlayer(Player player){
		
		if (game.getGameState() == GameState.STOPPED){
			
			player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "This game has been stopped, you cannot join.");

		} else {
			
			waitingPlayers.add(player);
			
			player.teleport(game.getArena().getPregameLobby());
			
			player.getInventory().clear();
			player.getInventory().setItem(8, NOT_READY_BLOCK);
			
			player.setInvulnerable(true);
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
			
			if (game.getGameState() == GameState.WAITING 
			 && waitingPlayers.size() == game.getArena().getPlayersNeededToStart()
		     && ready <= waitingPlayers.size() / 2){
				waitingPlayers.forEach(p -> p.sendMessage(Melee.CHAT_PREFIX + ChatColor.GREEN + "Enough players in the lobby, ready up to start the countdown!"));
			}
		}
	}
	
	public boolean removePlayer(Player player){
		if (waitingPlayers.remove(player)){
			if (player.getInventory().getItem(8) == NOT_READY_BLOCK) {
				ready--;
			}
			PlayerProfile.restore(player);
			return true;
		} else {
			return false;
		}
	}
	
	public void removeAll(){
		waitingPlayers.forEach(PlayerProfile::restore);
		reset();
	}
	
	public Set<Player> getWaitingPlayers(){
		return waitingPlayers;
	}
	
	public void startCountdown(){
		
		game.setGameState(GameState.STARTING);
		
		new BukkitRunnable(){
			
			int i = 5; //10
			
			@Override
			public void run(){
				if (i <= 0){
					attemptStart();
					return;
				}
				for (Player player : waitingPlayers){
					player.sendMessage(Melee.CHAT_PREFIX + ChatColor.GOLD + "Match starting in " + i + " second(s) on " + game.getArena().getName() + "!");
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .25f, .9f);
				}
				i--;
			}
			
			private void attemptStart() {
				this.cancel();
				
				if (game.getGameState() != GameState.STARTING 
				 || waitingPlayers.size() < game.getArena().getPlayersNeededToStart()
		   		 || ready <= waitingPlayers.size() / 2d){
					for (Player player : waitingPlayers){
						player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "Not enough players in lobby and ready, start aborted!");
					}
					game.setGameState(GameState.WAITING);
				} else {
					startNow();
				}
			}
			
		}.runTaskTimerAsynchronously(Melee.getPlugin(), 20, 20);
	}
	
	public void startNow(){
		game.start(waitingPlayers);
		reset();
	}
	
	private void reset() {
		waitingPlayers.clear();
		ready = 0;
	}
	
	@EventHandler
	public void onToggleReadiness(PlayerInteractEvent e){
		//TODO Fix being able to move ready block in inventory?
		if(! waitingPlayers.contains(e.getPlayer())) return;
		
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			if (READY_BLOCK.equals(e.getItem())){
				ready = Math.max(0, ready - 1);
				e.getPlayer().getInventory().setItem(8, NOT_READY_BLOCK);
				e.setCancelled(true);
			} else if (NOT_READY_BLOCK.equals(e.getItem())) {
				ready = Math.min(waitingPlayers.size(), ready + 1);
				e.getPlayer().getInventory().setItem(8, READY_BLOCK);
				e.setCancelled(true);
				if (game.getGameState() == GameState.WAITING 
				 && waitingPlayers.size() >= game.getArena().getPlayersNeededToStart()
				 && ready > (waitingPlayers.size() / 2d)){
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
