package me.cxom.melee2.gui;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.player.MeleePlayer;
import net.md_5.bungee.api.ChatColor;

public class MeleeBossBar {

	private BossBar bossbar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
	
	public MeleeBossBar(){}
	
	public MeleeBossBar(String title){
		bossbar.setTitle(title);
	}
	
	public void setMessage(String title){
		bossbar.setTitle(title);
	}
	
	public void setLeader(MeleePlayer leader){
		String leaderName = leader.getColor().getChatColor() + "" + ChatColor.ITALIC + leader.getPlayer().getName();
		if (!bossbar.getTitle().startsWith(leaderName)){
			flash(3);
		}
		setMessage(leaderName + /*leader.getColor().getChatColor() +*/ " (" + leader.getKills() + ")" + ChatColor.RESET + " is in the lead!");
	}
	
	public void setTier(MeleePlayer tier){
		setMessage(tier.getColor().getChatColor() + "" + ChatColor.ITALIC + tier.getPlayer().getName() + " (" + tier.getKills() + ") "
						+ ChatColor.RESET + "ties "
						+ bossbar.getTitle().substring(0, bossbar.getTitle().indexOf(' '))
						+ ChatColor.RESET + " for the lead!");
	}
	
	public void flash(int cycles){
		new BukkitRunnable(){
			int i = cycles * 2;
			@Override
			public void run(){
				if (i == 0){
					this.cancel();
					return;
				}
				if (i % 2 == 0){
					bossbar.setColor(BarColor.GREEN);
				} else {
					bossbar.setColor(BarColor.WHITE);
				}
				i--;
			}
		}.runTaskTimer(Melee.getPlugin(), 0, 6);
	}
	
	public void addPlayer(Player player){
		bossbar.addPlayer(player);
	}
	
	public void removePlayer(Player player){
		bossbar.removePlayer(player);
	}
	
	public void removeAll(){
		bossbar.removeAll();
	}
	
}
