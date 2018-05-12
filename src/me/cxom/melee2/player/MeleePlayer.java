package me.cxom.melee2.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.cxom.melee2.util.InventoryUtils;

public class MeleePlayer {

	private final UUID uuid;
	
	private final MeleeColor color;
	
	
	
	private int kills = 0;
	
	public MeleePlayer(Player player, MeleeColor color){
		this.uuid = player.getUniqueId();
		this.color = color;
		
		player.getInventory().clear();
		InventoryUtils.equipPlayer(player, color);
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
	
	@Override
	public boolean equals(Object o){
		if (! (o instanceof MeleePlayer)) return false;
		return uuid.equals(((MeleePlayer) o).getUniqueId());
	}
	
}
