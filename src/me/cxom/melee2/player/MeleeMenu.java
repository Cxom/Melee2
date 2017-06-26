package me.cxom.melee2.player;

import java.util.Arrays;
import java.util.Map;

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

public class MeleeMenu implements Listener {

	private static final String title = "Melee Games";
	
	public static Inventory getMenu(){
		Map<String, GameInstance> games = Melee.getGameMap();
		Inventory menu = Bukkit.createInventory(null, (games.size() / 9 + 1) * 9, title); 
		for (MeleeArena arena : ArenaManager.getArenas()){
			GameInstance game = Melee.getGame(arena.getName());
			ItemStack gameMarker = game.getGameState().getMenuItem();
			ItemMeta meta = gameMarker.getItemMeta();
			meta.setDisplayName(ChatColor.BLUE + arena.getName());
			meta.setLore(Arrays.asList(game.getGameState().getChatColor() + game.getGameState().name(),
									   Melee.getLobby(arena.getName()).getWaitingPlayers().size() + " player(s) in lobby."));
			gameMarker.setItemMeta(meta);
			menu.addItem(gameMarker);
		}
		return menu;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		if (e.getClickedInventory() == null) return;
		if (title.equals(e.getClickedInventory().getName())){
			if (e.getCurrentItem() != null
			 && e.getCurrentItem().hasItemMeta()
			 && e.getCurrentItem().getItemMeta().hasLore()){
				ItemStack clicked = e.getCurrentItem();
				if(! ChatColor.stripColor(clicked.getItemMeta().getLore().get(1)).equals("STOPPED")){
					String game = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
					Melee.addLobbier((Player) e.getWhoClicked(), game);
					e.getWhoClicked().closeInventory();
				}
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onMenuDrag(InventoryDragEvent e){
		if(e.getInventory().getName().equals(title)){
			e.setCancelled(true);
		}
	}
	
}
