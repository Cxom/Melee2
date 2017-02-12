package me.cxom.melee2.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerProfile {

	private static Map<UUID, PlayerProfile> saved = new HashMap<>();
	
	public static void save(Player player){
		saved.put(player.getUniqueId(), new PlayerProfile(player));
	}
	
	public static boolean isSaved(Player player){
		return saved.containsKey(player.getUniqueId());
	}
	
	public static void restore(Player player){
		if (saved.containsKey(player.getUniqueId())){
			saved.get(player.getUniqueId()).restore();
			saved.remove(player.getUniqueId());
		}
	}
	
	public static void restoreAll(){
		for (PlayerProfile pp : saved.values()){
			pp.restore();
		}
		saved.clear();
	}
	
	private final UUID uuid;
	private final ItemStack[] inventory;
	private final ItemStack[] armor;
	private final Location location;
	private final GameMode gamemode;
	private final boolean flying;
	private final int xpLvl;
	private final float xp;
	private final double health;
	private final int hunger;
	private final float saturation;
	private final float exhaustion;
	
	private PlayerProfile(Player player){
		this.uuid = player.getUniqueId();
		this.inventory = player.getInventory().getContents();
		this.armor = player.getInventory().getArmorContents();
		this.location = player.getLocation();
		this.gamemode = player.getGameMode();
		this.flying = player.isFlying();
		this.xpLvl = player.getLevel();
		this.xp = player.getExp();
		this.health = player.getHealth();
		this.hunger = player.getFoodLevel();
		this.saturation = player.getSaturation();
		this.exhaustion = player.getExhaustion();
	}
	
	private void restore(){
		Player player = Bukkit.getPlayer(uuid);
		player.getInventory().setContents(inventory);
		player.getInventory().setArmorContents(armor);
		player.setGameMode(gamemode);
		player.setFlying(flying);
		player.setLevel(xpLvl);
		player.setExp(xp);
		player.setHealth(health);
		player.setFoodLevel(hunger);
		player.setSaturation(saturation);
		player.setExhaustion(exhaustion);
		player.teleport(location);
	}
	
}
