package me.cxom.melee2.game;

import me.cxom.melee2.arena.Arena;
import me.cxom.melee2.common.model.GameState;

public interface PvpGame {
	
	public GameState getGameState();
	
//	default public boolean isAvailable() {
//		return getGameState() == GameState.WAITING;
//	}
	
	default public boolean isStopped() {
		return getGameState() == GameState.STOPPED;
	}
	
	default public boolean isWaiting() {
		return getGameState() == GameState.WAITING;
	}
	
	public Arena getArena();
	
}
