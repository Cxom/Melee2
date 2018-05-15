package me.cxom.melee2.gui.menu;

import java.util.Arrays;
import java.util.Collection;

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
import me.cxom.melee2.game.GameInstance;
import me.cxom.melee2.game.GameState;

public class MeleeMenu implements Listener {

	private static final String title = "Melee Games";
	
	public static Inventory getMenu(Collection<GameInstance> games){
		
		Inventory menu = Bukkit.createInventory(null, (games.size() / 9 + 1) * 9, title); 
		
		for (GameInstance game : games){
			
			ItemStack gameMarker = game.getGameState().getMenuItem();
			ItemMeta meta = gameMarker.getItemMeta();
			meta.setDisplayName(ChatColor.BLUE + game.getArena().getName());
			meta.setLore(Arrays.asList(game.getGameState().getChatColor() + game.getGameState().name(),
									   game.getLobby().getWaitingPlayers().size() + " player(s) in lobby."));
			gameMarker.setItemMeta(meta);
			
			menu.addItem(gameMarker);
		}
		return menu;
	}
	
	@EventHandler
	public static void onInventoryClick(InventoryClickEvent e){
		
		//Clicked in the inventory?
		if (e.getClickedInventory() == null) return;
		
		//Clicked in the melee menu?
		if (title.equals(e.getClickedInventory().getName())){
			
			//Prevent picking up an item
			e.setCancelled(true);
			
			//Clicked a valid item
			if (e.getCurrentItem() != null
			 && e.getCurrentItem().hasItemMeta()
			 && e.getCurrentItem().getItemMeta().hasLore()
			 && ! GameState.STOPPED.toString().equals(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(1)))){ //TODO i18n : replace toString
				
				//Get game name and add player
				String game = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				Melee.getGame(game).addPlayer((Player) e.getWhoClicked());
				
				//Close inventory
				e.getWhoClicked().closeInventory();
					
			}
		}
	}
	
	//Prevent modifying menu
	@EventHandler
	private static void onMenuDrag(InventoryDragEvent e){
		if(e.getInventory().getName().equals(title)){
			e.setCancelled(true);
		}
	}
	
}
