package me.cxom.melee2;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.arena.configuration.MeleeAndRabbitArenaLoader;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.game.rabbit.RabbitGame;
import net.punchtree.minigames.arena.creation.ArenaManager;
import net.punchtree.minigames.game.GameManager;
import net.punchtree.minigames.menu.MinigameMenu;
import net.punchtree.minigames.utility.player.InventoryUtils;
import net.punchtree.minigames.utility.player.PlayerProfile;

public class MeleeAndRabbitCommandExecutor implements CommandExecutor {

	private static final String MELEE_ADMIN_PERMISSION = "melee.admin";
	
	private final GameManager<RabbitGame> rabbitGameManager;
	private final GameManager<MeleeGame> meleeGameManager;
	private final MinigameMenu allLobbiesMenu;
	private final ArenaManager<MeleeArena> meleeArenaManager;
	private final Logger logger;
	private final File meleeArenaFolder;
	
	public MeleeAndRabbitCommandExecutor(GameManager<RabbitGame> rabbitGameManager, GameManager<MeleeGame> meleeGameManager, 
			MinigameMenu allLobbiesMenu, ArenaManager<MeleeArena> meleeArenaManager, Logger logger, File meleeArenaFolder) {
		this.rabbitGameManager = rabbitGameManager;
		this.meleeGameManager = meleeGameManager;
		this.allLobbiesMenu = allLobbiesMenu;
		this.meleeArenaManager = meleeArenaManager;
		this.logger = logger;
		this.meleeArenaFolder = meleeArenaFolder;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if (! (sender instanceof Player)) return true;
		Player player = (Player) sender;
		
		if (label.equalsIgnoreCase("rabbit")) {
			rabbitGameManager.showMenuTo(player);	
			return true;
		}
		
		if (label.equalsIgnoreCase("games")) {		
			allLobbiesMenu.showTo(player);
			return true;
		}
		
		if ( ! label.equalsIgnoreCase("melee")) return true;
			
		if (args.length > 0 && "join".equalsIgnoreCase(args[0])){
			if (args.length < 2) {
				player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + "/melee join <arena> (or just type /melee)");
			} else if ( ! meleeGameManager.hasGame(args[1])){
				player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + " There is no game/arena named " + args[1] + "!");
			} else {
				meleeGameManager.addPlayerToGameLobby(args[1], player);
			}
			return true;
		}
		
		if (args.length > 0) {
			if (!sender.hasPermission(MELEE_ADMIN_PERMISSION)) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
				return true;
			}
		    switch (args[0]) {
		    case "convert":
		    	if (args.length == 2) {
		    		String arenaName = args[1];
		    		MeleeArena meleeArena = meleeArenaManager.getArena(arenaName);
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

		    //default just won't return --> opens the /melee menu
		    }
		}
		
		if (PlayerProfile.isSaved(player)) {
			logger.severe(player.getName() + " tried to join a game but has a saved inventory!");
			return true;
		}
		
		meleeGameManager.showMenuTo((Player) sender);
		
		return true;

	}
	
	private void convertMeleeArena(Location relative, MeleeArena meleeArena) {
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

}
