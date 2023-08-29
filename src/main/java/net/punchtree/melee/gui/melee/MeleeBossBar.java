package net.punchtree.melee.gui.melee;

import net.punchtree.melee.gui.MinigameBossBar;
import net.punchtree.melee.player.MeleePlayer;
import net.md_5.bungee.api.ChatColor;

public class MeleeBossBar extends MinigameBossBar {
	
	private String leaderName = "";
	
	public void setLeader(MeleePlayer leader){
		String newLeaderName = leader.getColor().getChatColor() + "" + ChatColor.ITALIC + leader.getPlayer().getName();
		if (!newLeaderName.equals(leaderName)){
			leaderName = newLeaderName;
			flash(3);
		}
		setMessage(newLeaderName + /*leader.getColor().getChatColor() +*/ " (" + leader.getKills() + ")" + ChatColor.RESET + " is in the lead!");
	}
	
	public void setTier(MeleePlayer tier){
		setMessage(tier.getColor().getChatColor() + "" + ChatColor.ITALIC + tier.getPlayer().getName() + " (" + tier.getKills() + ") "
						+ ChatColor.RESET + "ties "
						+ leaderName
						+ ChatColor.RESET + " for the lead!");
	}
	
}
