package ai.akemi.bluemapskinsupport;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

// SkinsRestorerX 15.x
import net.skinsrestorer.api.event.SkinApplyEvent;

public class SkinsRestorerSkinApplyEventEventBusListener implements Consumer<SkinApplyEvent> {
	private final BlueMapSkinSupport blueMapSkinSupport;

	public SkinsRestorerSkinApplyEventEventBusListener(BlueMapSkinSupport sharedClassInstance) {
		blueMapSkinSupport = sharedClassInstance;
	}

	public void accept(SkinApplyEvent skinApplyEvent) {
        Player trueBukkitPlayerObject = skinApplyEvent.getPlayer(Player.class);
		blueMapSkinSupport.getLogger().info(trueBukkitPlayerObject.getName() + " changed their skin using SkinsRestorer / SkinsRestorerX! Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()…");
		blueMapSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
	}
}
