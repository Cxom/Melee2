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
import me.cxom.melee2.arena.RabbitArena;

public class ArenaManager {
	
	/**
	 * The folder containing the Arena YAML files
	 */
	private static File arenaFolder;
	
	/**
	 * A map of the Arena names -> their YAML Configurations
	 */
	private static Map<String, FileConfiguration> meleeArenaConfigs = new HashMap<>();
	
	/**
	 * A map of the Arena names -> their loaded Objects
	 */
	private static Map<String, MeleeArena> meleeArenas = new HashMap<>();
	
	
	
	private static Map<String, FileConfiguration> rabbitArenaConfigs = new HashMap<>();
	
	/**
	 * A map of the Arena names -> their loaded Objects
	 */
	private static Map<String, RabbitArena> rabbitArenas = new HashMap<>();
	
	// ---------- GETTERS ----------
	public static Collection<MeleeArena> getMeleeArenas(){
		return meleeArenas.values();
	}
	
	public static Collection<RabbitArena> getRabbitArenas(){
		return rabbitArenas.values();
	}
	
//	public static boolean isArena(String arenaName){
//		return meleeArenas.containsKey(arenaName) || loadArenaConfig(arenaName) != null;
//	}
//	
//	public static MeleeArena getArena(String arenaName){
//		if (!isArena(arenaName)) return null;
//		return meleeArenas.get(arenaName);
//	}
//	
//	public static FileConfiguration getArenaConfig(String arenaName){
//		if (!isArena(arenaName)) return null;
//		return arenaConfigs.get(arenaName);
//	}
	// --------------------------------
	
	
	
//	public static void modifyArena(String arenaName, String path, Object value){
//		if(!isArena(arenaName)) return;
//		FileConfiguration arena = getArenaConfig(arenaName);
//		arena.set(path, value);
//		arenaConfigs.put(arenaName, arena);
//		MeleeArena loaded = ArenaLoader.getInstance().loadMeleeArena(arena);
//		if (loaded != null){
//			meleeArenas.put(arenaName, loaded);
//		}
//	}
	
	public static void loadMeleeArenas(){
		
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
					
					String arenaName = arenaf.getName().substring(0, arenaf.getName().length() - 4);
					meleeArenaConfigs.put(arenaName, arena);
					MeleeArena loaded = ArenaLoader.getInstance().loadMeleeArena(arena);
					if (loaded != null){
						meleeArenas.put(arenaName, loaded);
					}
				} catch (IOException | InvalidConfigurationException e) {
					Bukkit.getLogger().warning("Could not load " + arenaf.getName() + "!");
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void loadRabbitArenas(){
		
		arenaFolder = new File(Melee.getPlugin().getDataFolder().getAbsolutePath()
				+ File.separator + "RabbitArenas");
		
		
		if(!arenaFolder.exists()){
			arenaFolder.mkdirs();
		}
		
		if(arenaFolder.listFiles() != null){
			for (File arenaf : Arrays.asList(arenaFolder.listFiles())) {
				FileConfiguration arena = new YamlConfiguration();
				try {
					arena.load(arenaf);
					
					String arenaName = arenaf.getName().substring(0, arenaf.getName().length() - 4);
					rabbitArenaConfigs.put(arenaName, arena);
					RabbitArena loaded = ArenaLoader.getInstance().loadRabbitArena(arena);
					if (loaded != null){
						rabbitArenas.put(arenaName, loaded);
					}
				} catch (IOException | InvalidConfigurationException e) {
					Bukkit.getLogger().warning("Could not load " + arenaf.getName() + "!");
					e.printStackTrace();
				}
			}
		}
	}
	
//	public static FileConfiguration loadMeleeArenaConfig(String arenaName){
//		File arenaf = new File(Melee.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "arenas" + File.separator + arenaName);
//		if(!arenaf.exists()){
//			return null;
//		} else {
//			FileConfiguration arena = new YamlConfiguration();
//			try {
//				arena.load(arenaf);
//				meleeArenaConfigs.put(arenaName, arena);
//				MeleeArena loaded = ArenaLoader.getInstance().loadMeleeArena(arena);
//				if (loaded != null){
//					meleeArenas.put(arenaf.getName(), loaded);
//				}
//				return arena;
//			} catch (IOException | InvalidConfigurationException e) {
//				Bukkit.getLogger().warning("Could not load " + arenaf.getName() + "!");
//				e.printStackTrace();
//				return null;
//			}
//		}
//		
//	}
	
	
	
//	public static void save(String arenaName){
//		if (!isArena(arenaName)) throw new IllegalArgumentException();
//		File arenaf = new File(Melee.getPlugin().getDataFolder().getAbsolutePath()
//				+ File.separator + "Arenas" + File.separator + arenaName + ".yml");
//		try {
//			getArenaConfig(arenaName).save(arenaf);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void saveAll(){
		for(Map.Entry<String, FileConfiguration> arenaEntry : meleeArenaConfigs.entrySet()){
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
