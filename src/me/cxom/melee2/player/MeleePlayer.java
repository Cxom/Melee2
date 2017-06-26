package me.cxom.melee2.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.cxom.melee2.Melee;
import me.cxom.melee2.game.GameInstance;
import me.cxom.melee2.util.InventoryUtils;

public class MeleePlayer {

	private final UUID uuid;
	private final String game;
	
	private final MeleeColor color;
	
	private int kills = 0;
	
	public MeleePlayer(Player player, MeleeColor color, String game){
		this.uuid = player.getUniqueId();
		this.color = color;
		this.game = game;
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
	
	public GameInstance getGame(){
		return Melee.getGame(game);
	}
	
	public String getGameName(){
		return game;
	}
	
	public boolean remove(){
		return getGame().removePlayer(getPlayer());
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
