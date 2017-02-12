package me.cxom.melee2.game;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.events.custom.MeleeDeathEvent;
import me.cxom.melee2.events.custom.MeleeKillEvent;
import me.cxom.melee2.player.MeleeColor;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.CirculatingList;

public class GameInstance implements Listener {

	private final MeleeArena arena;
	
	private Set<UUID> players = new HashSet<>();
	
	public GameInstance(MeleeArena arena){
		this.arena = arena;
		Bukkit.getServer().getPluginManager().registerEvents(this, Melee.getPlugin());
	}
	
	public void start(Set<Player> players){
		CirculatingList<MeleeColor> colors = new CirculatingList<>(MeleeColor.getDefaults(), true);
		for (Player player : players){
			MeleePlayer mp = new MeleePlayer(player, colors.next());
			Melee.addPlayer(mp);
			spawnPlayer(mp);
		}
	}
	
	public void spawnPlayer(MeleePlayer mp){
		mp.perfectStats();
		mp.getPlayer().teleport(arena.getSpawns().next());
	}
	
	public void broadcast(String message){
		for (UUID uuid : players){
			Bukkit.getPlayer(uuid).sendMessage(message);
		}
	}
	
	public void end(){
		for (UUID uuid : players){
			PlayerProfile.restore(uuid);
		}
	}
	
	public void removePlayer(Player player){
		if (players.contains(player.getUniqueId())){
			//caching? TODO If you leave and rejoin while the match is still in progress, it saves your stats
			Melee.removePlayer(player);
			PlayerProfile.restore(player);
		}
	}
	
	public void forceStop(){
		broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "The game has been interrupted. Stopping . . .");
		end();
	}
	
	@EventHandler
	public void onMeleeKill(MeleeKillEvent e){
		if (players.contains(e.getKiller().getUniqueId()) && players.contains(e.getKilledPlayer().getUniqueId())){
			spawnPlayer(e.getKilledPlayer());
			e.getKiller().getPlayer().sendMessage(Melee.CHAT_PREFIX + ChatColor.GRAY + "You now have "
													+ ChatColor.AQUA + e.getKiller().getKills()
													+ ChatColor.GRAY + " kills.");
			e.getKiller().incrementKills();		
			if (e.getKiller().getKills() == arena.getKillsToEnd()){
				broadcast(Melee.CHAT_PREFIX + e.getKiller().getColor().getChatColor() + e.getKiller().getPlayer().getName() + " has won the game!");
				end();
			}
		}
	}
	
	@EventHandler
	public void onMeleeDeath(MeleeDeathEvent e){
		if (! (e instanceof MeleeKillEvent) && players.contains(e.getMeleePlayer().getUniqueId())){
			e.getEntityDamageEvent().setCancelled(true);
			((Player) e.getEntityDamageEvent().getEntity()).setHealth(1);
		}
	}
	
}
