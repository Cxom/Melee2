package me.cxom.melee2.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MeleePlayer {

	private final UUID uuid;
	
	private final MeleeColor color;
	
	private int kills = 0;
	
	public MeleePlayer(Player player, MeleeColor color){
		uuid = player.getUniqueId();
		this.color = color;
	}
	
	public UUID getUniqueId(){
		return uuid;
	}
	
	public Player getPlayer(){
		return Bukkit.getPlayer(uuid);
	}
	
	public MeleeColor getColor(){
		return color;
	}
	
	public int getKills(){
		return kills;
	}
	
	public void incrementKills(){
		kills++;
	}
	
}
