package me.cxom.melee2.game;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.game.combat.AttackMethod;
import me.cxom.melee2.game.gui.MeleeBossBar;
import me.cxom.melee2.game.gui.ScrollingScoreboard;
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
	private final ScrollingScoreboard killfeed;
	
	
	public MeleeGUI(MeleeGame game){
		this.game = game;
		
		this.bossbar = new MeleeBossBar();
		this.killfeed = new ScrollingScoreboard(Melee.CHAT_PREFIX);
	}
	
	public void addPlayer(MeleePlayer mp) {
		Player player = mp.getPlayer();
		
		//Add to killfeed and bossbar
		killfeed.addPlayer(player);
		bossbar.addPlayer(player);
		
		//Set tablist
		player.setPlayerListName(mp.getColor().getChatColor() + player.getName() + " " + ChatColor.GRAY + mp.getKills());
	}
	
	public void removePlayer(Player player) {
		player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "" + ChatColor.ITALIC
				+ "Removing you from " + game.getArena().getName() + " . . .");
		
		killfeed.removePlayer(player);
		bossbar.removePlayer(player);
		player.setPlayerListName(player.getName());
	}
	
	// -------- Time Specific -------- //
	
	void playStart() {
		bossbar.setMessage(ChatColor.RESET + "Now playing on " + ChatColor.ITALIC + game.getArena().getName() + ChatColor.RESET + "!");
		
		game.getPlayers().forEach(mp -> {
			Player player = mp.getPlayer();
			
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			player.sendMessage(mp.getColor().getChatColor() + "You are " + mp.getColor().getChatColor().name().replace('_', ' ') + "!");
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		});
		
	}

	
	public void playKill(MeleePlayer killer, MeleePlayer killed, EntityDamageByEntityEvent e) {
		
		FireworkUtils.detontateInstantly(FireworkUtils.spawnFirework(killed.getPlayer().getLocation().add(0, 1.1, 0), killed.getColor(), killer.getColor(), 0));
		
		
		if (killer.equals(game.getLeader())) {
			bossbar.setLeader(killer);
		} else if (killer.getKills() == game.getLeader().getKills()) {
			bossbar.setTier(killer);
		}
		
		
		//Update tablist
		killer.getPlayer().setPlayerListName(killer.getColor().getChatColor() + killer.getPlayer().getName() + " " + ChatColor.GRAY + killer.getKills());
		
		
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
		
		
	}
	
	
	void playPostgame(MeleePlayer winner, int duration) {
		
		String winMessage = winner.getColor().getChatColor() + winner.getPlayer().getName() + ChatColor.WHITE + " has won the game!";
		bossbar.setMessage(winMessage);
		broadcast(Melee.CHAT_PREFIX + winMessage);
		
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
		}.runTaskTimer(Melee.getPlugin(), 10, 20);
		
	}
	
	/**
	 * Sends a message to all players in the game
	 * @param message
	 */
	void broadcast(String message){
		game.getPlayers().forEach(mp -> mp.getPlayer().sendMessage(message));
	}

	public void reset() {
		killfeed.removeAll();
		bossbar.removeAll();
		game.getPlayers().forEach(mp -> mp.getPlayer().setPlayerListName(mp.getPlayer().getName()));
		bossbar.reset();
	}

	public void playStop() {
		broadcast(Melee.CHAT_PREFIX + ChatColor.RED + "The game has been interrupted. Stopping . . .");
		bossbar.setMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Game Interrupted - Stopping...");
		bossbar.setColor(BarColor.RED);
	}

	
	
}
