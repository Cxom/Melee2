package me.cxom.melee2.gui.menu;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.arena.configuration.ArenaManager;
import me.cxom.melee2.game.GameInstance;
import me.cxom.melee2.game.GameState;

public class MeleeMenu implements Listener {

	private static final String title = "Melee Games";
	
	public static Inventory getMenu(){
		
		Inventory menu = Bukkit.createInventory(null, (Melee.getGameMap().size() / 9 + 1) * 9, title); 
		
		for (MeleeArena arena : ArenaManager.getArenas()){
		
			GameInstance game = Melee.getGame(arena.getName());
			
			ItemStack gameMarker = game.getGameState().getMenuItem();
			ItemMeta meta = gameMarker.getItemMeta();
			meta.setDisplayName(ChatColor.BLUE + arena.getName());
			meta.setLore(Arrays.asList(game.getGameState().getChatColor() + game.getGameState().name(),
									   game.getLobby().getWaitingPlayers().size() + " player(s) in lobby."));
			gameMarker.setItemMeta(meta);
			
			menu.addItem(gameMarker);
		}
		return menu;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		
		if (e.getClickedInventory() == null) return;
		
		if (title.equals(e.getClickedInventory().getName())){
			
			e.setCancelled(true);
			
			if (e.getCurrentItem() != null
			 && e.getCurrentItem().hasItemMeta()
			 && e.getCurrentItem().getItemMeta().hasLore()
			 && ! GameState.STOPPED.toString().equals(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(1)))){ //TODO i18n : replace toString
					
				String game = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				Melee.getGame(game).addPlayer((Player) e.getWhoClicked());
				e.getWhoClicked().closeInventory();
					
			}
		}
	}
	
	@EventHandler
	public void onMenuDrag(InventoryDragEvent e){
		if(e.getInventory().getName().equals(title)){
			e.setCancelled(true);
		}
	}
	
}
