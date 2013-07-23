package com.supaham.spawnexit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnExit extends JavaPlugin {

	public static ItemStack item;

	public static List<String> players = new ArrayList<String>();
	public static LinkedList<Location> pathLocs = new LinkedList<Location>();

	public static SpawnExit plugin;

	@Override
	public void onEnable() {

		plugin = this;
		loadConfig();
		initItemStack();
		getServer().getPluginManager().registerEvents(new SpawnExitListener(), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			public void run() {

				if (!players.isEmpty()) {
					for (String name : players) {
						Player player = Bukkit.getPlayer(name);
						if (player != null) {
							for (Location loc : pathLocs) {
								if (loc.getWorld() == player.getWorld()) player.playEffect(loc, Effect.SMOKE, 4);
							}
						}
					}
				}
			}
		}, 40L, 20L);
	}

	public void onDisable() {

		saveConfiguration();
	};

	public void loadConfig() {

		if (!getDataFolder().exists()) getDataFolder().mkdirs();
		File file = new File(this.getDataFolder(), "config.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.reloadConfig();
		if (!pathLocs.isEmpty()) pathLocs.clear();
		if (getConfig().isSet("paths")) {
			for (String string : getConfig().getStringList("paths")) {
				String[] split = string.split(" ");
				Location loc = new Location(getServer().getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
						Double.parseDouble(split[3]));
				addPath(loc);
			}
		}
	}

	public void saveConfiguration() {

		List<String> list = new ArrayList<String>();
		for (Location loc : pathLocs) {
			list.add(loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
		}
		getConfig().set("paths", list);
		saveConfig();
	}

	public void initItemStack() {

		item = new ItemStack(Material.GHAST_TEAR);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName("§ePath Selector");
		List<String> lore = new ArrayList<String>();
		lore.add("Left Click to remove a path");
		lore.add("Right Click to add a path");
		im.setLore(lore);
		item.setItemMeta(im);
	}

	public static boolean isItem(ItemStack is) {

		if (is.getTypeId() == item.getTypeId())
			if ((is.getItemMeta() != null && item.getItemMeta() != null) && is.getItemMeta().getDisplayName() == item.getItemMeta().getDisplayName())
				return true;
		return false;
	}

	public static boolean containsPlayer(Player player) {

		return players.contains(player.getName().toLowerCase());
	}

	public static void addPlayer(final Player player) {

		if (!containsPlayer(player)) {
			players.add(player.getName().toLowerCase());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				public void run() {

					removePlayer(player);
				}
			}, 1200L);
		}
	}

	public static void removePlayer(Player player) {

		if (containsPlayer(player)) players.remove(player.getName().toLowerCase());
	}

	public static boolean containsPath(Location loc) {

		for (Location loc2 : pathLocs) {
			if ((loc.getBlockX() == loc2.getBlockX()) && (loc.getBlockY() == loc2.getBlockY()) && (loc.getBlockZ() == loc2.getBlockZ())) return true;
		}
		return false;
	}

	public static boolean addPath(Location loc) {

		if (containsPath(loc))
			return false;
		else {
			pathLocs.add(loc);
			return true;
		}
	}

	public static boolean removePath(Location loc) {

		if (!containsPath(loc))
			return false;
		else {
			pathLocs.remove(loc);
			return true;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("This is an in-game command!");
			return true;
		}
		Player player = (Player) sender;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage("§c|§3-------- §eSpawnExit Help list §3--------§c|");
				if (player.hasPermission("spawnexit.use")) sender.sendMessage("§e/spawnexit - shows you a trail of spawn");
				if (player.hasPermission("spawnexit.admin")) sender.sendMessage("§e/spawnexit item - Gives you an item to add paths");
				if (player.hasPermission("spawnexit.admin")) sender.sendMessage("§e/spawnexit reload - Reloads the configuration");
			} else if (args[0].equalsIgnoreCase("item")) {
				if (!player.hasPermission("spawnexit.admin")) {
					player.sendMessage("§cNo permission");
					return true;
				}
				player.getInventory().addItem(item);
				player.updateInventory();
			} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				if (!player.hasPermission("spawnexit.admin")) {
					player.sendMessage("§cNo permission");
					return true;
				}
				player.sendMessage("§eConfiguration reloaded.");
				loadConfig();
			}
		} else {
			if (!player.hasPermission("spawnexit.use")) {
				player.sendMessage("§cNo permission");
				return true;
			}
			if (pathLocs.isEmpty()) {
				player.sendMessage("§cThe path has not been set yet");
				return true;
			} else {
				for (Location loc : pathLocs) {
					if (loc.getWorld() == player.getWorld()) {
						player.sendMessage("§cThis command may only be used in the main world.");
						break;
					}
				}
			}
			addPlayer(player);
			sender.sendMessage("§eFollow the path from spawn.");
		}
		return true;
	}
}
