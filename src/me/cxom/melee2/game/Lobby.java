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

public class Lobby implements Listener{
	
	private static final ItemStack READY_BLOCK;
	private static final ItemStack NOT_READY_BLOCK;
	static {
		READY_BLOCK = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
		ItemMeta im = READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "" + ChatColor.ITALIC + "READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click if you're",
				                                  "no longer ready!"));
		READY_BLOCK.setItemMeta(im);
		
		NOT_READY_BLOCK = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
		im = NOT_READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.RED + "" + ChatColor.ITALIC + "NOT READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click when you're",
                                                  "ready to play!"));
		NOT_READY_BLOCK.setItemMeta(im);
	}
	
	private GameInstance game;
	private int ready = 0;
	
	public Lobby(GameInstance game){
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
		this.game = game;
		this.ready = 0;
	}
	
	Set<Player> waitingPlayers = new HashSet<>();
	
	public void addPlayer(Player player){
		if (game.getGameState() == GameState.STOPPED){
			player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "This game has been stopped, you cannot join.");
			return;
		}
		waitingPlayers.add(player);
		PlayerProfile.save(player);
		player.teleport(game.getArena().getPregameLobby());
		player.setInvulnerable(true);
		player.setGameMode(GameMode.SURVIVAL);
		player.setFlying(false);
		player.setLevel(0);
		player.setExp(0);
		PlayerUtils.perfectStats(player);
		player.getInventory().clear();
		player.getInventory().setItem(8, NOT_READY_BLOCK);
		if (game.getGameState() == GameState.WAITING && waitingPlayers.size() == game.getArena().getPlayersToStart()){
			if (ready <= waitingPlayers.size() / 2){
				for (Player p : waitingPlayers){
					p.sendMessage(Melee.CHAT_PREFIX + ChatColor.GREEN + "Enough players in the lobby, ready up to start the countdown!");
				}
			}
		}
	}
	
	public boolean removePlayer(Player player){
		if (waitingPlayers.remove(player)){
			PlayerProfile.restore(player);
			return true;
		}
		return false;
	}
	
	public void removeAll(){
		for (Player player : waitingPlayers){
			PlayerProfile.restore(player);
		}
		waitingPlayers.clear();
	}
	
	public Set<Player> getWaitingPlayers(){
		return waitingPlayers;
	}
	
	public void startCountdown(){
		new BukkitRunnable(){
			int i = 10;
			@Override
			public void run(){
				if (i <= 0){
					this.cancel();
					if (game.getGameState() != GameState.STARTING 
							|| waitingPlayers.size() < game.getArena().getPlayersToStart()
							|| ready <= waitingPlayers.size() / 2d){
						for (Player player : waitingPlayers){
							player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "Not enough players in lobby and ready, start aborted!");
						}
						game.setGameState(GameState.WAITING);
					} else {
						startNow();
					}
					return;
				}
				for (Player player : waitingPlayers){
					player.sendMessage(Melee.CHAT_PREFIX + ChatColor.GOLD + "Match starting in " + i + " second(s) on " + game.getArena().getName() + "!");
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .25f, .9f);
				}
				i--;
			}
		}.runTaskTimerAsynchronously(Melee.getPlugin(), 20, 20);
	}
	
	public void startNow(){
		game.start(waitingPlayers);
		waitingPlayers.clear();
		ready = 0;
	}
	
	@EventHandler
	public void onToggleReadiness(PlayerInteractEvent e){
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (READY_BLOCK.equals(e.getItem())){
				ready = Math.max(0, ready - 1);
				e.getPlayer().getInventory().clear();
				e.getPlayer().getInventory().setItem(8, NOT_READY_BLOCK);
				e.setCancelled(true);
			} else if (NOT_READY_BLOCK.equals(e.getItem())) {
				ready = Math.min(waitingPlayers.size(), ready + 1);
				e.getPlayer().getInventory().clear();
				e.getPlayer().getInventory().setItem(8, READY_BLOCK);
				e.setCancelled(true);
				if (game.getGameState() == GameState.WAITING && waitingPlayers.size() >= game.getArena().getPlayersToStart()){
					if (ready > (waitingPlayers.size() / 2d)){
						game.setGameState(GameState.STARTING);
						startCountdown();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDropReadyBlock(PlayerDropItemEvent e){
		if (READY_BLOCK.equals(e.getItemDrop().getItemStack()) || NOT_READY_BLOCK.equals(e.getItemDrop().getItemStack())){
			e.setCancelled(true);
		}
	}
	
}
