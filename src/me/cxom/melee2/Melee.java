package me.cxom.melee2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.arena.configuration.MeleeAndRabbitArenaLoader;
import me.cxom.melee2.gui.menu.MinigameMenu;
import net.punchtree.minigames.lobby.Lobby;
import net.punchtree.minigames.utility.player.InventoryUtils;
import net.punchtree.minigames.utility.player.PlayerProfile;

public class Melee extends JavaPlugin {

	// Responsibility: Hold globals, handle plugin starting and stopping, route commands
	
	public static final String MELEE_CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Melee" + ChatColor.DARK_GREEN + "]" + ChatColor.RESET + " ";
	public static final String RABBIT_CHAT_PREFIX = ChatColor.GOLD + "[" + ChatColor.WHITE + "Rabbit" + ChatColor.GOLD + "]" + ChatColor.RESET + " ";
	
	private static Plugin plugin;
	public static Plugin getPlugin(){ return plugin; }
	
	static File meleeArenaFolder;
	static File rabbitArenaFolder;
	
	private MinigameMenu allLobbiesMenu;
	
	@Override
	public void onEnable(){
		
		// Initialize plugin stuff
		plugin = this;
		meleeArenaFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Arenas");
		rabbitArenaFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "RabbitArenas");

		// Register Events
		// - None global right now
		
		// Load arenas and create a game for each
		
		MeleeGameManager.createAllGames();
		RabbitGameManager.createAllGames();
		
		System.out.print("Created Melee Games: ");
		MeleeGameManager.getGamesList().forEach(a -> System.out.print(a.getName() + " "));
		System.out.println();
		
		System.out.print("Loaded Rabbit Games: ");
		RabbitGameManager.getGamesList().forEach(a -> System.out.print(a.getName()+ " "));
		System.out.println();
	
		List<Lobby> allLobbies = new ArrayList<Lobby>();
		allLobbies.addAll(MeleeGameManager.getLobbyList());
		allLobbies.addAll(RabbitGameManager.getLobbyList());
		allLobbiesMenu = new MinigameMenu("All Games", allLobbies);
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
			
			RabbitGameManager.showMenuTo(player);	
			return true;
		}
		
		if (label.equalsIgnoreCase("games") || label.equalsIgnoreCase("join")) {
			if (! (sender instanceof Player)) return true;
			Player player = (Player) sender;
			
			allLobbiesMenu.showTo(player);
			return true;
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
				
		    case "convert":
		    	if (args.length == 2) {
		    		String arenaName = args[1];
		    		MeleeArena meleeArena = MeleeGameManager.meleeArenaManager.getArena(arenaName);
		    		if (arenaName != null) {
		    			convertMeleeArena(player.getLocation().getBlock().getLocation(), meleeArena);
		    			player.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "Converted " + arenaName);
		    		}
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
		
		MeleeGameManager.showMenuTo((Player) sender);
		
		return true;
	}
	
	private static void convertMeleeArena(Location relative, MeleeArena meleeArena) {
		File arenaf = new File(meleeArenaFolder + File.separator + meleeArena.getName() + "-relative" + ".yml");
		
		try {
			if (! arenaf.exists()) {
				arenaf.createNewFile();
			}
			FileConfiguration arenacfg = new YamlConfiguration();
			
			arenacfg.load(arenaf);
			
			MeleeAndRabbitArenaLoader.convertMeleeArena(relative, meleeArena, arenacfg);
			
			arenacfg.save(arenaf);
			
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
//	public static MeleeGame getGame(String name){
//		return games.get(name);
//	}
	
}
