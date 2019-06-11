package me.cxom.melee2.gui;

import org.bukkit.ChatColor;

import me.cxom.melee2.common.model.AttackMethod;
import me.cxom.melee2.player.MeleePlayer;

public class Killfeed extends ScrollingScoreboard {

	public Killfeed(String title) {
		super(title);
	}
	
	public void sendKill(MeleePlayer killer, MeleePlayer killed, AttackMethod attackMethod) {
		String killerName = killer.getPlayer().getName();
		String killedName = killed.getPlayer().getName();
		
		String killfeedMessage = String.format("%s%s %s %s%s",
				killer.getColor().getChatColor(), killerName,
				ChatColor.WHITE + attackMethod.getIcon(),
				killed.getColor().getChatColor(), killedName);
		killfeedMessage = killfeedMessage.length() > MAX_SCOREBOARD_LENGTH ? killfeedMessage.substring(0, MAX_SCOREBOARD_LENGTH) : killfeedMessage;
		sendMessage(killfeedMessage);
	}

}
