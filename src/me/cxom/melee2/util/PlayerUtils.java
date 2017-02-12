package me.cxom.melee2.util;

import org.bukkit.entity.Player;

public class PlayerUtils {

	public static void perfectStats(Player player){
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setExhaustion(0);
		player.setFireTicks(0);
	}
	
}
