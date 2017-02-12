package me.cxom.melee2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.cxom.melee2.player.MeleePlayer;

public class Melee extends JavaPlugin {

	private static Plugin plugin;
	public static Plugin getPlugin(){ return plugin; }
	
	private static Map<UUID, MeleePlayer> players = new HashMap<>();
	
	@Override
	public void onEnable(){
		plugin = this;
		//register events
	}
	
	@Override
	public void onDisable(){
		//force stop matches
		//restore players
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
	
}
