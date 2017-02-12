package me.cxom.melee2.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.cxom.melee2.Melee;
import me.cxom.melee2.player.PlayerProfile;

public class CommandEvents implements Listener{

	List<String> cmds = new ArrayList<String>(Arrays.asList(new String[] {
			"/m", "/msg", "/message", "/t", "/tell", "/w", "/whisper", "/r",
			"/reply", "/ac", "/helpop"}));

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		String command = e.getMessage().toLowerCase() + " ";
		if (PlayerProfile.isSaved(player) && !player.isOp()){
			if (! (cmds.contains(command.split(" ")[0]) || (command.startsWith("melee leave")))) {
				e.setCancelled(true);
				player.sendMessage(Melee.CHAT_PREFIX + ChatColor.RED + "You do not have permission to use non-messaging commands in Melee. If you wish to leave the match, do /melee leave");
			}
		}
		
	}
	
}
