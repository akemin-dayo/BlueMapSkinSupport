package ai.akemi.bluemapskinsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import ru.csm.bukkit.event.SkinChangedEvent;
import ru.csm.bukkit.event.SkinResetEvent;

public class CustomSkinsManagerEventListeners implements Listener {
	private final BlueMapSkinSupport blueMapSkinSupport;

	public CustomSkinsManagerEventListeners(BlueMapSkinSupport sharedClassInstance) {
		blueMapSkinSupport = sharedClassInstance;
	}

	@EventHandler
	public void onSkinChangedEvent(SkinChangedEvent skinChangedEvent) {
		Player trueBukkitPlayerObject = blueMapSkinSupport.getServer().getPlayer(skinChangedEvent.getPlayer().getUUID());
		blueMapSkinSupport.getLogger().info(trueBukkitPlayerObject.getName() + " changed their skin using CustomSkinsManager! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()...");
		blueMapSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
	}
	
	@EventHandler
	public void onSkinResetEvent(SkinResetEvent skinResetEvent) {
		Player trueBukkitPlayerObject = blueMapSkinSupport.getServer().getPlayer(skinResetEvent.getPlayer().getUUID());
		blueMapSkinSupport.getLogger().info(skinResetEvent.getPlayer().getName() + " reset their skin using CustomSkinsManager! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()...");
		blueMapSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
	}
}
