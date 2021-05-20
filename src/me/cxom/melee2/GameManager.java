package me.cxom.melee2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import net.punchtree.minigames.game.PvpGame;
import net.punchtree.minigames.lobby.Lobby;
import net.punchtree.minigames.menu.MinigameMenu;

public class GameManager<GameType extends PvpGame> {
	
	//The models can be exposed
	private Map<String, GameType> games = new HashMap<>();
	public GameType getGame(String game) { return games.get(game); }
	
	//In practice, Lobbies to Games are 1-to-1, but in theory, this is not true.
	private Map<String, Lobby> lobbies = new HashMap<>();
	public Lobby getLobby(String lobby) { return lobbies.get(lobby); }
	
	private final String menuName;
	private MinigameMenu menu;
	
	public GameManager(String menuName) {
		this.menuName = menuName;
	}
	
	/*****************************************/
	
	public void addGame(String name, GameType game, Lobby lobby) {
		games.put(name, game);
		lobbies.put(name, lobby);
	}
	
//	public static void createAllGames() {
//		ArenaManager.getMeleeArenas().forEach(MeleeGameManager::createGame);
//	}
	
	public void stopAllGames() {
		games.values().forEach(PvpGame::interruptAndShutdown);
	}
	
	public boolean addPlayerToGameLobby(String lobby, Player player) {
		
		if (! hasLobby(lobby)) throw new AssertionError("There is no game with the name " + lobby + " !");
		
		getLobby(lobby).addPlayerIfPossible(player);
		
		//TODO
		return true;
	}
	
	
	
	public boolean hasGame(String game) {
		return games.containsKey(game);
	}

	public Collection<GameType> getGamesList(){
		return games.values();
	}
	
	public boolean hasLobby(String lobby) {
		return lobbies.containsKey(lobby);
	}
	
	public Collection<Lobby> getLobbyList(){
		return lobbies.values();
	}
	
	private void createMenu() {
		menu = new MinigameMenu(menuName, getLobbyList());
	}
	
	public void showMenuTo(Player player) {
		if (menu == null) {
			createMenu();
		}
		menu.showTo(player);
	}

//	public void debugGame(String game, Player player) {
//		getGame(game).debug(player);
//	}

	public void debugGamesList(Player player) {
		player.sendMessage("Games: " + games);
	}

	
	
}
