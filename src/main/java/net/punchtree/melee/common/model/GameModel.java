package net.punchtree.melee.common.model;

import net.punchtree.melee.arena.MeleeArena;

public abstract class GameModel {

	private String gameName;
	
	public abstract MeleeArena getArena();
	
}
