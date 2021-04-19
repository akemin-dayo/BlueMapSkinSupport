package ai.akemi.bluemapofflineskinsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NativeBukkitEventListeners implements Listener {
	private final BlueMapOfflineSkinSupport blueMapOfflineSkinSupport;

	public NativeBukkitEventListeners(BlueMapOfflineSkinSupport sharedClassInstance) {
		blueMapOfflineSkinSupport = sharedClassInstance;
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		blueMapOfflineSkinSupport.getLogger().info(event.getPlayer().getName() + " joined the server! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()...");
		blueMapOfflineSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(event.getPlayer());
	}
}
