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

import me.cxom.melee2.RabbitGameManager;
import net.punchtree.minigames.game.GameState;
import net.punchtree.minigames.game.PvpGame;
import net.punchtree.minigames.lobby.Lobby;

public class RabbitMenu implements Listener {
	
public static final String title = "Rabbit Games";
	
	public static Inventory createMenu(Collection<Lobby> lobbies){
		
//		new MinigameMenu(menuName);
		
		Inventory menu = Bukkit.createInventory(null, (lobbies.size() / 9 + 1) * 9, title); 
		
		for (Lobby lobby : lobbies){
			PvpGame game = lobby.getGame();
			
			ItemStack gameMarker = game.getGameState().getMenuItem();
			gameMarker.setAmount(Math.min(64, Math.max(1, lobby.getPlayersWaiting())));
			ItemMeta meta = gameMarker.getItemMeta();
			meta.setDisplayName(ChatColor.BLUE + game.getArena().getName());
			meta.setLore(Arrays.asList(game.getGameState().getChatColor() + game.getGameState().name(),
									   lobby.getPlayersWaiting() + " player(s) in lobby."));
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
		if (title.equals(e.getView().getTitle())){
			
			//Prevent picking up an item
			e.setCancelled(true);
			
			//Clicked a valid item
			if (e.getCurrentItem() != null
			 && e.getCurrentItem().hasItemMeta()
			 && e.getCurrentItem().getItemMeta().hasLore()
			 && ! GameState.STOPPED.toString().equals(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(1)))){ //TODO i18n : replace toString
				
				//Get game name and add player
				String game = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				RabbitGameManager.addPlayerToGameLobby(game, (Player) e.getWhoClicked());
				
				//Close inventory
				e.getWhoClicked().closeInventory();
					
			}
		}
	}
	
	//Prevent modifying menu
	@EventHandler
	private static void onMenuDrag(InventoryDragEvent e){
		if(e.getView().getTitle().equals(title)){
			e.setCancelled(true);
		}
	}
	
	
}
