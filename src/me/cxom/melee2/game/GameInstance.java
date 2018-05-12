package me.cxom.melee2.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

import me.cxom.melee2.Melee;
import me.cxom.melee2.arena.MeleeArena;
import me.cxom.melee2.game.combat.AttackMethod;
import me.cxom.melee2.gui.game.MeleeBossBar;
import me.cxom.melee2.gui.game.ScrollingScoreboard;
import me.cxom.melee2.player.MeleeColor;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.player.PlayerProfile;
import me.cxom.melee2.util.CirculatingList;
import me.cxom.melee2.util.FireworkUtils;
import me.cxom.melee2.util.PlayerUtils;

public class GameInstance {

	private final MeleeArena arena;
	private final Lobby lobby;
	
	private final MovementSystem movement = MovementPlusPlus.CXOMS_MOVEMENT; //If the old API doesn't like this, change type back to CxomsMovement
	private final MeleeBossBar bossbar;
	private final ScrollingScoreboard killfeed = new ScrollingScoreboard(Melee.CHAT_PREFIX);	

	private GameState gamestate = GameState.STOPPED;
	
	Map<UUID, MeleePlayer> players = new HashMap<>();
	
	private int mostKills = 0;
	
	public GameInstance(MeleeArena arena){
		this.arena = arena;
		this.lobby = new Lobby(this);
		this.bossbar = new MeleeBossBar();
		
		new MeleeEventListeners(this);
		
		reset();
	}
	
	//Methods
	public void addPlayer(Player player) {
		PlayerProfile.save(player);
		lobby.addPlayer(player);
	}
	
	private void broadcast(String message){
		for (UUID uuid : players.keySet()){
			Bukkit.getPlayer(uuid).sendMessage(message);
		}
	}
	
	private void spawnPlayer(MeleePlayer mp){
		Player player = mp.getPlayer();
		
		PlayerUtils.perfectStats(player);
		player.teleport(arena.getSpawns().next());
	}
	
	
	
	void start(Set<Player> startingPlayers){
		
		if (players.size() > 0) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/mail Cxomtdoh a MeleeInstance attempted to start with players still in the set!");
			throw new AssertionError("There should be no players in MeleeInstance before game start!"
					+ " Something is not clearing properly.");
		}
		
		CirculatingList<MeleeColor> colors = new CirculatingList<>(MeleeColor.getDefaults(), true);
		for (Player player : startingPlayers){
			
			/* trace message */ System.out.println("Initializing player " + player.getName());
			
			MeleePlayer mp = new MeleePlayer(player, colors.next());
			players.put(player.getUniqueId(), mp);
			
			
			spawnPlayer(mp);
			movement.addPlayer(player);
			bossbar.addPlayer(player);
			killfeed.addPlayer(player);
			player.setInvulnerable(false);
			
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			player.sendMessage(mp.getColor().getChatColor() + "You are " + mp.getColor().getChatColor().name().replace('_', ' ') + "!");
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
		
		gamestate = GameState.RUNNING;
		
	}
	
	private void postgame(MeleePlayer winner) {
		
		this.gamestate = GameState.ENDING; 
		
		String winMessage = winner.getColor().getChatColor() + winner.getPlayer().getName() + ChatColor.WHITE + " has won the game!";
		bossbar.setMessage(winMessage);
		for (UUID uuid : players.keySet()){
			Player player = Bukkit.getPlayer(uuid);
			player.sendMessage(Melee.CHAT_PREFIX + winMessage);
		}
		
		Color winnersColor = winner.getColor().getBukkitColor();
		new BukkitRunnable(){
			int i = 10; //10 seconds
			Random r = new Random();
			@Override
			public void run(){
				if (i <= 0){
					this.cancel();
					reset();
					return;
				}
				FireworkUtils.spawnFirework(arena.getSpawns().next(), winnersColor, r.nextInt(2) + 2);
				i--;
			}
		}.runTaskTimer(Melee.getPlugin(), 10, 20);
	}
	
	private void reset(){
		
		for (UUID uuid : players.keySet()){
			Bukkit.broadcastMessage("removing/restoring " + Bukkit.getPlayer(uuid).getName());
//			killfeed.removePlayer(Bukkit.getPlayer(uuid)); Is this not redundant?
			movement.removePlayer(uuid);
			
			PlayerProfile.restore(uuid);
		}
		
		//lobby.removeAll(); What was this doing here?????
		killfeed.removeAll();
		bossbar.removeAll();
		bossbar.setMessage(ChatColor.RESET + "Now playing on " + ChatColor.ITALIC + arena.getName() + ChatColor.RESET + "!");
		
		players.clear();
		
		mostKills = 0;
		
		gamestate = GameState.WAITING;
	}
	
	public boolean removePlayer(Player player){
		if (players.containsKey(player.getUniqueId())){
			
			//TODO maybe send message if player still in 
			
			//caching? TODO If you leave and rejoin while the match is still in progress, it saves your stats
			killfeed.removePlayer(player);
			bossbar.removePlayer(player);
			movement.removePlayer(player);
			players.remove(player.getUniqueId());
			
			PlayerProfile.restore(player); //This restores location
			
			if (gamestate == GameState.RUNNING && players.size() == 1){
				broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "Too many people have left. Shutting down the game :/");
				reset();
			}
			
			return true;
		
		} else {
			
			return lobby.removePlayer(player);
			
		}
	}
	
	public void forceStop(){
		broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "The game has been interrupted. Stopping . . .");
		reset(); //TODO Maybe split reset and removeAll up
		lobby.removeAll();
		gamestate = GameState.STOPPED;
	}
	
	// Melee Kills and Deaths
	
	void onMeleeKill(MeleePlayer killer, MeleePlayer killed, EntityDamageByEntityEvent e){
			
		//if suicide, not a kill
		if (killer.equals(killed)){
			onMeleeDeath(killed.getPlayer(), e);
			return;
		}
		
		//cancel damage
		e.setCancelled(true); //TODO Maybe refactor into cancelDamage method in MeleeKillEvent?
		
		FireworkUtils.detontateInstantly(FireworkUtils.spawnFirework(killed.getPlayer().getLocation().add(0, 1.1, 0), killed.getColor(), killer.getColor(), 0));
		
		spawnPlayer(killed);
		
		killer.incrementKills();
		if (killer.getKills() > mostKills){
			bossbar.setLeader(killer);
			mostKills = killer.getKills();
		} else if (killer.getKills() == mostKills){
			bossbar.setTier(killer);
		}
		
		//Send chat messages
		killer.getPlayer().sendMessage(Melee.CHAT_PREFIX + ChatColor.GRAY + "You now have "
												+ ChatColor.AQUA + killer.getKills()
												+ ChatColor.GRAY + " kill(s).");
		killed.getPlayer().sendMessage(Melee.CHAT_PREFIX + killer.getColor().getChatColor() + killer.getPlayer().getName()
											 	+ " (" + killer.getKills() + ") killed you!");
		
		//Send killfeed message
		String killfeedMessage = String.format("%s%s:%d %s %s%s",
				killer.getColor().getChatColor(), killer.getPlayer().getName(), killer.getKills(),
				ChatColor.WHITE + AttackMethod.getAttackMethod(e.getDamager()).getIcon(),
				killed.getColor().getChatColor(), killed.getPlayer().getName());
		int length = 39 - String.valueOf(killed.getKills()).length(); //40 chars max - 1 for color = 39
		killfeedMessage = killfeedMessage.length() > length ? killfeedMessage.substring(0, length) : killfeedMessage;
		killfeedMessage += ":" + killed.getKills();
		killfeed.sendMessage(killfeedMessage);
		
		
		if (killer.getKills() == arena.getKillsToEnd()){
			postgame(killer);
		}
		
	}
	
	void onMeleeDeath(Player killed, EntityDamageEvent e){
		e.setCancelled(true);
		//Let player take non-fatal damage that isn't caused by fall damage
		if (e.getCause() != DamageCause.FALL){
			((Player) e.getEntity()).setHealth(1);
		}
	}
	
	//----Setters----//
	
	//allows lobby to set GameState.STARING
	void setGameState(GameState gamestate){
		this.gamestate = gamestate; 
	} 
	
	//----Getters----//
	
	public GameState getGameState(){
		return gamestate; 
	}
	
	public Lobby getLobby(){
		return lobby;
	}
	
	public MeleeArena getArena(){
		return arena;
	}
	
	//----Debug Methods----//
	
	public void debug(Player player) {
		player.sendMessage("Game Players: " + players.keySet().stream()
													  .map(u -> Bukkit.getPlayer(u).getName())
													  .collect(Collectors.toList()));
		player.sendMessage("Game State: " + gamestate);
		player.sendMessage("Lobby players: " + lobby.getWaitingPlayers().stream().map(Player::getName).collect(Collectors.toList()));
	}
	
}
