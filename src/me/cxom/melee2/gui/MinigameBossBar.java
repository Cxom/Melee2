package me.cxom.melee2.gui;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;

public class MinigameBossBar {

	private BossBar bossbar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
	
	public MinigameBossBar(){}
	
	public MinigameBossBar(String title){
		bossbar.setTitle(title);
	}
	
	public void setColor(BarColor color) {
		bossbar.setColor(color);
	}
	
	public void setMessage(String title){
		bossbar.setTitle(title);
	}
	
	public void setProgress(double progress) {
		bossbar.setProgress(progress);
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
	
	public void reset() {
		bossbar.setTitle("");
		bossbar.setColor(BarColor.WHITE);
		bossbar.setStyle(BarStyle.SOLID);
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
