package me.cxom.melee2.events.custom;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

import me.cxom.melee2.player.MeleePlayer;

public class MeleeDeathEvent extends Event {
	////
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	////
	
	private final MeleePlayer mp;
	private final EntityDamageEvent ede;
	
	public MeleeDeathEvent(MeleePlayer mp, EntityDamageEvent ede) {
			this.mp = mp;
			this.ede = ede;
	}

	public MeleePlayer getMeleePlayer() {
		return mp;
	}
	
	public EntityDamageEvent getEntityDamageEvent(){
		return ede;
	}
	
}
