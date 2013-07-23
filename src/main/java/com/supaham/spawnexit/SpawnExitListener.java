package com.supaham.spawnexit;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpawnExitListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent evt) {

		if (!evt.getPlayer().hasPermission("spawnexit.add")) return;
		if (evt.getItem() == null || !SpawnExit.isItem(evt.getItem())) return;
		evt.setCancelled(true);
		Location loc = new Location(evt.getClickedBlock().getWorld(), evt.getClickedBlock().getX(), evt.getClickedBlock().getY() + 1, evt
				.getClickedBlock().getZ());
		if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (SpawnExit.removePath(loc))
				evt.getPlayer().sendMessage("§eRemoved Path");
			else evt.getPlayer().sendMessage("§cPath doesn't exist there");
		} else if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (SpawnExit.addPath(loc))
				evt.getPlayer().sendMessage("§eAdded Path");
			else evt.getPlayer().sendMessage("§cPath already exists there");
		}
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent evt) {

		if (SpawnExit.containsPlayer(evt.getPlayer())) SpawnExit.removePlayer(evt.getPlayer());
	}
}
