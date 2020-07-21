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
import net.punchtree.minigames.game.GameState;
import net.punchtree.minigames.game.PvpGame;
import net.punchtree.minigames.lobby.Lobby;

public class MinigameMenu implements Listener {
	
	private final String menuName;
	private List<Lobby> lobbies;
	
	private final Inventory menu;
	
	public MinigameMenu(String menuName, Collection<Lobby> lobbies) {
		//this.gameManager = gameManager;
		this.menuName = menuName;
		this.lobbies = new ArrayList<Lobby>(lobbies);
		
		this.menu = Bukkit.createInventory(null, (lobbies.size() / 9 + 1) * 9, menuName); 
		constructMenu();
		
		// Will this leak memory? - Regardless, menu objects are meant to be SAVED AND REUSED
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	private void constructMenu(){
		
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
	}
	
	public void refresh() {
		// TODO - change inventory size if a new lobby is added - when doing this, make sure this lobby never overflows (iteration limit)
		Bukkit.broadcastMessage("Refreshing Menu");
		menu.clear();
		constructMenu();
		Bukkit.broadcastMessage(ChatColor.GRAY + "Refreshed Menu");
	}
	
	public void showTo(Player player) {
		refresh();
		player.openInventory(menu);
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
				refresh();
			}
		}
	}
	
	private boolean clickedInMenu(InventoryClickEvent e) {
		return e.getClickedInventory() != null
			&& e.getView().getTitle().equals(menuName);
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
		if(e.getView().getTitle().equals(menuName)){
			e.setCancelled(true);
		}
	}
	
}
