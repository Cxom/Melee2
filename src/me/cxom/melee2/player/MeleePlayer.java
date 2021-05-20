package me.cxom.melee2.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.punchtree.minigames.utility.color.ColoredPlayer;
import net.punchtree.minigames.utility.player.InventoryUtils;
import net.punchtree.util.color.PunchTreeColor;

public class MeleePlayer implements ColoredPlayer {

	private final UUID uuid;
	
	private final PunchTreeColor color;
	
	private int kills = 0;
	
	public MeleePlayer(Player player, PunchTreeColor color){
		this.uuid = player.getUniqueId();
		this.color = color;
		
		player.getInventory().clear();
		InventoryUtils.equipPlayer(player, color);
	}
	
	public UUID getUniqueId(){
		return uuid;
	}
	
	public Player getPlayer(){
		return Bukkit.getPlayer(uuid);
	}
	
	public PunchTreeColor getColor(){
		return color;
	}
	
	public String getColoredName() {
		return getColor().getChatColor() + getPlayer().getName() + ChatColor.RESET;
	}
	
	public String getColoredName(ChatColor format) {
		return getColor().getChatColor() + "" + format + getPlayer().getName() + ChatColor.RESET;
	}
	
	public int getKills(){
		return kills;
	}
	
	public void incrementKills(){
		kills++;
	}
	
	@Override
	public boolean equals(Object o){
		if (! (o instanceof MeleePlayer)) return false;
		return uuid.equals(((MeleePlayer) o).getUniqueId());
	}
	
}
