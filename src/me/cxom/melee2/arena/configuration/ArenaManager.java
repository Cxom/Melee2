package me.cxom.melee2.arena.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;

public class ArenaManager {
	
	private static File arenaFolder;
	
	private static Map<String, FileConfiguration> arenaConfigs = new HashMap<>();
	
	private static Map<String, MeleeArena> arenas = new HashMap<>();
	
	public static Collection<MeleeArena> getArenas(){
		return arenas.values();
	}
	
	public static boolean isArena(String arenaName){
		return arenas.containsKey(arenaName) || loadArenaConfig(arenaName) != null;
	}
	
	public static MeleeArena getArena(String arenaName){
		if (!isArena(arenaName)) return null;
		return arenas.get(arenaName);
	}
	
	public static FileConfiguration getArenaConfig(String arenaName){
		if (!isArena(arenaName)) return null;
		return arenaConfigs.get(arenaName);
	}
	
	public static void modifyArena(String arenaName, String path, Object value){
		if(!isArena(arenaName)) return;
		FileConfiguration arena = getArenaConfig(arenaName);
		arena.set(path, value);
		arenaConfigs.put(arenaName, arena);
		MeleeArena loaded = ArenaLoader.load(arena);
		if (loaded != null){
			arenas.put(arenaName, loaded);
		}
	}
	
	public static void loadArenas(){
		
		arenaFolder = new File(Melee.getPlugin().getDataFolder().getAbsolutePath()
				+ File.separator + "Arenas");
		
		
		if(!arenaFolder.exists()){
			arenaFolder.mkdirs();
		}
		
		if(arenaFolder.listFiles() != null){
			for (File arenaf : Arrays.asList(arenaFolder.listFiles())) {
				FileConfiguration arena = new YamlConfiguration();
				try {
					arena.load(arenaf);
					arenaConfigs.put(arenaf.getName(), arena);
					MeleeArena loaded = ArenaLoader.load(arena);
					if (loaded != null){
						arenas.put(arenaf.getName(), loaded);
					}
				} catch (IOException | InvalidConfigurationException e) {
					Bukkit.getLogger().warning("Could not load " + arenaf.getName() + "!");
					e.printStackTrace();
				}
			}
		}
	}
	
	public static FileConfiguration loadArenaConfig(String arenaName){
		File arenaf = new File(Melee.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "arenas" + File.separator + arenaName);
		if(!arenaf.exists()){
			return null;
		} else {
			FileConfiguration arena = new YamlConfiguration();
			try {
				arena.load(arenaf);
				arenaConfigs.put(arenaName, arena);
				MeleeArena loaded = ArenaLoader.load(arena);
				if (loaded != null){
					arenas.put(arenaf.getName(), loaded);
				}
				return arena;
			} catch (IOException | InvalidConfigurationException e) {
				Bukkit.getLogger().warning("Could not load " + arenaf.getName() + "!");
				e.printStackTrace();
				return null;
			}
		}
		
	}
	
	public static void save(String arenaName){
		if (!isArena(arenaName)) throw new IllegalArgumentException();
		File arenaf = new File(Melee.getPlugin().getDataFolder().getAbsolutePath()
				+ File.separator + "Arenas" + File.separator + arenaName + ".yml");
		try {
			getArenaConfig(arenaName).save(arenaf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveAll(){
		for(Map.Entry<String, FileConfiguration> arenaEntry : arenaConfigs.entrySet()){
			File arenaf = new File(Melee.getPlugin().getDataFolder().getAbsolutePath()
					+ File.separator + "Arenas" + File.separator + arenaEntry.getKey() + ".yml");
			try {
				arenaEntry.getValue().save(arenaf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
