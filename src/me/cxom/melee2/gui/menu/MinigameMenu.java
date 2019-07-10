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

import me.cxom.melee2.Melee;
import me.cxom.melee2.common.model.GameState;
import me.cxom.melee2.game.PvpGame;
import me.cxom.melee2.game.lobby.Lobby;

public class MinigameMenu implements Listener {

	private static boolean eventsRegistered = false;
	
	private final String menuName;
	
	private List<Lobby> lobbies;
	
	public MinigameMenu(String menuName, Collection<Lobby> lobbies) {
		//this.gameManager = gameManager;
		this.menuName = menuName;
		this.lobbies = new ArrayList<Lobby>(lobbies);
		//FIXME don't do this jank - it doesn't work
		if (!eventsRegistered) {
			Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
			eventsRegistered = true;
		}
	}
	
//	public static final String title = "Melee Games";
	
	public static Inventory createMenu(String menuName, List<Lobby> lobbyList) {
		return new MinigameMenu(menuName, lobbyList).get();
	}
	
	public Inventory get(){
		
//		new MinigameMenu(menuName);
		
		Inventory menu = Bukkit.createInventory(null, (lobbies.size() / 9 + 1) * 9, menuName); 
		
		int slotIndex = 0;
		for (Lobby lobby : lobbies){
			PvpGame game = lobby.getGame();
			
			ItemStack gameMarker = game.getGameState().getMenuItem();
			gameMarker.setAmount(Math.min(64, Math.max(1, lobby.getPlayersWaiting())));
			ItemMeta meta = gameMarker.getItemMeta();
			meta.setDisplayName(ChatColor.BLUE + lobby.getName());
			meta.setLore(Arrays.asList(game.getGameState().getChatColor() + game.getGameState().name(),
									   lobby.getPlayersWaiting() + " player(s) in lobby."));
			gameMarker.setItemMeta(meta);
			
			menu.setItem(slotIndex, gameMarker);
			
			slotIndex++;
		}
		return menu;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		if (clickedInMenu(e)){
			//Prevent picking up an item
			e.setCancelled(true);
			
			if (clickedAValidItem(e.getCurrentItem())){ 
//				String gameName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
				Player playerToAdd = (Player) e.getWhoClicked();
				
				if (clickedOnLobby(e)) {
					Lobby lobbyToAddTo = getClickedLobby(e);
					lobbyToAddTo.addPlayerIfPossible(playerToAdd);
				}
				
//				gameManager.addPlayerToGameLobby(gameName, (Player) e.getWhoClicked());
				
				playerToAdd.closeInventory();
					
			}
		}
	}
	
	private boolean clickedInMenu(InventoryClickEvent e) {
		return e.getClickedInventory() != null
			&& e.getClickedInventory().getName().equals(menuName);
	}
	
	private boolean clickedOnLobby(InventoryClickEvent e) {
		return e.getSlot() < lobbies.size();
	}
	
	private Lobby getClickedLobby(InventoryClickEvent e) {
		return lobbies.get(e.getSlot());
	}
	
	private boolean clickedAValidItem(ItemStack currentItem) {
		return currentItem != null
			&& currentItem.hasItemMeta()
			&& currentItem.getItemMeta().hasLore()
			&& ! GameState.STOPPED.toString().equals(ChatColor.stripColor(currentItem.getItemMeta().getLore().get(1)));
		//TODO i18n : replace toString ^^
	}
	
	//Prevent modifying menu
	@EventHandler
	private void onMenuDrag(InventoryDragEvent e){
		if(e.getInventory().getName().equals(menuName)){
			e.setCancelled(true);
		}
	}
	
}
