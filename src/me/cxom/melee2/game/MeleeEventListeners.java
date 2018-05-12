package me.cxom.melee2.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.cxom.melee2.Melee;

public class MeleeEventListeners implements Listener {

	
	private final GameInstance game;
	
	MeleeEventListeners(GameInstance game){
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	// Kill and Death events
	
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent e){
		//Player's in game?
		if (! (game.players.containsKey(e.getEntity().getUniqueId()))) return;
		Player killed = (Player) e.getEntity();
		
		//Actually killed?
		if (e.getFinalDamage() < killed.getHealth()) return;
		
		//Killed by an entity?
		if (e instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) e;
			Entity killer = edbee.getDamager();
			
			//Killed by a player (also in game)?
			if (killer instanceof Player && game.players.containsKey(killer.getUniqueId())){
				game.onMeleeKill(game.players.get(killer.getUniqueId()), game.players.get(killed.getUniqueId()), edbee);
				return;
				
			//Killed by an arrow shot by a player?
			} else if (killer instanceof Arrow && ((Arrow) killer).getShooter() instanceof Player){
				Player shooter = (Player) ((Arrow) killer).getShooter();
				killer.remove();
				
				//Player who shot arrow is in game?
				if (game.players.containsKey(shooter.getUniqueId())){
					
					game.onMeleeKill(game.players.get(shooter.getUniqueId()), game.players.get(killed.getUniqueId()), edbee);
					return;
				}
			}
		}
		//Killed by not an entity, or an entity not in the game
		game.onMeleeDeath(killed, e);
	}
	
	// Quit & Leave Events
	
		@EventHandler
		public void onPlayerLeaveServer(PlayerQuitEvent e){
			game.removePlayer(e.getPlayer());
		}
		
		//I *think* that this was a preprocess event so that the game the player was in was available,
		//but this could probably be done properly
		@EventHandler
		public void onMeleeLeaveCommand(PlayerCommandPreprocessEvent e) {
			if (! e.getMessage().startsWith("/melee leave")) return;
			if (game.removePlayer(e.getPlayer())){
				e.setCancelled(true); //Prevents normal command excecution
			}
		}

	// Cancelled Events
		
		private final List<String> cmds = new ArrayList<String>(Arrays.asList(new String[] {
				"/m", "/msg", "/message", "/t", "/tell", "/w", "/whisper", "/r",
				"/reply", "/ac", "/helpop"}));

		@EventHandler
		public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
			Player player = e.getPlayer();
			String command = e.getMessage().toLowerCase() + " ";
			if (game.players.containsKey(player.getUniqueId())
			 && ! player.isOp()
			 && ! cmds.contains(command.split(" ")[0])
			 && ! command.toLowerCase().startsWith("/melee leave")) {
				
				e.setCancelled(true);
				player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "You do not have permission to use non-messaging commands in Melee. If you wish to leave the match, type /melee leave.");
				
			}
			
		}
		
		// Cancelling entity-explosion prevents damage from fireworks
		@EventHandler
		public void onFallDamage(EntityDamageEvent e) {
			if (e.getEntity() instanceof Player
			 && game.players.containsKey(e.getEntity().getUniqueId()) 
	    	 && (e.getCause() == DamageCause.FALL || e.getCause() == DamageCause.ENTITY_EXPLOSION)){
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onFoodLevelChange(FoodLevelChangeEvent e) {
			if (e.getEntity() instanceof Player && game.players.containsKey(e.getEntity().getUniqueId())){
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onPlayerRegainHealth(EntityRegainHealthEvent e) {
			if (e.getEntity() instanceof Player && game.players.containsKey(e.getEntity().getUniqueId())){
				e.setCancelled(true);
			}
		}
		
}
