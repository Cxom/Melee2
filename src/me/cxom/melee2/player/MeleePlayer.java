package me.cxom.melee2.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MeleePlayer {

	private final UUID uuid;
	
	private int kills = 0;
	
	public MeleePlayer(Player player){
		uuid = player.getUniqueId();
	}
	
	public UUID getUniqueId(){
		return uuid;
	}
	
	public Player getPlayer(){
		return Bukkit.getPlayer(uuid);
	}
	
	public int getKills(){
		return kills;
	}
	
	public void incrementKills(){
		kills++;
	}
	
}
