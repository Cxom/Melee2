package me.cxom.melee2.arena;

import java.util.List;

import org.bukkit.Location;

import me.cxom.melee2.util.CirculatingList;

public class MeleeArena {

	private final String name;
	private final Location pregameLobby;
	private final int playersNeededToStart;
	private final CirculatingList<Location> spawns;
	private final int killsToEnd = 1; //TODO make this a member variable of GameInstance as well, set to arena value as default
	
	public MeleeArena(String name, Location pregameLobby, int playersNeededToStart, List<Location> spawns, int killsToEnd){
		this.name = name;
		this.pregameLobby = pregameLobby;
		this.playersNeededToStart = playersNeededToStart;
		this.spawns = new CirculatingList<Location>(spawns, true);
		//this.killsToEnd = killsToEnd;
	}
	
	public String getName(){
		return name;
	}
	
	public Location getPregameLobby(){
		return pregameLobby;
	}
	
	public int getPlayersNeededToStart(){
		return playersNeededToStart;
	}

	public CirculatingList<Location> getSpawns() {
		return spawns;
	}

	public int getKillsToEnd() {
		return killsToEnd;
	}
	
}
