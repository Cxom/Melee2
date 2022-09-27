package me.cxom.melee2.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.cxom.melee2.game.MeleeLikeGame;
import me.cxom.melee2.game.melee.MeleeGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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

import me.cxom.melee2.Melee;

/**
 * This class is effectively a part of the Control layer of the MVC
 * As such, it listens to events in the game, and propagates to the M, V, and C layers
 * as appropriate.
 */
public class MeleeLikeEventListeners implements Listener {
	
	private final MeleeLikeGame game;
	
	public MeleeLikeEventListeners(MeleeLikeGame game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	// Kill and Death events
	
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent e){

		//Is the player in the game?
		if (!isPlayerInGame(e.getEntity())) return;
		Player killed = (Player) e.getEntity();

		//Is it disabled damage?
		if (MeleeGame.damageCauseIsProtected(e.getCause())) {
			e.setCancelled(true);
			return;
		}
		
		//Is the damage lethal?
		if (!isDamageLethal(e, killed)) return;
		
		Player killer = null;
		EntityDamageByEntityEvent edbee = null;
		
		//Was the player killed by an entity?
		if (e instanceof EntityDamageByEntityEvent){
			edbee = (EntityDamageByEntityEvent) e;
			Entity killingEntity = edbee.getDamager();
			
			// Determine killer
			if (isPlayerInGame(killingEntity)){
				killer = (Player) killingEntity;	
			} else if (isShotByPlayerInGame(killingEntity)){
				killer = (Player) ((Projectile) killingEntity).getShooter();
				killingEntity.remove(); // Don't keep arrows around
			}
		}
		
		if (killer != null) {
			game.handleKill(killer, killed, edbee);
		} else {
			game.handleDeath(killed, e); //Killed by non-entity, or an entity not in the game
		}
		
	}
	
	private boolean isPlayerInGame(Entity killingEntity) {
		return killingEntity instanceof Player && game.hasPlayer(killingEntity.getUniqueId());
	}

	private boolean isDamageLethal(EntityDamageEvent ede, Player killed) {
		return ede.getFinalDamage() >= killed.getHealth();
	}

	private boolean isShotByPlayerInGame(Entity killingEntity) {
		return killingEntity instanceof Projectile projectile
				&& projectile.getShooter() instanceof Player shooter
				&& isPlayerInGame(shooter);
	}
	
	
	
	// Quit & Leave Events
	
	@EventHandler
	public void onPlayerLeaveServer(PlayerQuitEvent e){
		game.removePlayerFromGame(e.getPlayer());
	}
	
	//This was a preprocess event so that the game the player is in can be determined,
	//but this could probably be done properly
	@EventHandler
	public void onMeleeLeaveCommand(PlayerCommandPreprocessEvent e) {
		if (! e.getMessage().startsWith("/leave")) return;
		if (game.removePlayerFromGame(e.getPlayer())){
			e.setCancelled(true); //Prevents normal command execution
		}
	}

	// Cancelled Events

	// Environment damage is cancelled in the EntityDamageEvent handler up above

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
			"/reply", "/ac", "/helpop", "/leave"}));

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		String command = e.getMessage().toLowerCase() + " ";
		if ((game.hasPlayer(player))
		 && ! player.isOp()
		 && ! cmds.contains(command.split(" ")[0])) {
			
			e.setCancelled(true);
			player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + "You do not have permission to use non-messaging commands in Melee. If you wish to leave the match, type /melee leave.");
			
		}
		
	}
		
}
