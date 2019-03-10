package me.cxom.melee2.player;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

@SuppressWarnings("serial")
public class MinigameColor extends Color {

	// Bukkit Colors:

		public static final MinigameColor WHITE = new MinigameColor(255, 255, 255, ChatColor.WHITE);
		public static final MinigameColor YELLOW = new MinigameColor(255, 255, 85, ChatColor.YELLOW);
		public static final MinigameColor LIGHT_PURPLE = new MinigameColor(255, 85, 255, ChatColor.LIGHT_PURPLE);
		public static final MinigameColor RED = new MinigameColor(255, 85, 85, ChatColor.RED);
		public static final MinigameColor AQUA = new MinigameColor(85, 255, 255, ChatColor.AQUA);
		public static final MinigameColor GREEN = new MinigameColor(85, 255, 85, ChatColor.GREEN);
		public static final MinigameColor BLUE = new MinigameColor(85, 85, 255, ChatColor.BLUE);
		public static final MinigameColor DARK_GRAY = new MinigameColor(85, 85, 85, ChatColor.DARK_GRAY);
		public static final MinigameColor GRAY = new MinigameColor(170, 170, 170, ChatColor.GRAY);
		public static final MinigameColor GOLD = new MinigameColor(255, 170, 0, ChatColor.GOLD);
		public static final MinigameColor DARK_PURPLE = new MinigameColor(170, 0, 170, ChatColor.DARK_PURPLE);
		public static final MinigameColor DARK_RED = new MinigameColor(170, 0, 0, ChatColor.DARK_RED);
		public static final MinigameColor DARK_AQUA = new MinigameColor(0, 170, 170, ChatColor.DARK_AQUA);
		public static final MinigameColor DARK_GREEN = new MinigameColor(0, 170, 0, ChatColor.DARK_GREEN);
		public static final MinigameColor DARK_BLUE = new MinigameColor(0, 0, 170, ChatColor.DARK_BLUE);
		public static final MinigameColor BLACK = new MinigameColor(0, 0, 0, ChatColor.BLACK);

		private static final Map<String, MinigameColor> defaults;
		static{
			defaults = new HashMap<String, MinigameColor>();
			defaults.put("WHITE", MinigameColor.WHITE);
			defaults.put("YELLOW", MinigameColor.YELLOW);
			defaults.put("LIGHT_PURPLE", MinigameColor.LIGHT_PURPLE);
			defaults.put("RED", MinigameColor.RED);
			defaults.put("AQUA", MinigameColor.AQUA);
			defaults.put("GREEN", MinigameColor.GREEN);
			defaults.put("BLUE", MinigameColor.BLUE);
			defaults.put("DARK_GRAY", MinigameColor.DARK_GRAY);
			defaults.put("GRAY", MinigameColor.GRAY);
			defaults.put("GOLD", MinigameColor.GOLD);
			defaults.put("DARK_PURPLE", MinigameColor.DARK_PURPLE);
			defaults.put("DARK_RED", MinigameColor.DARK_RED);
			defaults.put("DARK_AQUA", MinigameColor.DARK_AQUA);
			defaults.put("DARK_GREEN", MinigameColor.DARK_GREEN);
			defaults.put("DARK_BLUE", MinigameColor.DARK_BLUE);
			defaults.put("BLACK", MinigameColor.BLACK);
		}
		
		//old wool colors
//		public static final MinigameColor WOOL_WHITE = new MinigameColor(228,228,228);
//		public static final MinigameColor WOOL_ORANGE = new MinigameColor(234,126,53);
//		public static final MinigameColor WOOL_MAGENTA = new MinigameColor();
//		public static final MinigameColor WOOL_LIGHT_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_YELLOW = new MinigameColor(194,181,28);
//		public static final MinigameColor WOOL_LIME = new MinigameColor(57,186,46);
//		public static final MinigameColor WOOL_PINK = new MinigameColor();
//		public static final MinigameColor WOOL_DARK_GRAY = new MinigameColor(65,65,65);
//		public static final MinigameColor WOOL_LIGHT_GRAY = new MinigameColor(160,167,167);
//		public static final MinigameColor WOOL_CYAN = new MinigameColor();
//		public static final MinigameColor WOOL_PURPLE = new MinigameColor();
//		public static final MinigameColor WOOL_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_BROWN = new MinigameColor();
//		public static final MinigameColor WOOL_GREEN = new MinigameColor();
//		public static final MinigameColor WOOL_RED = new MinigameColor(158,43,39);
//		public static final MinigameColor WOOL_BLACK = new MinigameColor(24,20,20);
		
		// still old wool colors - better than wolves
//		public static final MinigameColor WOOL_WHITE = new MinigameColor(228,228,228);
//		public static final MinigameColor WOOL_ORANGE = new MinigameColor(234,126,53);
//		public static final MinigameColor WOOL_MAGENTA = new MinigameColor();
//		public static final MinigameColor WOOL_LIGHT_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_YELLOW = new MinigameColor(194,181,28);
//		public static final MinigameColor WOOL_LIME = new MinigameColor(57,186,46);
//		public static final MinigameColor WOOL_PINK = new MinigameColor();
//		public static final MinigameColor WOOL_DARK_GRAY = new MinigameColor(65,65,65);
//		public static final MinigameColor WOOL_LIGHT_GRAY = new MinigameColor(160,167,167);
//		public static final MinigameColor WOOL_CYAN = new MinigameColor();
//		public static final MinigameColor WOOL_PURPLE = new MinigameColor();
//		public static final MinigameColor WOOL_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_BROWN = new MinigameColor();
//		public static final MinigameColor WOOL_GREEN = new MinigameColor();
//		public static final MinigameColor WOOL_RED = new MinigameColor(158,43,39);
//		public static final MinigameColor WOOL_BLACK = new MinigameColor(24,20,20);
		
		public static final MinigameColor CONCRETE_WHITE = new MinigameColor(207,213,214);
		public static final MinigameColor CONCRETE_ORANGE = new MinigameColor(224,97,1);
		public static final MinigameColor CONCRETE_MAGENTA = new MinigameColor(169,48,159);
		public static final MinigameColor CONCRETE_LIGHT_BLUE = new MinigameColor(36,136,199);
		public static final MinigameColor CONCRETE_YELLOW = new MinigameColor(241,175,21);
		public static final MinigameColor CONCRETE_LIME = new MinigameColor(94,169,25);
		public static final MinigameColor CONCRETE_PINK = new MinigameColor(214,101,143);
		public static final MinigameColor CONCRETE_DARK_GRAY = new MinigameColor(55,58,62);
		public static final MinigameColor CONCRETE_LIGHT_GRAY = new MinigameColor(125,125,115);
		public static final MinigameColor CONCRETE_CYAN = new MinigameColor(21,119,136);
		public static final MinigameColor CONCRETE_PURPLE = new MinigameColor(100,32,156);
		public static final MinigameColor CONCRETE_BLUE = new MinigameColor(45,47,143);
		public static final MinigameColor CONCRETE_BROWN = new MinigameColor(96,60,32);
		public static final MinigameColor CONCRETE_GREEN = new MinigameColor(73,91,36);
		public static final MinigameColor CONCRETE_RED = new MinigameColor(142,33,33);
		public static final MinigameColor CONCRETE_BLACK = new MinigameColor(8,10,15);
		
		private static final Map<String, MinigameColor> concretes;
		static{
			concretes = new HashMap<String, MinigameColor>();
			concretes.put("CONCRETE_WHITE", MinigameColor.CONCRETE_WHITE);
			concretes.put("CONCRETE_ORANGE", MinigameColor.CONCRETE_ORANGE);
			concretes.put("CONCRETE_MAGENTA", MinigameColor.CONCRETE_MAGENTA);
			concretes.put("CONCRETE_LIGHT_BLUE", MinigameColor.CONCRETE_LIGHT_BLUE);
			concretes.put("CONCRETE_YELLOW", MinigameColor.CONCRETE_YELLOW);
			concretes.put("CONCRETE_LIME", MinigameColor.CONCRETE_LIME);
			concretes.put("CONCRETE_PINK", MinigameColor.CONCRETE_PINK);
			concretes.put("CONCRETE_DARK_GRAY", MinigameColor.CONCRETE_DARK_GRAY);
			concretes.put("CONCRETE_LIGHT_GRAY", MinigameColor.CONCRETE_LIGHT_GRAY);
			concretes.put("CONCRETE_CYAN", MinigameColor.CONCRETE_CYAN);
			concretes.put("CONCRETE_PURPLE", MinigameColor.CONCRETE_PURPLE);
			concretes.put("CONCRETE_BLUE", MinigameColor.CONCRETE_BLUE);
			concretes.put("CONCRETE_BROWN", MinigameColor.CONCRETE_BROWN);
			concretes.put("CONCRETE_GREEN", MinigameColor.CONCRETE_GREEN);
			concretes.put("CONCRETE_RED", MinigameColor.CONCRETE_RED);
			concretes.put("CONCRETE_BLACK", MinigameColor.CONCRETE_BLACK);
		}
		
		/* 
		 * Wool & Clay Colors
		 * RED(), ORANGE(), YELLOW(), GREEN(), BLUE(), PURPLE(), 
		 * LIME(), MAGENTA(), LIGHTBLUE(),
		 * PINK(), CYAN(), BROWN(),
		 * WHITE(), LIGHTGRAY(), GRAY(), BLACK();
		 */
		
		public static Collection<MinigameColor> getDefaults(){
			return defaults.values();
		}
		
		public static Collection<MinigameColor> getConcretes(){
			return concretes.values();
		}
		
		public static ChatColor getNearestChatColor(int red, int green, int blue) {
			double distance = 500;
			ChatColor closest = ChatColor.WHITE;
			for (MinigameColor c : defaults.values()) {
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
		
		public static MinigameColor valueOf(String colorName){
			for(String color : defaults.keySet()){
				if(colorName.equalsIgnoreCase(color)
				|| colorName.equalsIgnoreCase(color.replaceAll("_", ""))){
					return defaults.get(color);
				}
			}
			System.out.println("No color found: " + colorName);
			return MinigameColor.WHITE;
		}
	
		//------------------------------------------------------------------//

		private ChatColor chatColor;
		
		public MinigameColor(int red, int green, int blue){
			this(red, green, blue, getNearestChatColor(red, green, blue));
		}
		
		public MinigameColor(int red, int green, int blue, ChatColor chatColor){
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

