package me.cxom.melee2.game;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.events.custom.MeleeDeathEvent;
import me.cxom.melee2.events.custom.MeleeKillEvent;
import me.cxom.melee2.gui.ScrollingScoreboard;
import me.cxom.melee2.player.MeleeColor;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.CirculatingList;
import me.cxom.melee2.util.PlayerUtils;

public class GameInstance implements Listener {

	private final MeleeArena arena;
	public MeleeArena getArena(){ return arena; }
	
	private Set<UUID> players = new HashSet<>();
	
	private MovementSystem movement = MovementPlusPlus.CXOMS_MOVEMENT;
	
	private final ScrollingScoreboard killfeed = new ScrollingScoreboard(Melee.CHAT_PREFIX);
	
	private GameState gamestate = GameState.STOPPED;
	public GameState getGameState(){ return gamestate; }
	/*package*/ void setGameState(GameState gamestate){ this.gamestate = gamestate; } //allows lobby to set GameState.STARING
	
	public GameInstance(MeleeArena arena){
		this.arena = arena;
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
		gamestate = GameState.WAITING;
	}
	
	/*package*/ void start(Set<Player> players){
		CirculatingList<MeleeColor> colors = new CirculatingList<>(MeleeColor.getDefaults(), true);
		for (Player player : players){
			MeleePlayer mp = new MeleePlayer(player, colors.next());
			Melee.addPlayer(mp);
			this.players.add(player.getUniqueId());
			spawnPlayer(mp);
			movement.addPlayer(player);
			killfeed.addPlayer(player);
			player.setInvulnerable(false);
		}
		gamestate = GameState.RUNNING;
	}
	
	private void spawnPlayer(MeleePlayer mp){
		PlayerUtils.perfectStats(mp.getPlayer());
		mp.getPlayer().teleport(arena.getSpawns().next());
	}
	
	private void broadcast(String message){
		for (UUID uuid : players){
			Bukkit.getPlayer(uuid).sendMessage(message);
		}
	}
	
	private void end(){
		for (UUID uuid : players){
			killfeed.removePlayer(Bukkit.getPlayer(uuid));
			movement.removePlayer(uuid);
			Melee.removePlayer(uuid);
			PlayerProfile.restore(uuid);
		}
		players.clear();
		gamestate = GameState.WAITING;
	}
	
	public boolean removePlayer(Player player){
		if (players.contains(player.getUniqueId())){
			//caching? TODO If you leave and rejoin while the match is still in progress, it saves your stats
			killfeed.removePlayer(player);
			movement.removePlayer(player);
			players.remove(player.getUniqueId());
			Melee.removePlayer(player);
			PlayerProfile.restore(player);
			if (players.size() == 1){
				broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "Too many people have left. Shutting down the game :/");
				end();
			}
			return true;
		}
		return false;
	}
	
	public void forceStop(){
		broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "The game has been interrupted. Stopping . . .");
		end();
		gamestate = GameState.STOPPED;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMeleeKill(MeleeKillEvent e){
		if (players.contains(e.getKiller().getUniqueId()) && players.contains(e.getKilledPlayer().getUniqueId())){
			MeleePlayer killer = e.getKiller();
			MeleePlayer killed = e.getKilledPlayer();
			if (killer.equals(killed)){
				onMeleeDeath(e);
				return;
			}
			e.getEntityDamageByEntityEvent().setCancelled(true);
			spawnPlayer(killed);
			killer.incrementKills();	
			killer.getPlayer().sendMessage(Melee.CHAT_PREFIX + ChatColor.GRAY + "You now have "
													+ ChatColor.AQUA + killer.getKills()
													+ ChatColor.GRAY + " kill(s).");
			String killfeedMessage = String.format("%s%s:%d %s %s%s",
					killer.getColor().getChatColor(), killer.getPlayer().getName(), killer.getKills(),
					ChatColor.WHITE + e.getAttackMethod().getIcon(),
					killed.getColor().getChatColor(), killed.getPlayer().getName());
			int length = 39 - String.valueOf(killed.getKills()).length(); //40 chars max - 1 for color = 39
			killfeedMessage = killfeedMessage.length() > length ? killfeedMessage.substring(0, length) : killfeedMessage;
			killfeedMessage += ":" + killed.getKills();
			killfeed.sendMessage(killfeedMessage);
			if (killer.getKills() == arena.getKillsToEnd()){
				broadcast(Melee.CHAT_PREFIX + killer.getColor().getChatColor() + killer.getPlayer().getName() + " has won the game!");
				end();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onMeleeDeath(MeleeDeathEvent e){
		if (players.contains(e.getMeleePlayer().getUniqueId())){
			e.getEntityDamageEvent().setCancelled(true);
			((Player) e.getEntityDamageEvent().getEntity()).setHealth(1);
		}
	}
	
	@EventHandler
	public void onPlayerLeaveGame(PlayerCommandPreprocessEvent e){
		if (e.getMessage().equalsIgnoreCase("/melee leave")){
			if (!removePlayer(e.getPlayer())){
				Melee.getLobby(arena.getName()).removePlayer(e.getPlayer());
			};
		}
	}
	
}
