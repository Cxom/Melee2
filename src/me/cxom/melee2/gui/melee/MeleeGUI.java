package me.cxom.melee2.gui.melee;

import java.util.Collection;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.common.model.AttackMethod;
import me.cxom.melee2.game.melee.MeleeGame;
import me.cxom.melee2.gui.Killfeed;
import me.cxom.melee2.messaging.Messaging;
import me.cxom.melee2.player.MeleePlayer;
import me.cxom.melee2.util.FireworkUtils;

public class MeleeGUI {

	/*
	 * List of features:
	 *  = Chat
	 *   - At player join
	 *   - At kill
	 *   - At game end (win, termination)
	 *   - At player leave
	 *  = Bossbar
	 *   - At start
	 *   - At kill
	 *   - At postgame
	 *  = Scoreboard
	 *   - At kill
	 *  = Tab List
	 *   - At start
	 *   - At kill
	 *   
	 */
	
	// THE GAME
	private MeleeGame game;
	
	
	// THE GUI ELEMENTS
	private final MeleeBossBar bossbar;
	private final Killfeed killfeed;
	private final MeleeTabList tablist;
	
	public MeleeGUI(MeleeGame game){
		this.game = game;
		
		this.bossbar = new MeleeBossBar();
		this.killfeed = new Killfeed(Melee.MELEE_CHAT_PREFIX);
		this.tablist = new MeleeTabList();
	}
	
	public void addPlayer(MeleePlayer mp) {
		Player player = mp.getPlayer();
		
		killfeed.addPlayer(player);
		bossbar.addPlayer(player);		
		tablist.addPlayer(mp);
	}
	
	public void addPlayers(Collection<MeleePlayer> players) {
		players.forEach(this::addPlayer);
	}
	
	
	public void removePlayer(Player player) {
		player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + "" + ChatColor.ITALIC
				+ "Removing you from " + game.getArena().getName() + " . . .");
		
		killfeed.removePlayer(player);
		bossbar.removePlayer(player);
		tablist.removePlayer(player);
	}
	
	// -------- Time Specific -------- //
	
	public void playStart() {
		bossbar.setMessage(ChatColor.RESET + "Now playing on " + ChatColor.ITALIC + game.getArena().getName() + ChatColor.RESET + "!");
		
		sendEveryoneColorMessages();
	}
	
	private void sendEveryoneColorMessages(){
		game.getPlayers().forEach(mp -> {
			Player player = mp.getPlayer();
			
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			player.sendMessage(mp.getColor().getChatColor() + "You are " + mp.getColor().getChatColor().name().replace('_', ' ') + "!");
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		});
	}
	
	public void playKill(MeleePlayer killer, MeleePlayer killed, EntityDamageByEntityEvent e, Location killLocation) {
		
		FireworkUtils.detontateInstantly(FireworkUtils.spawnFirework(killLocation.add(0, 1.1, 0), killed.getColor(), killer.getColor(), 0));
		
		updateBossBarOnKill(killer);
		tablist.updatePlayer(killer);
		
		//Send chat messages
		killer.getPlayer().sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.GRAY + "You now have "
												+ ChatColor.AQUA + killer.getKills()
												+ ChatColor.GRAY + " kill(s).");
		killed.getPlayer().sendMessage(Melee.MELEE_CHAT_PREFIX + killer.getColor().getChatColor() + killer.getPlayer().getName()
											 	+ " (" + killer.getKills() + ") killed you!");

		AttackMethod attackMethod = AttackMethod.getAttackMethod(e.getDamager());
		killfeed.sendKill(killer, killed, attackMethod);
		
	}
	
	private void updateBossBarOnKill(MeleePlayer killer) {
		if (killer.equals(game.getLeader())) {
			bossbar.setLeader(killer);
		} else if (killer.getKills() == game.getLeader().getKills()) {
			bossbar.setTier(killer);
		}
	}
	
	public void playPostgame(MeleePlayer winner, int duration) {
		
		String winMessage = winner.getColor().getChatColor() + winner.getPlayer().getName() + ChatColor.WHITE + " has won the game!";
		bossbar.setMessage(winMessage);
		broadcast(Melee.MELEE_CHAT_PREFIX + winMessage);
		
		Color winnersColor = winner.getColor().getBukkitColor();
		new BukkitRunnable(){
			int i = duration - 1; // Subtract 1 so the first firework doesn't go off immediately
			Random r = new Random();
			@Override
			public void run(){
				if (i > 0){
					FireworkUtils.spawnFirework(game.getSpawns().next(), winnersColor, r.nextInt(2) + 1);
					i--;
				} else {
					this.cancel();
				}
			}
		}.runTaskTimer(Melee.getPlugin(), 10, 30);
		
	}
	
	public void playTooManyLeft() {
		broadcast(Melee.MELEE_CHAT_PREFIX + ChatColor.RED + "Too many people have left. Shutting down the game :/");
	}
	
	public void broadcast(String message){
		game.getPlayers().forEach(mp -> mp.getPlayer().sendMessage(message));
	}

	public void reset() {
		killfeed.removeAll();
		bossbar.removeAll();
		tablist.removeAll();
		bossbar.reset();
	}

	public void playStop() {
		broadcast(Messaging.GAME_INTERRUPTED.get());
		bossbar.setMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Game Interrupted - Stopping...");
		bossbar.setColor(BarColor.RED);
	}

	public void playDeath(MeleePlayer mpKilled, EntityDamageEvent e, Location deathLocation) {
	
		// Currently, non-kill death doesn't happen, so nothing we want to do
		
	}

	
	
}
