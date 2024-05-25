package ai.akemi.bluemapskinsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

// SkinsRestorerX 14.x
import net.skinsrestorer.api.bukkit.events.SkinApplyBukkitEvent;

public class SkinsRestorerEventListeners implements Listener {
	private final BlueMapSkinSupport blueMapSkinSupport;

	public SkinsRestorerEventListeners(BlueMapSkinSupport sharedClassInstance) {
		blueMapSkinSupport = sharedClassInstance;
	}

	@EventHandler
	public void onSkinsRestorerSkinApplyBukkitEvent(SkinApplyBukkitEvent skinApplyBukkitEvent) {
		Player trueBukkitPlayerObject = skinApplyBukkitEvent.getWho();
		blueMapSkinSupport.getLogger().info(trueBukkitPlayerObject.getName() + " changed their skin using SkinsRestorer / SkinsRestorerX! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()…");
		blueMapSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
	}
}
