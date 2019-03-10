package me.cxom.melee2.util;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.cxom.melee2.Melee;
import me.cxom.melee2.player.MinigameColor;

public class InventoryUtils {

	private static File inventoryFile = new File(Melee.getPlugin().getDataFolder().getAbsolutePath()
			+ File.separator + "inventories.yml");
	
	private static FileConfiguration inventoryBackups;
	static {
		inventoryBackups = new YamlConfiguration();
		try {
			inventoryBackups.load(inventoryFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void restoreBackupInventory(UUID uuid, Player player){
		if (inventoryBackups.isConfigurationSection(uuid.toString())){
			List<Map<String, Object>> serializedContents = (List<Map<String, Object>>) inventoryBackups.get(uuid.toString() + ".inventory");
			ItemStack[] contents = new ItemStack[36];
			for (int i = 0; i < serializedContents.size(); i++)
				contents[i] = ItemStack.deserialize(serializedContents.get(i));
			player.getInventory().setContents(contents);
		}
	}
	
	public static void backupInventory(Player player){
		List<Map<String, Object>> serializedContents = new ArrayList<>();
		for (ItemStack is : player.getInventory().getContents())
			if (is == null)
				continue;
			else
				serializedContents.add(is.serialize());
		inventoryBackups.set(player.getUniqueId() + ".inventory", serializedContents);
		try {
			inventoryBackups.save(inventoryFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void equipPlayer(Player player, MinigameColor color) {
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
	
	public static ItemStack[] getArmorSet(MinigameColor color){
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
