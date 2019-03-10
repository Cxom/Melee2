package me.cxom.melee2.arena;

import org.bukkit.Location;

public abstract class Arena {

	private final String name;
	private final Location pregameLobby;
	private final int playersNeededToStart;
	
	public Arena(String name, Location pregameLobby, int playersNeededToStart){
		this.name = name;
		this.pregameLobby = pregameLobby;
		this.playersNeededToStart = playersNeededToStart;
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
	
}
