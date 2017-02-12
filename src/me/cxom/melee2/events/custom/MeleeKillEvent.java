package me.cxom.melee2.events.custom;

import org.bukkit.entity.Arrow;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import me.cxom.melee2.player.MeleePlayer;

public class MeleeKillEvent extends MeleeDeathEvent{
	////
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	////
	
	private final MeleePlayer killer;
	private final MeleePlayer killed;
	private final EntityDamageByEntityEvent edbee;
	
	public MeleeKillEvent(MeleePlayer killer, MeleePlayer killed, EntityDamageByEntityEvent edbee) {
		super(killed, edbee);	
		this.killer = killer;
		this.killed = killed;
		this.edbee = edbee;
	}

	public MeleePlayer getKiller() {
		return killer;
	}
	
	public MeleePlayer getKilledPlayer(){
		return killed;
	}
	
	public EntityDamageEvent getEntityDamageByEntityEvent(){
		return edbee;
	}
	
	public boolean isBowKill(){
		return edbee.getDamager() instanceof Arrow;
	}
	
}
