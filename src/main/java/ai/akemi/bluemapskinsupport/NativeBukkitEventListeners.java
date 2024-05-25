package ai.akemi.bluemapskinsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NativeBukkitEventListeners implements Listener {
	private final BlueMapSkinSupport blueMapSkinSupport;

	public NativeBukkitEventListeners(BlueMapSkinSupport sharedClassInstance) {
		blueMapSkinSupport = sharedClassInstance;
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		blueMapSkinSupport.getLogger().info(event.getPlayer().getName() + " joined the server! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()…");
		blueMapSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(event.getPlayer());
	}
}
