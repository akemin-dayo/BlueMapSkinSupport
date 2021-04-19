package ai.akemi.bluemapofflineskinsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;

public class SkinsRestorerEventListeners implements Listener {
	private final BlueMapOfflineSkinSupport blueMapOfflineSkinSupport;
	
	public SkinsRestorerEventListeners(BlueMapOfflineSkinSupport sharedClassInstance) {
		blueMapOfflineSkinSupport = sharedClassInstance;
	}

	@EventHandler
	public void onSkinsRestorerSkinsMenuInventoryCloseEvent(InventoryCloseEvent inventoryCloseEvent) {
		// TODO: This is a pretty terrible hack for many, MANY reasons, but unfortunately SkinsRestorer does not appear to broadcast event notifications for skin change/reset, unlike CustomSkinsManager.
		// Current deficiencies with this implementation include:
		// ① It only works if the player uses the /skins UI menu. Commands such as /skin skinName or /skin clear will not trigger an update.
		// ② It only works if SkinsRestorer's UI localisation is set to English, as my code only checks for the unlocalised string "Skins Menu"… and nothing else.
		// I really should consider joining their Discord or something and asking them if they'd be willing to implement some event notifications… it would make this a LOT better.
		if (inventoryCloseEvent.getView().getTitle().contains("Skins Menu")) {
			Player trueBukkitPlayerObject = null;
			if (inventoryCloseEvent.getPlayer() instanceof Player) {
				trueBukkitPlayerObject = (Player)inventoryCloseEvent.getPlayer();
			} else {
				blueMapOfflineSkinSupport.getLogger().severe("Could not determine the true Bukkit Player object for " + inventoryCloseEvent.getPlayer().getName() + "!");
				return;
			}

			blueMapOfflineSkinSupport.getLogger().info(trueBukkitPlayerObject.getName() + " opened and closed the SkinsRestorer menu! This action may have resulted in a skin change. Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()...");
			blueMapOfflineSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
		}
	}
}
