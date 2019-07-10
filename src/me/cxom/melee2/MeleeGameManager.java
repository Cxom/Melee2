package me.cxom.melee2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.arena.configuration.ArenaManager;
import me.cxom.melee2.game.PvpGame;
import me.cxom.melee2.game.lobby.Lobby;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.game.melee.MeleeGameController;
import me.cxom.melee2.gui.menu.MeleeMenu;

public class MeleeGameManager {
	
//	private MeleeGameManager() {}
//	
//	private static MeleeGameManager instance = new MeleeGameManager();
//	public static MeleeGameManager getManager(){
//		return instance;
//	}
	
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
	
	/*****************************************/
	
	//TODO Should this take a string arg? Any usefulness? Speculative?
	private static void createGame(MeleeArena arena) {
		MeleeGameController controller = new MeleeGameController(arena);
		controllers.put(arena.getName(), controller);
		models.put(arena.getName(), controller.getGame());
		lobbies.put(arena.getName(), controller.getLobby());
	}
	
	public static void createAllGames() {
		ArenaManager.getMeleeArenas().forEach(MeleeGameManager::createGame);
	}
	
	public static void stopAllGames() {
		controllers.values().forEach(MeleeGameController::stopGame);
	}
	
	public static boolean addPlayerToGameLobby(String lobbyName, Player player) {
		
		if (! hasLobby(lobbyName)) throw new AssertionError("There is no game with the name " + lobbyName + " !");
		
		getLobby(lobbyName).addPlayerIfPossible(player);
		
		//TODO
		return true;
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
	
	public static Inventory getMenu() {
		return MeleeMenu.createMenu(getLobbyList());
	}
	
	public static Inventory getMenu(Predicate<PvpGame> filter) {
		Set<Lobby> lobbies = getLobbyList().stream()
											   .filter(lobby -> filter.test(lobby.getGame()))
											   .collect(Collectors.toSet());
		return MeleeMenu.createMenu(lobbies);
	}

	public static void debugGame(String game, Player player) {
		getController(game).debug(player);
	}

	public static void debugGamesList(Player player) {
		player.sendMessage("Games: " + models);
	}

	
	
}
