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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.cxom.melee2.Melee;

public class MeleeEventListeners implements Listener {

	// Listeners belong in this class if the represent a change in the model
	// That 
	
	private final MeleeGameController controller;
	private final MeleeGame game;
	
	MeleeEventListeners(MeleeGameController controller, MeleeGame game){
		this.controller = controller;
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	// Kill and Death events
	
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent e){
		//Player's in game?
		if (! (game.hasPlayer(e.getEntity().getUniqueId()))) return;
		Player killed = (Player) e.getEntity();
		
		//Actually killed?
		if (e.getFinalDamage() < killed.getHealth()) return;
		
		//Killed by an entity?
		if (e instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) e;
			Entity killer = edbee.getDamager();
			
			//Killed by a player (also in game)?
			if (killer instanceof Player && game.hasPlayer(killer.getUniqueId())){
				controller.handleKill(game.getPlayer(killer.getUniqueId()), game.getPlayer(killed.getUniqueId()), edbee);
				return;
				
			//Killed by an arrow shot by a player?
			} else if (killer instanceof Arrow && ((Arrow) killer).getShooter() instanceof Player){
				Player shooter = (Player) ((Arrow) killer).getShooter();
				killer.remove();
				
				//Player who shot arrow is in game?
				if (game.hasPlayer(shooter.getUniqueId())){
					
					//TODO Change the name of this method - handleKill?
					controller.handleKill(game.getPlayer(shooter.getUniqueId()), game.getPlayer(killed.getUniqueId()), edbee);
					return;
				}
			}
		}
		//Killed by not an entity, or an entity not in the game
		controller.handleDeath(killed, e);
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
				player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "You do not have permission to use non-messaging commands in Melee. If you wish to leave the match, type /melee leave.");
				
			}
			
		}
		
}
