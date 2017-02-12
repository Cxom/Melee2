package me.cxom.melee2.player;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

@SuppressWarnings("serial")
public class MeleeColor extends Color {

	// Bukkit Colors:

		public static final MeleeColor WHITE = new MeleeColor(255, 255, 255, ChatColor.WHITE);
		public static final MeleeColor YELLOW = new MeleeColor(255, 255, 85, ChatColor.YELLOW);
		public static final MeleeColor LIGHT_PURPLE = new MeleeColor(255, 85, 255, ChatColor.LIGHT_PURPLE);
		public static final MeleeColor RED = new MeleeColor(255, 85, 85, ChatColor.RED);
		public static final MeleeColor AQUA = new MeleeColor(85, 255, 255, ChatColor.AQUA);
		public static final MeleeColor GREEN = new MeleeColor(85, 255, 85, ChatColor.GREEN);
		public static final MeleeColor BLUE = new MeleeColor(85, 85, 255, ChatColor.BLUE);
		public static final MeleeColor DARK_GRAY = new MeleeColor(85, 85, 85, ChatColor.DARK_GRAY);
		public static final MeleeColor GRAY = new MeleeColor(170, 170, 170, ChatColor.GRAY);
		public static final MeleeColor GOLD = new MeleeColor(255, 170, 0, ChatColor.GOLD);
		public static final MeleeColor DARK_PURPLE = new MeleeColor(170, 0, 170, ChatColor.DARK_PURPLE);
		public static final MeleeColor DARK_RED = new MeleeColor(170, 0, 0, ChatColor.DARK_RED);
		public static final MeleeColor DARK_AQUA = new MeleeColor(0, 170, 170, ChatColor.DARK_AQUA);
		public static final MeleeColor DARK_GREEN = new MeleeColor(0, 170, 0, ChatColor.DARK_GREEN);
		public static final MeleeColor DARK_BLUE = new MeleeColor(0, 0, 170, ChatColor.DARK_BLUE);
		public static final MeleeColor BLACK = new MeleeColor(0, 0, 0, ChatColor.BLACK);

		private static final Map<String, MeleeColor> defaults;
		static{
			defaults = new HashMap<String, MeleeColor>();
			defaults.put("WHITE", MeleeColor.WHITE);
			defaults.put("YELLOW", MeleeColor.YELLOW);
			defaults.put("LIGHT_PURPLE", MeleeColor.LIGHT_PURPLE);
			defaults.put("RED", MeleeColor.RED);
			defaults.put("AQUA", MeleeColor.AQUA);
			defaults.put("GREEN", MeleeColor.GREEN);
			defaults.put("BLUE", MeleeColor.BLUE);
			defaults.put("DARK_GRAY", MeleeColor.DARK_GRAY);
			defaults.put("GRAY", MeleeColor.GRAY);
			defaults.put("GOLD", MeleeColor.GOLD);
			defaults.put("DARK_PURPLE", MeleeColor.DARK_PURPLE);
			defaults.put("DARK_RED", MeleeColor.DARK_RED);
			defaults.put("DARK_AQUA", MeleeColor.DARK_AQUA);
			defaults.put("DARK_GREEN", MeleeColor.DARK_GREEN);
			defaults.put("DARK_BLUE", MeleeColor.DARK_BLUE);
			defaults.put("BLACK", MeleeColor.BLACK);
		}
		
		/* 
		 * Wool & Clay Colors
		 * RED(), ORANGE(), YELLOW(), GREEN(), BLUE(), PURPLE(), 
		 * LIME(), MAGENTA(), LIGHTBLUE(),
		 * PINK(), CYAN(), BROWN(),
		 * WHITE(), LIGHTGRAY(), GRAY(), BLACK();
		 */
		
		public static Collection<MeleeColor> getDefaults(){
			return defaults.values();
		}
		
		public static ChatColor getNearestChatColor(int red, int green, int blue) {
			double distance = 500;
			ChatColor closest = ChatColor.WHITE;
			for (MeleeColor c : defaults.values()) {
				double newDistance = 
						Math.sqrt(Math.pow((double) (red - c.getRed()), 2)
								+ Math.pow((double) (green - c.getGreen()), 2)
								+ Math.pow((double) (blue - c.getBlue()), 2));
				if (newDistance < distance) {
					distance = newDistance;
					closest = c.getChatColor();
				}
			}
			return closest;
		}
		
		public static MeleeColor valueOf(String colorName){
			for(String color : defaults.keySet()){
				if(colorName.equalsIgnoreCase(color)
				|| colorName.equalsIgnoreCase(color.replaceAll("_", ""))){
					return defaults.get(color);
				}
			}
			System.out.println("No color found: " + colorName);
			return MeleeColor.WHITE;
		}
	
		//------------------------------------------------------------------//

		private ChatColor chatColor;
		
		public MeleeColor(int red, int green, int blue){
			this(red, green, blue, getNearestChatColor(red, green, blue));
		}
		
		public MeleeColor(int red, int green, int blue, ChatColor chatColor){
			super(red, green, blue);
			this.chatColor = chatColor;
		}
		
		public ChatColor getChatColor(){
			return chatColor;
		}
		
		public void setChatColor(ChatColor chatColor){
			this.chatColor = chatColor;
		}
		
		public org.bukkit.Color getBukkitColor(){
			return org.bukkit.Color.fromRGB(this.getRed(), this.getGreen(), this.getBlue());
		}
	
}

