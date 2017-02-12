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
		Player player = (Player) e.getEntity();
		if (e.getCause() == DamageCause.FALL) return;
		if (e.getFinalDamage() < player.getHealth()) return;
		Bukkit.getServer().getPluginManager().callEvent(new MeleeDeathEvent(Melee.getPlayer(player), e));
	}

	@EventHandler
	public void onMeleeKill(MeleeDeathEvent e){
		EntityDamageEvent ede = e.getEntityDamageEvent();
		if (! (ede instanceof EntityDamageByEntityEvent)) return;
		EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) ede;
		Entity killer = edbee.getDamager();
		if (killer instanceof Player && Melee.isPlayer(killer.getUniqueId())){
			Bukkit.getServer().getPluginManager().callEvent(new MeleeKillEvent(Melee.getPlayer(killer.getUniqueId()), e.getMeleePlayer(), edbee));
		} else if (killer instanceof Arrow && ((Arrow) killer).getShooter() instanceof Player){
			Player shooter = (Player) ((Arrow) killer).getShooter();
			if (Melee.isPlayer(shooter)){
				Bukkit.getServer().getPluginManager().callEvent(new MeleeKillEvent(Melee.getPlayer(shooter), e.getMeleePlayer(), edbee));
			}
		}
	}
	
}
