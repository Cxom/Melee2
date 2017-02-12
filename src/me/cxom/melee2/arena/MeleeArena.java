package me.cxom.melee2.arena;

import java.util.List;

import org.bukkit.Location;

import me.cxom.melee2.util.CirculatingList;

public class MeleeArena {

	private final String name;
	private final Location pregameLobby;
	private final int playersToStart;
	private final CirculatingList<Location> spawns;
	private final int killsToEnd;
	
	public MeleeArena(String name, Location pregameLobby, int playersToStart, List<Location> spawns, int killsToEnd){
		this.name = name;
		this.pregameLobby = pregameLobby;
		this.playersToStart = playersToStart;
		this.spawns = new CirculatingList<Location>(spawns, true);
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

	public CirculatingList<Location> getSpawns() {
		return spawns;
	}

	public int getKillsToEnd() {
		return killsToEnd;
	}
	
}
