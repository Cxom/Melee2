package net.punchtree.melee.arena.configuration;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.punchtree.melee.arena.MeleeArena;
import net.punchtree.melee.arena.RabbitArena;
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
	
	private static int spawnCounter = 1;
	
	public static void convertMeleeArena(Location relative, MeleeArena meleeArena, FileConfiguration arenacfg) {
		arenacfg.set("name", meleeArena.getName());
		
		arenacfg.set("world", meleeArena.getSpawns().get(0).getWorld().getName());
		
		ConfigurationSection spawnsSec = arenacfg.createSection("spawns");
		
		spawnCounter = 1;
		meleeArena.getSpawns().stream().map((Location l) -> { return l.subtract(relative); }).forEach((Location l) -> {
			ConfigurationSection spawnSec = spawnsSec.createSection(String.valueOf(spawnCounter));
			setLocation(spawnSec, l);
			++spawnCounter;
		}) ;
		
		ConfigurationSection lobbySec = arenacfg.createSection("lobby");
		setLocation(lobbySec, meleeArena.getPregameLobby().subtract(relative));
		
		ConfigurationSection relativeSec = arenacfg.createSection("relative");
		setLocation(relativeSec, relative);
	}
	
	public static void setLocation(ConfigurationSection section, Location location) {
		section.set("x", location.getX());
		section.set("y", location.getY());
		section.set("z", location.getZ());
		section.set("yaw", location.getPitch());
		section.set("pitch", location.getYaw());
	}
	
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
