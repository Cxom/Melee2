package me.cxom.melee2.arena;

import java.util.List;

import org.bukkit.Location;

import net.punchtree.minigames.arena.Arena;

public class MeleeArena extends Arena {
	
	private final List<Location> spawns;
	private final int killsToEnd = 7; //TODO make this a member variable of GameInstance as well, set to arena value as default
	
	public MeleeArena(String name, Location pregameLobby, int playersNeededToStart, List<Location> spawns, int killsToEnd){
		super(name, pregameLobby, playersNeededToStart);
		this.spawns = spawns;
		//this.killsToEnd = killsToEnd;
	}

	public List<Location> getSpawns() {
		return spawns;
	}

	public int getKillsToEnd() {
		return killsToEnd;
	}
	
}
