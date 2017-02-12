package me.cxom.melee2.events.custom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.cxom.melee2.Melee;

public class MeleeEventCaller implements Listener {

	@EventHandler
	public void onMeleeDeath(EntityDamageEvent e){
		if (! (Melee.isPlayer(e.getEntity().getUniqueId()))) return;
		Player killed = (Player) e.getEntity();
		if (e.getCause() == DamageCause.FALL) return;
		if (e.getFinalDamage() < killed.getHealth()) return;
		if (e instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) e;
			Entity killer = edbee.getDamager();
			if (killer instanceof Player && Melee.isPlayer(killer.getUniqueId())){
				Bukkit.getServer().getPluginManager().callEvent(new MeleeKillEvent(Melee.getPlayer(killer.getUniqueId()), Melee.getPlayer(killed), edbee));
				return;
			} else if (killer instanceof Arrow && ((Arrow) killer).getShooter() instanceof Player){
				Player shooter = (Player) ((Arrow) killer).getShooter();
				if (Melee.isPlayer(shooter)){
					Bukkit.getServer().getPluginManager().callEvent(new MeleeKillEvent(Melee.getPlayer(shooter), Melee.getPlayer(killed), edbee));
					return;
				}
			}
		}
		Bukkit.getServer().getPluginManager().callEvent(new MeleeDeathEvent(Melee.getPlayer(killed), e));
	}
	
}
