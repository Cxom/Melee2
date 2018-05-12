package me.cxom.melee2;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.cxom.melee2.arena.configuration.ArenaManager;
import me.cxom.melee2.game.GameInstance;
import me.cxom.melee2.gui.menu.MeleeMenu;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.InventoryUtils;

public class Melee extends JavaPlugin {

	public static final String CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Melee" + ChatColor.DARK_GREEN + "]" + ChatColor.RESET + " ";
	
	private static Plugin plugin;
	public static Plugin getPlugin(){ return plugin; }
	
	private static Map<String, GameInstance> games = new HashMap<>();
	
	
	@Override
	public void onEnable(){
		
		plugin = this;
		
		//Bukkit.getServer().getPluginManager().registerEvents(new MeleeEventCaller(), getPlugin());
		//Bukkit.getServer().getPluginManager().registerEvents(new CancelledEvents(), getPlugin());
		//Bukkit.getServer().getPluginManager().registerEvents(new CommandEvents(), getPlugin());
		Bukkit.getServer().getPluginManager().registerEvents(new MeleeMenu(), getPlugin());
		
		ArenaManager.loadArenas();
		ArenaManager.getArenas().forEach((arena) -> games.put(arena.getName(), new GameInstance(arena)));
		
	}
	

	@Override
	public void onDisable(){
		games.values().forEach(game -> game.forceStop());
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if ( ! label.equalsIgnoreCase("melee")) return true;
		
		if (! (sender instanceof Player)) return true;
		Player player = (Player) sender;
			
		if (args.length > 0){
		    switch (args[0]) {
		    case "leave":
				//This is preprocessed in MeleeInstance, and cancelled if it goes through.
		    	//Good chance that it will be changed to just be completely br
		    	player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "You're not in a game!");
//						} else {
//							player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "" + ChatColor.ITALIC 
//									+ "Removing you from " + lobby + " lobby . . .");
//							if (getLobby(lobby) != null) {
//								getLobby(lobby).removePlayer(player);
//							}
//							deregisterLobbier(player);
						
//						}
//						player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "" + ChatColor.ITALIC
//								+ "Removing you from " + mp.getGameName() + " . . ."); Move to remove methods
				
				return true;
		    case "join":
				if (args.length < 2) {
					player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "/melee join <arena> (or just type /melee)");
				} else if (! games.containsKey(args[1])){
					player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + " There is no game/arena named " + args[1] + "!");
				} else {
					getGame(args[1]).addPlayer(player);
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
		    		player.sendMessage("Games: " + games);
		    	} else {
		    		getGame(args[1]).debug(player);
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
		
		((Player) sender).openInventory(MeleeMenu.getMenu());
		
		return true;
	}
	
	public static GameInstance getGame(String name){
		return games.get(name);
	}
	
	public static Map<String, GameInstance> getGameMap(){
		return games;
	}
	
}
