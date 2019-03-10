package me.cxom.melee2.arena.old;

import java.util.List;

import org.bukkit.Location;

public class MeleeArenaOld extends Arena {

	private final String name;
	private final Location pregameLobby;
	private final int playersNeededToStart;
	private final List<Location> spawns;
	private final int killsToEnd = 1; //TODO make this a member variable of GameInstance as well, set to arena value as default
	
	public MeleeArenaOld(String name, Location pregameLobby, int playersNeededToStart, List<Location> spawns, int killsToEnd){
		this.name = name;
		this.pregameLobby = pregameLobby;
		this.playersNeededToStart = playersNeededToStart;
		this.spawns = spawns;
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

	public List<Location> getSpawns() {
		return spawns;
	}

	public int getKillsToEnd() {
		return killsToEnd;
	}
	
}
