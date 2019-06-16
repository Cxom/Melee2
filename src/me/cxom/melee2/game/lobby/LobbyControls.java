package me.cxom.melee2.game.lobby;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.cxom.melee2.Melee;

public class LobbyControls implements Listener {
	
	private static final ItemStack READY_BLOCK;
	private static final ItemStack NOT_READY_BLOCK;
	private static final ItemStack LEAVE_LOBBY;
	static {
		//Initializing the item meta for READY_BLOCK
		READY_BLOCK = new ItemStack(Material.GREEN_TERRACOTTA);
		ItemMeta im = READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "" + ChatColor.ITALIC + "READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click if you're",
				                                  "no longer ready!"));
		READY_BLOCK.setItemMeta(im);
		
		//Initializing the item meta for NOT_READY_BLOCK
		NOT_READY_BLOCK = new ItemStack(Material.RED_TERRACOTTA);
		im = NOT_READY_BLOCK.getItemMeta();
		im.setDisplayName(ChatColor.RED + "" + ChatColor.ITALIC + "NOT READY");
		im.setLore(Arrays.asList(ChatColor.GRAY + "Right click when you're",
                                                  "ready to play!"));
		NOT_READY_BLOCK.setItemMeta(im);
		
		LEAVE_LOBBY = new ItemStack(Material.BARRIER);
		im = LEAVE_LOBBY.getItemMeta();
		im.setDisplayName(ChatColor.RED + "Leave the lobby");
		LEAVE_LOBBY.setItemMeta(im);
	}
	
	private final Lobby lobby;
	
	public LobbyControls(Lobby lobby) {
		this.lobby = lobby;
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	public void giveLobbyControls(Player player) {
		player.getInventory().clear();
		player.getInventory().setItem(0, NOT_READY_BLOCK);
		player.getInventory().setItem(8, LEAVE_LOBBY);
	}
	
	@EventHandler
	public void onToggleReadiness(PlayerInteractEvent e){
		
		Player player = e.getPlayer();
		//TODO Fix being able to move ready block in inventory?
		if(! lobby.hasPlayer(player)) return;
		
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			if (LEAVE_LOBBY.equals(e.getItem())) {
				lobby.removeAndRestorePlayer(e.getPlayer());
				e.setCancelled(true);
			} else if (READY_BLOCK.equals(e.getItem())){
				lobby.setPlayerReadiness(player, false);
				e.getPlayer().getInventory().setItem(8, NOT_READY_BLOCK);
				e.setCancelled(true);
			} else if (NOT_READY_BLOCK.equals(e.getItem())) {
				lobby.setPlayerReadiness(player, true);
				e.getPlayer().getInventory().setItem(8, READY_BLOCK);
				e.setCancelled(true);
				
				lobby.startCountdownIfStartConditionsMet();
				 
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
