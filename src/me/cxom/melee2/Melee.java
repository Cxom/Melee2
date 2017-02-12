package me.cxom.melee2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.arena.configuration.ArenaManager;
import me.cxom.melee2.events.custom.MeleeEventCaller;
import me.cxom.melee2.game.GameInstance;
import me.cxom.melee2.game.Lobby;
import me.cxom.melee2.player.MeleePlayer;

public class Melee extends JavaPlugin {

	public static final String CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Melee" + ChatColor.DARK_GREEN + "]" + ChatColor.RESET + " ";
	
	private static Plugin plugin;
	public static Plugin getPlugin(){ return plugin; }
	
	private static Map<UUID, MeleePlayer> players = new HashMap<>();
	
	private static Map<String, Lobby> lobbies = new HashMap<>();
	private static Map<String, GameInstance> games = new HashMap<>();
	
	@Override
	public void onEnable(){
		plugin = this;
		Bukkit.getServer().getPluginManager().registerEvents(new MeleeEventCaller(), getPlugin());
		//register events
		for (MeleeArena arena : ArenaManager.getArenas()){
			GameInstance game = new GameInstance(arena);
			lobbies.put(arena.getName(), new Lobby(game));
			games.put(arena.getName(), game);
		}
	}
	
	@Override
	public void onDisable(){
		for (Lobby lobby : lobbies.values()){
			lobby.removeAll();
		}
		for (GameInstance game : games.values()){
			game.forceStop();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		return true;
	}
	
	public static boolean isPlayer(Player player){ return isPlayer(player.getUniqueId()); }
	public static boolean isPlayer(UUID uuid){
		return players.containsKey(uuid);
	}
	
	public static MeleePlayer getPlayer(Player player){ return getPlayer(player.getUniqueId()); }
	public static MeleePlayer getPlayer(UUID uuid){
		return players.get(uuid);
	}
	
	public static void addPlayer(MeleePlayer jp){
		players.put(jp.getUniqueId(), jp);
	}
	
	public static void removePlayer(MeleePlayer jp){ removePlayer(jp.getUniqueId()); }
	public static void removePlayer(Player player){ removePlayer(player.getUniqueId()); }
	public static void removePlayer(UUID uuid){
		players.remove(uuid);
	}
	
	public Lobby getLobby(String name){
		return lobbies.get(name);
	}
	
	public GameInstance getGame(String name){
		return games.get(name);
	}
	
	public static Map<String, GameInstance> getGameMap(){
		return games;
	}
	
}
