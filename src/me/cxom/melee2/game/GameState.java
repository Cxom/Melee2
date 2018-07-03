package me.cxom.melee2.game;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum GameState {

	STARTING(ChatColor.YELLOW, new ItemStack(Material.CONCRETE, 1 , (short) 4)),
	RUNNING(ChatColor.LIGHT_PURPLE, new ItemStack(Material.CONCRETE, 1 , (short) 6)),
	ENDING(ChatColor.GOLD, new ItemStack(Material.CONCRETE, 1 , (short) 1)),
	WAITING(ChatColor.GREEN, new ItemStack(Material.CONCRETE, 1 , (short) 5)),
	STOPPED(ChatColor.RED, new ItemStack(Material.CONCRETE, 1 , (short) 14));
	
	private final ChatColor chatColor;
	private final ItemStack menuItem;
	
	private GameState(ChatColor chatColor, ItemStack menuItem){
		this.chatColor = chatColor;
		this.menuItem = menuItem;
	}
	
	public ChatColor getChatColor(){
		return chatColor;
	}
	
	public ItemStack getMenuItem(){
		return menuItem.clone();
	}
	
}
