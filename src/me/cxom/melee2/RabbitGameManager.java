package me.cxom.melee2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.cxom.melee2.arena.RabbitArena;
import me.cxom.melee2.arena.configuration.ArenaManager;
import me.cxom.melee2.game.rabbit.RabbitGame;
import me.cxom.melee2.game.rabbit.RabbitGameController;
import me.cxom.melee2.gui.menu.RabbitMenu;
import net.punchtree.minigames.game.PvpGame;
import net.punchtree.minigames.lobby.Lobby;

public class RabbitGameManager {
	
//	private MeleeGameManager() {}
//	
//	private static MeleeGameManager instance = new MeleeGameManager();
//	public static MeleeGameManager getManager(){
//		return instance;
//	}
	
	//The controllers should NEVER be exposed outside this class
	private static Map<String, RabbitGameController> controllers = new HashMap<>();
	private static RabbitGameController getController(String game) { return controllers.get(game); }
	
	//The models can be exposed
	private static Map<String, RabbitGame> models = new HashMap<>();
	public static RabbitGame getGame(String game) { return models.get(game); }
	
	//In practice, Lobbies to Games are 1-to-1, but in theory, this is not true.
	private static Map<String, Lobby> lobbies = new HashMap<>();
	public static Lobby getLobby(String lobby) { return lobbies.get(lobby); }
	
	
	private static final String MENU_NAME = "Rabbit Games";
	
	/*****************************************/
	
	//TODO Should this take a string arg? Any usefulness? Speculative?
	public static void createGame(RabbitArena arena) {
		RabbitGameController controller = new RabbitGameController(arena);
		controllers.put(arena.getName(), controller);
		models.put(arena.getName(), controller.getGame());
		lobbies.put(arena.getName(), controller.getLobby());
	}
	
	public static void createAllGames() {
		ArenaManager.getRabbitArenas().forEach(RabbitGameManager::createGame);
	}
	
	public static void stopAllGames() {
		controllers.values().forEach(RabbitGameController::stopGame);
	}
	
	public static boolean addPlayerToGameLobby(String lobby, Player player) {
		
		if (! hasLobby(lobby)) throw new AssertionError("There is no game with the name " + lobby + " !");
		
		getLobby(lobby).addPlayerIfPossible(player);
		
		//TODO
		return true;
	}
	
	
	
	public static boolean hasGame(String game) {
		return models.containsKey(game);
	}

	public static Collection<RabbitGame> getGamesList(){
		return models.values();
	}
	
	public static boolean hasLobby(String lobby) {
		return lobbies.containsKey(lobby);
	}
	
	public static Collection<Lobby> getLobbyList(){
		return lobbies.values();
	}
	
	public static Inventory getMenu() {
		return RabbitMenu.createMenu(getLobbyList());
	}
	
	public static Inventory getMenu(Predicate<PvpGame> filter) {
		Set<Lobby> lobbies = getLobbyList().stream()
											   .filter(lobby -> filter.test(lobby.getGame()))
											   .collect(Collectors.toSet());
		return RabbitMenu.createMenu(lobbies);
	}

	public static void debugGame(String game, Player player) {
		getController(game).debug(player);
	}

//	public static void debugGamesList(Player player) {
//		player.sendMessage("Games: " + models);
//	}

	
	
}
