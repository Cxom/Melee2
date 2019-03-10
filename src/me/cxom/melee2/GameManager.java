package me.cxom.melee2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.cxom.melee2.arena.configuration.ArenaManager;
import me.cxom.melee2.game.PvpGame;
import me.cxom.melee2.game.PvpGameController;
import me.cxom.melee2.game.lobby.Lobby;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.gui.menu.MinigameMenu;

public class GameManager<Controller extends PvpGameController, Model extends PvpGame, Arena> {
	
//	private MeleeGameManager() {}
//	
//	private static MeleeGameManager instance = new MeleeGameManager();
//	public static MeleeGameManager getManager(){
//		return instance;
//	}
	
	//The controllers should NEVER be exposed outside this class
	private Map<String, Controller> controllers = new HashMap<>();
	private Controller getController(String game) { return controllers.get(game); }
	
	//The models can be exposed
	private Map<String, Model> models = new HashMap<>();
	public Model getGame(String game) { return models.get(game); }
	
	//In practice, Lobbies to Games are 1-to-1, but in theory, this is not true.
	private Map<String, Lobby> lobbies = new HashMap<>();
	public Lobby getLobby(String lobby) { return lobbies.get(lobby); }
	
	
	/*****************************************/
	
	//TODO Should this take a string arg? Any usefulness? Speculative?
//	private void createGame(MeleeArena arena) {
//		MeleeGameController controller = new MeleeGameController(arena);
//		controllers.put(arena.getName(), controller);
//		models.put(arena.getName(), controller.getGame());
//		lobbies.put(arena.getName(), controller.getLobby());
//	}
	
//	public static void createAllGames() {
//		ArenaManager.getMeleeArenas().forEach(MeleeGameManager::createGame);
//	}
	
	public void stopAllGames() {
		controllers.values().forEach(PvpGameController::stopGame);
	}
	
	public boolean addPlayerToGameLobby(String lobby, Player player) {
		
		if (! hasLobby(lobby)) throw new AssertionError("There is no game with the name " + lobby + " !");
		
		getController(lobby).addPlayerToLobby(player);
		
		//TODO
		return true;
	}
	
	
	
	public boolean hasGame(String game) {
		return models.containsKey(game);
	}

	public Collection<Model> getGamesList(){
		return models.values();
	}
	
	public boolean hasLobby(String lobby) {
		return lobbies.containsKey(lobby);
	}
	
	public Collection<Lobby> getLobbyList(){
		return lobbies.values();
	}
	
	public Inventory getMenu() {
		return MinigameMenu.createMenu(getLobbyList());
	}
	
	public Inventory getMenu(Predicate<MeleeGame> filter) {
		Set<Lobby> lobbies = getLobbyList().stream()
											   .filter(lobby -> filter.test(lobby.getGame()))
											   .collect(Collectors.toSet());
		return MinigameMenu.createMenu(lobbies);
	}

//	public void debugGame(String game, Player player) {
//		getController(game).debug(player);
//	}

	public void debugGamesList(Player player) {
		player.sendMessage("Games: " + models);
	}

	
	
}
