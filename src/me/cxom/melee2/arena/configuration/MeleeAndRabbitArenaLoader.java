package me.cxom.melee2.arena.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.arena.RabbitArena;
import net.punchtree.minigames.arena.creation.ArenaLoader;

public class MeleeAndRabbitArenaLoader extends ArenaLoader {
	
	public static MeleeArena loadMeleeArena(FileConfiguration arenacfg){
		String name = arenacfg.getString("name");
		
		World world = getRootWorld(arenacfg);
		
		List<Location> spawns = getList(arenacfg.getConfigurationSection("spawns"),
										(ConfigurationSection section) -> { return getLocation(section, world); });
		
		Location pregameLobby;
		if (arenacfg.isConfigurationSection("lobby")){
			pregameLobby = getLocation(arenacfg.getConfigurationSection("lobby"));
		} else {
			pregameLobby = spawns.get(0);
		}
		
		boolean relative = arenacfg.isConfigurationSection("relative");
		if (relative){
			Location anchor = getLocation(arenacfg.getConfigurationSection("relative"));
			spawns.stream().map((Location l) -> { return l.add(anchor); }).collect(Collectors.toList());
			pregameLobby.add(anchor);
		}
		
		int playersToStart = arenacfg.getInt("playersToStart", 2);
		int killsToEnd = arenacfg.getInt("killsToEnd", 7);
		
		return new MeleeArena(name, pregameLobby, playersToStart, spawns, killsToEnd);
	};
	
	public static RabbitArena loadRabbitArena(FileConfiguration arenacfg) {
		String name = arenacfg.getString("name");
		
		World world = getRootWorld(arenacfg);
		
		List<Location> spawns = getList(arenacfg.getConfigurationSection("spawns"),
										(ConfigurationSection section) -> { return getLocation(section, world); });
		
		Location pregameLobby;
		if (arenacfg.isConfigurationSection("lobby")){
			pregameLobby = getLocation(arenacfg.getConfigurationSection("lobby"));
		} else {
			pregameLobby = spawns.get(0);
		}
		
		Location centerpoint = getLocation(arenacfg.getConfigurationSection("centerpoint"));
		
		boolean relative = arenacfg.isConfigurationSection("relative");
		if (relative){
			Location anchor = getLocation(arenacfg.getConfigurationSection("relative"));
			spawns.stream().map((Location l) -> { return l.add(anchor); }).collect(Collectors.toList());
			centerpoint.add(anchor);
			pregameLobby.add(anchor);
		}
		
		int playersToStart = arenacfg.getInt("playersToStart", 2);
//		int timeToWin = arenacfg.getInt("timeToWin", 35);
		
		return new RabbitArena(name, pregameLobby, playersToStart, spawns, centerpoint);
	}
	
}
