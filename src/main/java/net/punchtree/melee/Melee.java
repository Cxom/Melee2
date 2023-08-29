package net.punchtree.melee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import net.punchtree.melee.arena.MeleeArena;
import net.punchtree.melee.arena.RabbitArena;
import net.punchtree.melee.arena.configuration.MeleeAndRabbitArenaLoader;
import net.punchtree.melee.game.melee.MeleeGame;
import net.punchtree.melee.game.rabbit.RabbitGame;
import net.punchtree.minigames.arena.creation.ArenaManager;
import net.punchtree.minigames.game.GameManager;
import net.punchtree.minigames.lobby.PerMapLegacyLobby;
import net.punchtree.minigames.menu.MinigameMenu;
import net.punchtree.minigames.utility.player.PlayerProfile;

public class Melee extends JavaPlugin {

	// Responsibility: Hold globals, handle plugin starting and stopping, route commands
	
	public static final String MELEE_CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Melee" + ChatColor.DARK_GREEN + "]" + ChatColor.RESET + " ";
	public static final String RABBIT_CHAT_PREFIX = ChatColor.GOLD + "[" + ChatColor.WHITE + "Rabbit" + ChatColor.GOLD + "]" + ChatColor.RESET + " ";
	
	private static Melee plugin;
	public static Melee getPlugin(){ return plugin; }
	
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
	
		List<PerMapLegacyLobby> allLobbies = new ArrayList<PerMapLegacyLobby>();
		allLobbies.addAll(meleeGameManager.getLobbyList());
		allLobbies.addAll(rabbitGameManager.getLobbyList());
		allLobbiesMenu = new MinigameMenu("All Games", allLobbies);

		setCommandExecutors();
	}

	private void setCommandExecutors() {
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
			meleeGameManager.addGame(meleeArena.getName(), game, new PerMapLegacyLobby(game, PlayerProfile::restore, Melee.MELEE_CHAT_PREFIX));
		});
		
		// Create a rabbit game for each rabbit arena
		rabbitArenaManager.loadArenas();
		rabbitArenaManager.getArenas().forEach(rabbitArena -> {
			RabbitGame game = new RabbitGame(rabbitArena);
			rabbitGameManager.addGame(rabbitArena.getName(), game, new PerMapLegacyLobby(game, PlayerProfile::restore, Melee.RABBIT_CHAT_PREFIX));
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
	
	public GameManager<MeleeGame> getMeleeGameManager() {
		return meleeGameManager; 
	}
	
	public GameManager<RabbitGame> getRabbitGameManager() {
		return rabbitGameManager;
	}
	
}
