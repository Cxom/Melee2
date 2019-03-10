package me.cxom.melee2.util;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.cxom.melee2.Melee;
import me.cxom.melee2.player.MinigameColor;

public class FireworkUtils {

	public static Firework spawnFirework(Location loc, MinigameColor color, int power){ return spawnFirework(loc, color.getBukkitColor(), power); }
	public static Firework spawnFirework(Location loc, Color color, int power){
		return spawnFirework(loc, color, Color.fromRGB(Math.min(color.getRed() + 50,  255), Math.min(color.getGreen() + 50, 255), Math.min(color.getBlue() + 50, 255)), power);
	}
	
	public static Firework spawnFirework(Location loc, MinigameColor primary, MinigameColor secondary, int power){ return spawnFirework(loc, primary.getBukkitColor(), secondary.getBukkitColor(), power); }
	public static Firework spawnFirework(Location loc, Color primary, Color secondary, int power){
		Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
//		Random r = new Random();
//		FireworkEffect effect = FireworkEffect.builder()
//									.with(Type.BALL_LARGE)
//									.flicker(r.nextBoolean())
//									.withColor(primary)
//									.withFade(secondary)
//									.trail(r.nextBoolean())
//									.build();
//		FireworkEffect effect2 = FireworkEffect.builder()
//									.with(Type.STAR)
//									.flicker(r.nextBoolean())
//									.withColor(secondary)
//									.build();
//		fwm.addEffects(effect, effect2);
		fwm.setPower(power);
		fw.setFireworkMeta(fwm);
		return fw;
	}
	
	public static void detontateInstantly(Firework fw){
//		new BukkitRunnable(){
//			@Override
//			public void run(){
//				fw.detonate();
//			}
//		}.runTaskLater(Melee.getPlugin(), 1);
	}
	
}
