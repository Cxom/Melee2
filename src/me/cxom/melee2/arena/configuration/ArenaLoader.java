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

public class ArenaLoader {
	
	public static MeleeArena load(FileConfiguration arenacfg){
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
	
	/*Static loading parsers*/
	
	public static World getWorld(ConfigurationSection section){
		return Bukkit.getWorld(section.getString("world"));
	}
	
	public static World getRootWorld(ConfigurationSection section){
		return getWorld(section.getRoot());
	}
	
	public static String getName(File arenaFile){
		String name = arenaFile.getName();
        return name.substring(0, name.length() - 4);
	}
	
	public static <T> List<T> getList(ConfigurationSection section, Function<ConfigurationSection, T> loader) throws ClassCastException{
		List<T> list = new ArrayList<>();
		for(String key : section.getKeys(false)){
			try {
				list.add(loader.apply(section.getConfigurationSection(key)));
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public static Location getLocation(ConfigurationSection spawnInfo){
		return getLocation(spawnInfo, getRootWorld(spawnInfo));
	}
	
	public static Location getLocationWithWorld(ConfigurationSection section){
		return getLocation(section, getWorld(section));
	}
	
	public static Location getLocation(ConfigurationSection spawnInfo, World world){
		return new Location(
			world,
			spawnInfo.getDouble("x"),
			spawnInfo.getDouble("y"),
			spawnInfo.getDouble("z"),
			(float) spawnInfo.getDouble("pitch", 0),
			(float) spawnInfo.getDouble("yaw", 0) //TODO Is 0 pitch, 0 yaw looking straight forward?
		);
	}
	
}
