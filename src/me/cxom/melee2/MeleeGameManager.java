package me.cxom.melee2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.arena.configuration.MeleeAndRabbitArenaLoader;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.game.melee.MeleeGameController;
import me.cxom.melee2.gui.menu.MinigameMenu;
import net.punchtree.minigames.arena.creation.ArenaManager;
import net.punchtree.minigames.lobby.Lobby;

public class MeleeGameManager {
	
//	private MeleeGameManager() {}
//	
//	private static MeleeGameManager instance = new MeleeGameManager();
//	public static MeleeGameManager getManager(){
//		return instance;
//	}
	
	static ArenaManager<MeleeArena> meleeArenaManager;
	
	//The controllers should NEVER be exposed outside this class
	private static Map<String, MeleeGameController> controllers = new HashMap<>();
	private static MeleeGameController getController(String game) { return controllers.get(game); }
	
	//The models can be exposed
	private static Map<String, MeleeGame> models = new HashMap<>();
	public static MeleeGame getGame(String game) { return models.get(game); }
	
	//In practice, Lobbies to Games are 1-to-1, but in theory, this is not true.
	private static Map<String, Lobby> lobbies = new HashMap<>();
	public static Lobby getLobby(String lobby) { return lobbies.get(lobby); }
	
	private static final String MENU_NAME = "Melee Games";
	
	private static MinigameMenu menu;
	
	/*****************************************/
	
	//TODO Should this take a string arg? Any usefulness? Speculative?
	private static void createGame(MeleeArena arena) {
		MeleeGameController controller = new MeleeGameController(arena);
		controllers.put(arena.getName(), controller);
		models.put(arena.getName(), controller.getGame());
		lobbies.put(arena.getName(), controller.getLobby());
	}
	
	public static void createAllGames() {
		meleeArenaManager = new ArenaManager<>(Melee.meleeArenaFolder, MeleeAndRabbitArenaLoader::loadMeleeArena);
		meleeArenaManager.loadArenas();
		meleeArenaManager.getArenas().forEach(MeleeGameManager::createGame);
	}
	
	public static void stopAllGames() {
		controllers.values().forEach(MeleeGameController::stopGame);
	}
	
	public static boolean addPlayerToGameLobby(String lobbyName, Player player) {
		
		if (! hasLobby(lobbyName)) throw new AssertionError("There is no game with the name " + lobbyName + " !");
		
		getLobby(lobbyName).addPlayerIfPossible(player);
		menu.refresh();
		
		//TODO
		return true;
	}
	
	private static void createMenu() {
		menu = new MinigameMenu(MENU_NAME, lobbies.values());
	}
	
	public static boolean hasGame(String game) {
		return models.containsKey(game);
	}

	public static Collection<MeleeGame> getGamesList(){
		return models.values();
	}
	
	public static boolean hasLobby(String lobby) {
		return lobbies.containsKey(lobby);
	}
	
	public static Collection<Lobby> getLobbyList(){
		return lobbies.values();
	}
	
	public static void showMenuTo(Player player) {
		if (menu == null) {
			createMenu();
		}
		menu.showTo(player);
	}
	
	// We can reenable this if we actually need it
//	public static Inventory getMenu(Predicate<PvpGame> filter) {
//		Set<Lobby> lobbies = getLobbyList().stream()
//											   .filter(lobby -> filter.test(lobby.getGame()))
//											   .collect(Collectors.toSet());
//		return MeleeMenu.createMenu(lobbies);
//	}

	public static void debugGame(String game, Player player) {
		getController(game).debug(player);
	}

	public static void debugGamesList(Player player) {
		player.sendMessage("Games: " + models);
	}

	
	
}
