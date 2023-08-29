package net.punchtree.melee.player;

import org.bukkit.entity.Player;

import net.punchtree.util.color.PunchTreeColor;

public class RabbitPlayer extends MeleePlayer implements Comparable<RabbitPlayer> {

	private int flagCounter;
	
	public RabbitPlayer(Player player, PunchTreeColor color, int flagCounterStart) {
		super(player, color);
		
		this.flagCounter = flagCounterStart;
	}
	
	public int getFlagCounter() {
		return Math.max(0, flagCounter);
	}
	
	public boolean decrementFlagCounter() {
		--flagCounter;
		return flagCounter <= 0;
	}

	@Override
	public int compareTo(RabbitPlayer player) {
		return Integer.compare(player.getFlagCounter(), this.getFlagCounter());
	}

}
