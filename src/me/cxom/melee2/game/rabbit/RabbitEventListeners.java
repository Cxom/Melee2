package me.cxom.melee2.game.rabbit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.gui.rabbit.RabbitGUI;
import me.cxom.melee2.player.RabbitPlayer;
import net.punchtree.minigames.game.GameState;

/**
 * This class is effectively a part of the Control layer of the MVC
 * As such, it listens to events in the game, and propagates to the M, V, and C layers
 * as appropriate.
 */
class RabbitEventListeners implements Listener {
	
	private final RabbitGameController controller; // Controller
	private final RabbitGame game; // Model
	private final RabbitGUI gui;
	
	RabbitEventListeners(RabbitGameController controller, RabbitGame game, RabbitGUI gui){
		this.controller = controller;
		this.game = game;
		this.gui = gui;
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
		
		Player killer = null;
		EntityDamageByEntityEvent edbee = null;
		
		//Killed by an entity?
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
			propagateKill(killer, killed, edbee);
		} else {
			propagateDeath(killed, e); //Killed by not an entity, or an entity not in the game
		}
		
	}
	
	private void propagateKill(Player killer, Player killed, EntityDamageByEntityEvent edbee) {
		
		if (game.getGameState() != GameState.RUNNING) return;
		
		RabbitPlayer mpKiller = game.getPlayer(killer.getUniqueId());
		RabbitPlayer mpKilled = game.getPlayer(killed.getUniqueId());
		Location killLocation = killed.getLocation(); // GUI is updated after model, but the game will respawn the player, so we save the kill location
		game.handleKill(mpKiller, mpKilled, edbee);
		gui.playKill(mpKiller, mpKilled, edbee, killLocation);
		
//		if (game.hasWinner()) { // This needs to only trigger once
//			propogateWin(mpKiller);
//		};
	}
	
	private void propogateWin(RabbitPlayer winner) {
		gui.playPostgame(winner, MeleeGame.POSTGAME_DURATION_SECONDS);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				game.resetGame();
			}
		}.runTaskLater(Melee.getPlugin(), MeleeGame.POSTGAME_DURATION_SECONDS * 20);
	}
	
	private void propagateDeath(Player killed, EntityDamageEvent e) {
		
		RabbitPlayer mpKilled = game.getPlayer(killed.getUniqueId());
		Location deathLocation = killed.getLocation();
		game.handleDeath(mpKilled, e);
		gui.playDeath(mpKilled, e, deathLocation);
		
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
		
		@EventHandler
		public void onEnvironmentDamage(EntityDamageEvent e) {
			if (entityIsInGame(e.getEntity()) && RabbitGame.damageCauseIsProtected(e.getCause())){
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onFoodLevelChange(FoodLevelChangeEvent e) {
			if (entityIsInGame(e.getEntity())){
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onPlayerRegainHealth(EntityRegainHealthEvent e) {
			if (entityIsInGame(e.getEntity())){
				e.setCancelled(true);
			}
		}
		
		private boolean entityIsInGame(Entity entity) {
			return entity instanceof Player && game.hasPlayer(entity.getUniqueId());
		}
		
		@EventHandler
		public void onPlayerOpenInventoryEvent(InventoryOpenEvent e) {
			if (e.getInventory().getType() != InventoryType.PLAYER && game.hasPlayer(e.getPlayer().getUniqueId())) {			
				e.setCancelled(true);
			}
		}
		
		@EventHandler
		public void onPlayerBlockBreak(BlockBreakEvent e) {
			if (game.hasPlayer(e.getPlayer())) {
				e.setCancelled(true);
			}
		}
		
		@EventHandler
		public void onPlayerBlockPlace(BlockPlaceEvent e) {
			if (game.hasPlayer(e.getPlayer())) {
				e.setCancelled(true);
			}
		}
		
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

