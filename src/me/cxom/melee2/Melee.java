package me.cxom.melee2;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Melee extends JavaPlugin {

	private static Plugin plugin;
	public static Plugin getPlugin(){ return plugin; }
	
	
	
	@Override
	public void onEnable(){
		plugin = this;
		//register events
	}
	
	@Override
	public void onDisable(){
		//force stop matches
		//restore players
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		return true;
	}
	
}
