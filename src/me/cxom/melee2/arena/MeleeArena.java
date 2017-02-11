package me.cxom.melee2.arena;

import java.util.List;

import org.bukkit.Location;

public class MeleeArena {

	private final String name;
	private final Location pregameLobby;
	private final int playersToStart;
	private final List<Location> spawns;
	private final int killsToEnd;
	
	public MeleeArena(String name, Location pregameLobby, int playersToStart, List<Location> spawns, int killsToEnd){
		this.name = name;
		this.pregameLobby = pregameLobby;
		this.playersToStart = playersToStart;
		this.spawns = spawns;
		this.killsToEnd = killsToEnd;
	}
	
	public String getName(){
		return name;
	}
	
	public Location getPregameLobby(){
		return pregameLobby;
	}
	
	public int getPlayersToStart(){
		return playersToStart;
	}

	public List<Location> getSpawns() {
		return spawns;
	}

	public int getKillsToEnd() {
		return killsToEnd;
	}
	
}
