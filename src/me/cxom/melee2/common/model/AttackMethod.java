package me.cxom.melee2.common.model;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public enum AttackMethod{
	
	SLAY("⚔"),
	SHOOT("➵"),
	PUNCH("ლ");//ᕗ
	//ツ 
	
	private final String icon;
	
	private AttackMethod(String icon){
		this.icon = icon;
	}
	
	public String getIcon(){
		return icon;
	}
	
	public static AttackMethod getAttackMethod(Entity damager){
		if (damager instanceof Arrow)
			return AttackMethod.SHOOT;
		else if (((Player) damager).getInventory().getItemInMainHand().getType() == Material.STONE_SWORD)
			return AttackMethod.SLAY;
		else
			return AttackMethod.PUNCH;
	}
	
}
