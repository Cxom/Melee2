package me.cxom.melee2.game.melee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.cxom.melee2.Melee;

/**
 * This class is effectively a part of the Control layer of the MVC
 * As such, it listens to events in the game, and propagates to the M, V, and C layers
 * as appropriate.
 */
class MeleeEventListeners implements Listener {
	
	private final MeleeGameController controller; // Controller
	private final MeleeGame game; // Model
	
	MeleeEventListeners(MeleeGameController controller, MeleeGame game){
		this.controller = controller;
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	// Kill and Death events
	
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent e){
		
		UUID entityId = e.getEntity().getUniqueId();
		
		//Is the player in the game?
		if (!game.hasPlayer(entityId)) return;
		Player killed = (Player) e.getEntity();
		
		//Is the damage lethal?
		if (e.getFinalDamage() < killed.getHealth()) return;
		
		Player killer = null;
		EntityDamageByEntityEvent edbee = null;
		
		//Was the player killed by an entity?
		if (e instanceof EntityDamageByEntityEvent){
			edbee = (EntityDamageByEntityEvent) e;
			Entity killingEntity = edbee.getDamager();
			
			// Determine killer
			if (killingEntity instanceof Player && game.hasPlayer(killingEntity.getUniqueId())){
				//Killed by a player (also in game)
				killer = (Player) killingEntity;	
				
			} else if (killingEntity instanceof Arrow && ((Arrow) killingEntity).getShooter() instanceof Player){
				//Killed by an arrow shot by a player (not sure if player in game yet)
				
				Player shooter = (Player) ((Arrow) killingEntity).getShooter();
				killingEntity.remove();
				
				if (game.hasPlayer(shooter.getUniqueId())){
					//Player who shot arrow is in game
					killer = shooter;
					
				}
			}
		}
		
		if (killer != null) {
			controller.propagateKill(killer, killed, edbee);
		} else {
			controller.propagateDeath(killed, e); //Killed by not an entity, or an entity not in the game
		}
		
	}
	
	
	
	
	// Quit & Leave Events
	
		@EventHandler
		public void onPlayerLeaveServer(PlayerQuitEvent e){
			if ( ! controller.removePlayerFromGame(e.getPlayer())) {
				 controller.removePlayerFromLobby(e.getPlayer());
			}
		}
		
		//This was a preprocess event so that the game the player is in can be determined,
		//but this could probably be done properly
		@EventHandler
		public void onMeleeLeaveCommand(PlayerCommandPreprocessEvent e) {
			if (! e.getMessage().startsWith("/melee leave")) return;
			if (controller.removePlayerFromGame(e.getPlayer()) || controller.removePlayerFromLobby(e.getPlayer())){
				e.setCancelled(true); //Prevents normal command execution
			}
		}

	// Cancelled Events
		
		private static final List<String> cmds = new ArrayList<String>(Arrays.asList(new String[] {
				"/m", "/msg", "/message", "/t", "/tell", "/w", "/whisper", "/r",
				"/reply", "/ac", "/helpop"}));

		@EventHandler
		public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
			Player player = e.getPlayer();
			String command = e.getMessage().toLowerCase() + " ";
			if ((game.hasPlayer(player) || controller.getLobby().hasPlayer(player))
			 && ! player.isOp()
			 && ! cmds.contains(command.split(" ")[0])
			 && ! command.toLowerCase().startsWith("/melee leave")) {
				
				e.setCancelled(true);
				player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + "You do not have permission to use non-messaging commands in Melee. If you wish to leave the match, type /melee leave.");
				
			}
			
		}
		
}
