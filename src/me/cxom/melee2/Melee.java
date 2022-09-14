package me.cxom.melee2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import me.cxom.melee2.arena.RabbitArena;
import me.cxom.melee2.arena.configuration.MeleeAndRabbitArenaLoader;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.game.rabbit.RabbitGame;
import net.punchtree.minigames.arena.creation.ArenaManager;
import net.punchtree.minigames.game.GameManager;
import net.punchtree.minigames.lobby.Lobby;
import net.punchtree.minigames.menu.MinigameMenu;
import net.punchtree.minigames.utility.player.InventoryUtils;
import net.punchtree.minigames.utility.player.PlayerProfile;

public class Melee extends JavaPlugin {

	// Responsibility: Hold globals, handle plugin starting and stopping, route commands
	
	public static final String MELEE_CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Melee" + ChatColor.DARK_GREEN + "]" + ChatColor.RESET + " ";
	public static final String RABBIT_CHAT_PREFIX = ChatColor.GOLD + "[" + ChatColor.WHITE + "Rabbit" + ChatColor.GOLD + "]" + ChatColor.RESET + " ";
	
	private static Plugin plugin;
	public static Plugin getPlugin(){ return plugin; }
	
	private File meleeArenaFolder;
	private File rabbitArenaFolder;
	
	private ArenaManager<MeleeArena> meleeArenaManager;
	private ArenaManager<RabbitArena> rabbitArenaManager;
	
	private GameManager<MeleeGame> meleeGameManager;
	private GameManager<RabbitGame> rabbitGameManager;
	
	private MinigameMenu allLobbiesMenu;
	
	private MeleeAndRabbitCommandExecutor commandExecutor;
	
	@Override
	public void onEnable(){
		
		// Initialize plugin stuff
		plugin = this;
		meleeArenaFolder = new File(getDataFolder().getAbsolutePath() + File.separator + "Arenas");
		rabbitArenaFolder = new File(getDataFolder().getAbsolutePath() + File.separator + "RabbitArenas");

		meleeArenaManager = new ArenaManager<>(meleeArenaFolder, MeleeAndRabbitArenaLoader::loadMeleeArena);
		rabbitArenaManager = new ArenaManager<>(rabbitArenaFolder, MeleeAndRabbitArenaLoader::loadRabbitArena);
		
		meleeGameManager = new GameManager<>("Melee Games");
		rabbitGameManager = new GameManager<>("Rabbit Games");
		
		// TODO Register Events smarter

		createAllGames();
	
		List<Lobby> allLobbies = new ArrayList<Lobby>();
		allLobbies.addAll(meleeGameManager.getLobbyList());
		allLobbies.addAll(rabbitGameManager.getLobbyList());
		allLobbiesMenu = new MinigameMenu("All Games", allLobbies);
		
		commandExecutor = new MeleeAndRabbitCommandExecutor(rabbitGameManager, meleeGameManager, allLobbiesMenu, meleeArenaManager, getLogger(), meleeArenaFolder);
		getCommand("melee").setExecutor(commandExecutor);
		getCommand("rabbit").setExecutor(commandExecutor);
		getCommand("games").setExecutor(commandExecutor);
	}
	
	/**
	 * Load arenas and create a game for each
	 */
	private void createAllGames() {
		// Create a melee game for each melee arena
		meleeArenaManager.loadArenas();
		meleeArenaManager.getArenas().forEach(meleeArena -> {
			MeleeGame game = new MeleeGame(meleeArena);
			meleeGameManager.addGame(meleeArena.getName(), game, new Lobby(game, game::startGame, Melee.MELEE_CHAT_PREFIX));
		});
		
		// Create a rabbit game for each rabbit arena
		rabbitArenaManager.loadArenas();
		rabbitArenaManager.getArenas().forEach(rabbitArena -> {
			RabbitGame game = new RabbitGame(rabbitArena);
			rabbitGameManager.addGame(rabbitArena.getName(), game, new Lobby(game, game::startGame, Melee.RABBIT_CHAT_PREFIX));
		});
		
		// Log out what games were created
		String meleeGamesList = meleeGameManager.getGamesList().stream()
															   .map(game -> game.getArena().getName())
															   .collect(Collectors.joining(", "));
		getLogger().info("Created Melee Games for arenas : " + meleeGamesList);

		String rabbitGamesList = rabbitGameManager.getGamesList().stream()
																 .map(game -> game.getArena().getName())
																 .collect(Collectors.joining(", "));
		getLogger().info("Created Rabbit Games for arenas : " + rabbitGamesList);
	}
	

	@Override
	public void onDisable(){
		meleeGameManager.stopAllGames();
		rabbitGameManager.stopAllGames();
	}
	
}
