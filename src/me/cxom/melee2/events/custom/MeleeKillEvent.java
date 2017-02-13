package me.cxom.melee2.events.custom;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
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
	
	public static enum AttackMethod{
		
		SLAY("⚔"),
		SHOOT("➵"),
		PUNCH("ლ");//ᕗ
		//ツ 
		
		private final String icon;
		
		private AttackMethod(String icon){
			this.icon = icon;
		}
		
		public String getIcon(){
			return icon;
		}
		
	}
	
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
	
	public AttackMethod getAttackMethod(){
		return isBowKill() ? AttackMethod.SHOOT :
			   ((Player) edbee.getDamager()).getInventory().getItemInMainHand().getType() == Material.STONE_SWORD ? AttackMethod.SLAY : 
			   AttackMethod.PUNCH;
	}
	
}
