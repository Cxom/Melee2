package me.cxom.melee2;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.cxom.melee2.arena.configuration.ArenaManager;
import me.cxom.melee2.game.lobby.Lobby;
import me.cxom.melee2.gui.menu.MeleeMenu;
import me.cxom.melee2.gui.menu.MinigameMenu;
import me.cxom.melee2.gui.menu.RabbitMenu;
import me.cxom.melee2.util.InventoryUtils;
import me.cxom.melee2.util.PlayerProfile;

public class Melee extends JavaPlugin {

	// Responsibility: Hold globals, handle plugin starting and stopping, route commands
	
	public static final String MELEE_CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Melee" + ChatColor.DARK_GREEN + "]" + ChatColor.RESET + " ";
	public static final String RABBIT_CHAT_PREFIX = ChatColor.GOLD + "[" + ChatColor.WHITE + "Rabbit" + ChatColor.GOLD + "]" + ChatColor.RESET + " ";
	
	private static Plugin plugin;
	public static Plugin getPlugin(){ return plugin; }
	
	


	
	
	@Override
	public void onEnable(){
		
		plugin = this;
		
		// Register Events
		Bukkit.getServer().getPluginManager().registerEvents(new MeleeMenu(), getPlugin());
		Bukkit.getServer().getPluginManager().registerEvents(new RabbitMenu(), getPlugin());
		
		// Load arenas and create a game for each
		ArenaManager.loadMeleeArenas();
		ArenaManager.loadRabbitArenas();
		
		System.out.print("Loaded Melee Arenas: ");
		ArenaManager.getMeleeArenas().forEach(a -> System.out.print(a.getName() + " "));
		System.out.println();
		
		System.out.print("Loaded Rabbit Arenas: ");
		ArenaManager.getRabbitArenas().forEach(a -> System.out.print(a.getName()+ " "));
		System.out.println();
		
		MeleeGameManager.createAllGames();
		
		RabbitGameManager.createAllGames();
	}
	

	@Override
	public void onDisable(){
		MeleeGameManager.stopAllGames();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if (label.equalsIgnoreCase("rabbit")) {
			if (! (sender instanceof Player)) return true;
			Player player = (Player) sender;
			
			player.openInventory(RabbitGameManager.getMenu());			
		}
		
		if (label.equalsIgnoreCase("games") || label.equalsIgnoreCase("join")) {
			if (! (sender instanceof Player)) return true;
			Player player = (Player) sender;
			
			List<Lobby> allLobbies = new ArrayList<Lobby>();
			allLobbies.addAll(MeleeGameManager.getLobbyList());
			allLobbies.addAll(RabbitGameManager.getLobbyList());
			
			Inventory allLobbiesMenu = MinigameMenu.createMenu("All Games", allLobbies);
			player.openInventory(allLobbiesMenu);
		}
		
		if ( ! label.equalsIgnoreCase("melee")) return true;
		
		if (! (sender instanceof Player)) return true;
		Player player = (Player) sender;
			
		if (args.length > 0){
		    switch (args[0]) {
		    case "leave":
				/*
				 * This is preprocessed in MeleeInstance in order to determine the game,
				 * and cancelled if it goes through.
				 */
		    	player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + "You're not in a game!");
				
				return true;
		    case "join":
				if (args.length < 2) {
					player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + "/melee join <arena> (or just type /melee)");
				} else if (! MeleeGameManager.hasGame(args[1])){
					player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + " There is no game/arena named " + args[1] + "!");
				} else {
					MeleeGameManager.addPlayerToGameLobby(args[1], player);
				}
				return true;
				
				
			// debug commands
		    case "backup":
				InventoryUtils.backupInventory(player);
				return true;
		    case "restore":
				if (! (player).isOp()) return true;
				InventoryUtils.restoreBackupInventory(Bukkit.getOfflinePlayer(args[1]).getUniqueId(), player);
				return true;
		    case "debug":
		    	if (args.length <= 1) {
		    		MeleeGameManager.debugGamesList(player);
		    	} else {
		    		MeleeGameManager.debugGame(args[1], player);
		    	} 
		    	return true;
		    
		    //default just won't return --> opens the /melee menu
		    }
		    
		}
		
		if (PlayerProfile.isSaved(player)) {
			//Don't do this at home. This was done by trained unprofessionals with no supervision.
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/mail Cxomtdoh " + player.getName() + " tried to join with a saved inventory.");
			return true;
		}
		
		((Player) sender).openInventory(MeleeGameManager.getMenu());
		
		return true;
	}
	
//	public static MeleeGame getGame(String name){
//		return games.get(name);
//	}
	
}
