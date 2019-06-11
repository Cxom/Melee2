package me.cxom.melee2.gui.rabbit;

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
import org.bukkit.scheduler.BukkitTask;

import me.cxom.melee2.Melee;
import me.cxom.melee2.common.model.AttackMethod;
import me.cxom.melee2.game.rabbit.RabbitGame;
import me.cxom.melee2.game.rabbit.RabbitGame.FlagStatus;
import me.cxom.melee2.game.rabbit.RabbitGameObserver;
import me.cxom.melee2.gui.Killfeed;
import me.cxom.melee2.gui.MinigameBossBar;
import me.cxom.melee2.gui.melee.MeleeTabList;
import me.cxom.melee2.player.RabbitPlayer;
import me.cxom.melee2.util.FireworkUtils;

public class RabbitGUI implements RabbitGameObserver {

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
	private RabbitGame game;
	
	
	// THE GUI ELEMENTS
	private final MinigameBossBar bossbar;
	private final Killfeed killfeed;
	//TODO make tablist to record time, not kills -> RabbitTabList
	private final MeleeTabList tablist;
	
	public RabbitGUI(RabbitGame game){
		this.game = game;
		game.registerObserver(this);
		
		this.bossbar = new MinigameBossBar();
		this.killfeed = new Killfeed(Melee.RABBIT_CHAT_PREFIX);
		this.tablist = new MeleeTabList();
	}
	
	public void addPlayer(RabbitPlayer mp) {
		Player player = mp.getPlayer();
		killfeed.addPlayer(player);
		bossbar.addPlayer(player);
		tablist.addPlayer(mp);
	}
	
	public void addPlayers(Collection<RabbitPlayer> players) {
		players.forEach(this::addPlayer);
	}
	
	
	public void removePlayer(Player player) {
		player.sendMessage(Melee.RABBIT_CHAT_PREFIX + ChatColor.RED + "" + ChatColor.ITALIC
				+ "Removing you from " + game.getArena().getName() + " . . .");
		
		killfeed.removePlayer(player);
		bossbar.removePlayer(player);
		tablist.removePlayer(player);
	}
	
	// -------- Time Specific -------- //
	
	public void playStart() {
		bossbar.setMessage(ChatColor.RESET + "Now playing on " + ChatColor.ITALIC + game.getArena().getName() + ChatColor.RESET + "!");
		bossbar.setColor(BarColor.YELLOW);
		
		final int nowPlayingMessageDuration = 3;
		
		new BukkitRunnable() {
			
			int countdown = game.getInitialFlagSpawnDelay() - nowPlayingMessageDuration;
			
			public void run() {
				bossbar.setMessage(flagString + ChatColor.RED + " The flag will spawn in " + ChatColor.GRAY + countdown + ChatColor.RED + " seconds! " + flagString);
				countdown--;
				if (countdown <= 0) {
					this.cancel();
				}
			}
		}.runTaskTimerAsynchronously(Melee.getPlugin(), 20 * nowPlayingMessageDuration, 20);
		
		game.getPlayers().forEach(mp -> {
			Player player = mp.getPlayer();
			
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			player.sendMessage(mp.getColor().getChatColor() + "You are " + mp.getColor().getChatColor().name().replace('_', ' ') + "!");
			player.sendMessage(mp.getColor().getChatColor() + "" + ChatColor.BOLD + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		});
		
	}

	
	public void playKill(RabbitPlayer killer, RabbitPlayer killed, EntityDamageByEntityEvent e, Location killLocation) {
		
		FireworkUtils.detontateInstantly(FireworkUtils.spawnFirework(killLocation.add(0, 1.1, 0), killed.getColor(), killer.getColor(), 0));
		
		
//		if (killer.equals(game.getLeader())) {
//			bossbar.setLeader(killer);
//		} else if (killer.getKills() == game.getLeader().getKills()) {
//			bossbar.setTier(killer);
//		}
		
		tablist.updatePlayer(killer);
		
		
		//Send chat messages
		killer.getPlayer().sendMessage(Melee.RABBIT_CHAT_PREFIX + ChatColor.GRAY + "You now have "
												+ ChatColor.AQUA + killer.getKills()
												+ ChatColor.GRAY + " kill(s).");
		killed.getPlayer().sendMessage(Melee.RABBIT_CHAT_PREFIX + killer.getColor().getChatColor() + killer.getPlayer().getName()
											 	+ " (" + killer.getKills() + ") killed you!");
		
		
		//Send killfeed message
		AttackMethod attackMethod = AttackMethod.getAttackMethod(e.getDamager());
		killfeed.sendKill(killer, killed, attackMethod);
	}
	
	
	public void playPostgame(RabbitPlayer winner, int duration) {
		
		String winMessage = winner.getColor().getChatColor() + winner.getPlayer().getName() + ChatColor.WHITE + " has won the game!";
		bossbar.setMessage(winMessage);
		broadcast(Melee.RABBIT_CHAT_PREFIX + winMessage);
		
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
		broadcast(Melee.RABBIT_CHAT_PREFIX + ChatColor.RED + "Too many people have left. Shutting down the game :/");
	}

	public void broadcast(String message){
		game.getPlayers().forEach(mp -> mp.getPlayer().sendMessage(message));
	}

	@Override
	public void onGameReset(Collection<RabbitPlayer> rabbitPlayers) {
		reset();
	}
	
	private void reset() {
		// TODO change all these to use suppliers?
		killfeed.removeAll();
		bossbar.removeAll();
		tablist.removeAll();
		bossbar.reset();
	}

	public void playStop() {
		broadcast(Melee.RABBIT_CHAT_PREFIX + ChatColor.RED + "The game has been interrupted. Stopping . . .");
		bossbar.setMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Game Interrupted - Stopping...");
		bossbar.setColor(BarColor.RED);
	}

	public void playDeath(RabbitPlayer rpKilled, EntityDamageEvent e, Location deathLocation) {
	
		// Currently, non-kill death doesn't happen, so nothing we want to do
		
	}
	
//	public void onFlagStatusChange(FlagStatus oldStatus, FlagStatus newStatus){
		
		
		
//		if(newStatus == FlagStatus.NOT_IN_PLAY) return; //Do nothing at the end of the game
//		
//		switch(oldStatus) {
//		case NOT_IN_PLAY: //NOT_IN_PLAY only goes to SPAWNED at beginning of game
//			playFlagFirstSpawn();
//			break;
//		case SPAWNED: // SPAWNED only goes to HELD when someone picks the flag up
//			playFlagPickup(game.getFlagHolder());
//			break;
//		case HELD: // HELD only goes to DROPPED when the flag carrier drops the flag
//			playFlagDrop()
//		}
//	}
	
	private final String flagString = "" + ChatColor.GOLD + ChatColor.ITALIC + ChatColor.BOLD + "⚑" + ChatColor.RESET;
	
	private BukkitTask flagCounterTask;
	
	private final class FlagCounter extends BukkitRunnable {
		
		private RabbitPlayer flagHolder; 
		
		public FlagCounter(RabbitPlayer flagHolder) {
			this.flagHolder = flagHolder;
		}
		
		@Override
		public void run() {
			if (game.getFlagStatus() != FlagStatus.HELD || ! game.getFlagHolder().equals(flagHolder)) {
				this.cancel();
				return;
			}
			
			flagHolder.getPlayer().setPlayerListName(flagHolder.getColor().getChatColor() + flagHolder.getPlayer().getName() + " " + ChatColor.GRAY + flagHolder.getFlagCounter());
			
			bossbar.setMessage(flagString + " " + flagHolder.getColoredName() + flagHolder.getColor().getChatColor() + " has the flag!"
					+ ChatColor.RESET + " - " + ChatColor.GRAY + ChatColor.UNDERLINE + flagHolder.getFlagCounter() + ChatColor.RESET + " " + flagString);
			bossbar.setProgress(1 - (flagHolder.getFlagCounter() * 1.0 / game.getTimeToWin()));
		}
		
		@Override
		public void cancel() {
			bossbar.setProgress(1);
			super.cancel();
		}
	}
	
	
	
//	@Override
//	public void onGameStart() {
//		//playStart()
//	}
			
	@Override
	public void onGameWin(RabbitPlayer winner) {
		playPostgame(winner, RabbitGame.POSTGAME_DURATION_SECONDS);
		if (flagCounterTask != null) {
			flagCounterTask.cancel();
		}
	}
	
	@Override
	public void onFlagSpawn(Location flagSpawnLocation, boolean isRespawned) {
		bossbar.setMessage(flagString + ChatColor.YELLOW + " The flag has spawned! " + flagString);
	}
	
	@Override
	public void onFlagPickUp(RabbitPlayer flagHolder, boolean wasDropped) {
		bossbar.setMessage(flagString + " " + flagHolder.getColoredName(ChatColor.UNDERLINE) + flagHolder.getColor().getChatColor() + " picked up the flag! " + flagString);
		bossbar.flash(2);
		flagCounterTask = new FlagCounter(game.getFlagHolder()).runTaskTimer(Melee.getPlugin(), 30, game.getFlagTaskRate());
	}
	
	@Override
	public void onFlagDrop(RabbitPlayer flagDropper, Location flagDropLocation) {
		bossbar.setMessage(flagString + " " + flagDropper.getColoredName() + " has dropped the flag! " + flagString);
	}
	
//	public void playFlagPickup(RabbitPlayer carrier) {
//		
//	}
	
//	public void playFlagFirstSpawn() {
//		
//	}
//	
//	public void playFlagRespawn() {
//		
//	}
	
}
