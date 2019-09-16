package me.cxom.melee2.arena;

import java.util.List;

import org.bukkit.Location;

import net.punchtree.minigames.arena.Arena;

public class RabbitArena extends Arena {
	
	private final List<Location> spawns;
	
	private final Location centerpoint;
	private final int flagTimeToWin = 60; //TODO make this a member variable of GameInstance as well, set to arena value as default
	
	
	public RabbitArena(String name, Location pregameLobby, int playersNeededToStart, List<Location> spawns,
			/*int flagTimeToWin,*/ Location centerpoint) {
		super(name, pregameLobby, playersNeededToStart);
		
		this.spawns = spawns;
		this.centerpoint = centerpoint;
	}

	public List<Location> getSpawns() {
		return spawns;
	}
	
	public Location getCenterpoint() {
		return centerpoint; 
	}

	public int getFlagTimeToWin() {
		return flagTimeToWin;
	}
	
}
	

