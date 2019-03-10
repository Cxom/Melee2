package me.cxom.melee2.game.rabbit;

import java.util.Collection;

import org.bukkit.Location;

import me.cxom.melee2.player.RabbitPlayer;

public interface RabbitGameObserver {

	public default void onGameStart() {
		
	}
	
	public default void onGameWin(RabbitPlayer winner) {
		
	}
	
	public default void onGameReset(Collection<RabbitPlayer> rPlayers) {
		
	}
	
	public default void onFlagSpawn(Location flagSpawnLocation, boolean isRespawned) {
		
	}
	
	public default void onFlagPickUp(RabbitPlayer flagHolder, boolean wasDropped) {
		
	}
	
	public default void onFlagDrop(RabbitPlayer flagDropper, Location flagDropLocation) {
		
	}
	
//	public default void onFlagStatusChange(FlagStatus oldStatus, FlagStatus newStatus) {
//		
//	}
	
	
}
