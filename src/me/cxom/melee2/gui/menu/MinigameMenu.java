package me.cxom.melee2.gui.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

import me.cxom.melee2.common.model.GameState;
import me.cxom.melee2.game.PvpGame;
import me.cxom.melee2.game.lobby.Lobby;

public class MinigameMenu implements Listener {

	private final String menuName;
	
	private List<Lobby> lobbies;
	
	public MinigameMenu(String menuName, Collection<Lobby> lobbies) {
		//this.gameManager = gameManager;
		this.menuName = menuName;
		this.lobbies = new ArrayList<Lobby>(lobbies);
	}
	
//	public static final String title = "Melee Games";
	
	public static Inventory createMenu(String menuName, List<Lobby> lobbyList) {
		return new MinigameMenu(menuName, lobbyList).get();
	}
	
	public Inventory get(){
		
//		new MinigameMenu(menuName);
		
		Inventory menu = Bukkit.createInventory(null, (lobbies.size() / 9 + 1) * 9, menuName); 
		
		for (Lobby lobby : lobbies){
			PvpGame game = lobby.getGame();
			
			ItemStack gameMarker = game.getGameState().getMenuItem();
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
	public void onInventoryClick(InventoryClickEvent e){
		
		//Clicked in the inventory?
		if (e.getClickedInventory() == null) return;
		
		//Clicked in the melee menu?
		if (menuName.equals(e.getClickedInventory().getName())){
			
			//Prevent picking up an item
			e.setCancelled(true);
			
			//Clicked a valid item
			if (e.getCurrentItem() != null
			 && e.getCurrentItem().hasItemMeta()
			 && e.getCurrentItem().getItemMeta().hasLore()
			 && ! GameState.STOPPED.toString().equals(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(1)))){ //TODO i18n : replace toString
				
				//Get game name and add player
				String gameName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				
				if (lobbies.size() - 1 <= e.getSlot()) {
					lobbies.get(e.getSlot()).addPlayerIfPossible((Player) e.getWhoClicked());
				}
//				gameManager.addPlayerToGameLobby(gameName, (Player) e.getWhoClicked());
				
				//Close inventory
				e.getWhoClicked().closeInventory();
					
			}
		}
	}
	
	//Prevent modifying menu
	@EventHandler
	private void onMenuDrag(InventoryDragEvent e){
		if(e.getInventory().getName().equals(menuName)){
			e.setCancelled(true);
		}
	}
	
}
