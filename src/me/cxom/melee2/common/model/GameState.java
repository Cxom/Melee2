package me.cxom.melee2.common.model;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum GameState {

//	STARTING(ChatColor.YELLOW, new ItemStack(Material.YELLOW_CONCRETE)),
	RUNNING(ChatColor.LIGHT_PURPLE, new ItemStack(Material.PINK_CONCRETE)),
	ENDING(ChatColor.GOLD, new ItemStack(Material.ORANGE_CONCRETE)),
	WAITING(ChatColor.GREEN, new ItemStack(Material.LIME_CONCRETE)),
	STOPPED(ChatColor.RED, new ItemStack(Material.RED_CONCRETE));
	
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
