package me.cxom.melee2.util;



import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.cxom.melee2.player.MeleeColor;

public class InventoryUtils {

	public static void equipPlayer(Player player, MeleeColor color) {
		PlayerInventory pi = player.getInventory();
		pi.setItem(0, makeUnbreakable(new ItemStack(Material.STONE_SWORD)));
		ItemStack b = makeUnbreakable(new ItemStack(Material.BOW));
		ItemMeta m = b.getItemMeta();
		m.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
		b.setItemMeta(m);
		pi.setItem(1, b);
		pi.setItem(9, new ItemStack(Material.ARROW));
		pi.setArmorContents(getArmorSet(color));
	}
	
	public static ItemStack[] getArmorSet(MeleeColor color){
		Color armorColor = color.getBukkitColor();

		ItemStack helmetItem = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmetItem.getItemMeta();
		helmetMeta.setColor(armorColor);
		helmetMeta.setUnbreakable(true);
		helmetItem.setItemMeta(helmetMeta);

		ItemStack chestplateItem = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta chestPlateMeta = (LeatherArmorMeta) chestplateItem.getItemMeta();
		chestPlateMeta.setColor(armorColor);
		chestPlateMeta.setUnbreakable(true);
		chestplateItem.setItemMeta(chestPlateMeta);

		ItemStack leggingsItem = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggingsItem.getItemMeta();
		leggingsMeta.setColor(armorColor);
		leggingsMeta.setUnbreakable(true);
		leggingsItem.setItemMeta(leggingsMeta);

		ItemStack bootsItem = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta bootsMeta = (LeatherArmorMeta) bootsItem.getItemMeta();
		bootsMeta.setColor(armorColor);
		bootsMeta.setUnbreakable(true);
		bootsItem.setItemMeta(bootsMeta);

		return new ItemStack[] { bootsItem, leggingsItem, chestplateItem, helmetItem };
	}
	
	public static ItemStack makeUnbreakable(ItemStack i){
		ItemMeta m = i.getItemMeta();
		m.setUnbreakable(true);
		i.setItemMeta(m);
		return i;
	}
	
}
