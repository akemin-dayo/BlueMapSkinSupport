package ai.akemi.bluemapofflineskinsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import ru.csm.bukkit.event.SkinChangedEvent;
import ru.csm.bukkit.event.SkinResetEvent;

public class CustomSkinsManagerEventListeners implements Listener {
	private final BlueMapOfflineSkinSupport blueMapOfflineSkinSupport;

	public CustomSkinsManagerEventListeners(BlueMapOfflineSkinSupport sharedClassInstance) {
		blueMapOfflineSkinSupport = sharedClassInstance;
	}

	@EventHandler
	public void onSkinChangedEvent(SkinChangedEvent skinChangedEvent) {
		Player trueBukkitPlayerObject = blueMapOfflineSkinSupport.getServer().getPlayer(skinChangedEvent.getPlayer().getUUID());
		blueMapOfflineSkinSupport.getLogger().info(trueBukkitPlayerObject.getName() + " changed their skin using CustomSkinsManager! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()...");
		blueMapOfflineSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
	}
	
	@EventHandler
	public void onSkinResetEvent(SkinResetEvent skinResetEvent) {
		Player trueBukkitPlayerObject = blueMapOfflineSkinSupport.getServer().getPlayer(skinResetEvent.getPlayer().getUUID());
		blueMapOfflineSkinSupport.getLogger().info(skinResetEvent.getPlayer().getName() + " reset their skin using CustomSkinsManager! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()...");
		blueMapOfflineSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
	}
}
