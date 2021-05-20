package me.cxom.melee2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import me.cxom.melee2.arena.RabbitArena;
import me.cxom.melee2.arena.configuration.MeleeAndRabbitArenaLoader;
import me.cxom.melee2.game.rabbit.RabbitGame;
import net.punchtree.minigames.arena.creation.ArenaManager;
import net.punchtree.minigames.lobby.Lobby;
import net.punchtree.minigames.menu.MinigameMenu;

public class RabbitGameManager {
	
//	private MeleeGameManager() {}
//	
//	private static MeleeGameManager instance = new MeleeGameManager();
//	public static MeleeGameManager getManager(){
//		return instance;
//	}
	
	//The models can be exposed
	private static Map<String, RabbitGame> models = new HashMap<>();
	public static RabbitGame getGame(String game) { return models.get(game); }
	
	//In practice, Lobbies to Games are 1-to-1, but in theory, this is not true.
	private static Map<String, Lobby> lobbies = new HashMap<>();
	public static Lobby getLobby(String lobby) { return lobbies.get(lobby); }
	
	
	private static final String MENU_NAME = "Rabbit Games";
	
	private static MinigameMenu menu;
	
	/*****************************************/
	
	//TODO Should this take a string arg? Any usefulness? Speculative?
	public static void createGame(RabbitArena arena) {
		RabbitGame game = new RabbitGame(arena);
		models.put(arena.getName(), game);
		lobbies.put(arena.getName(), game.getLobby());
	}
	
	public static void createAllGames() {
		ArenaManager<RabbitArena> rabbitArenaManager = new ArenaManager<>(Melee.rabbitArenaFolder, MeleeAndRabbitArenaLoader::loadRabbitArena);
		rabbitArenaManager.loadArenas();
		rabbitArenaManager.getArenas().forEach(RabbitGameManager::createGame);
	}
	
	public static void stopAllGames() {
		models.values().forEach(RabbitGame::stopGame);
	}
	
	public static boolean addPlayerToGameLobby(String lobby, Player player) {
		
		if (! hasLobby(lobby)) throw new AssertionError("There is no game with the name " + lobby + " !");
		
		getLobby(lobby).addPlayerIfPossible(player);
		
		//TODO
		return true;
	}
	
	private static void createMenu() {
		menu = new MinigameMenu(MENU_NAME, lobbies.values());
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
//		return RabbitMenu.createMenu(lobbies);
//	}

	public static void debugGame(String game, Player player) {
		getGame(game).debug(player);
	}

//	public static void debugGamesList(Player player) {
//		player.sendMessage("Games: " + models);
//	}

	
	
}
