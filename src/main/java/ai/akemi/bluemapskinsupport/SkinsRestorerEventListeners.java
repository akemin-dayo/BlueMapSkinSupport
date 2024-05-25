package ai.akemi.bluemapskinsupport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;

public class SkinsRestorerEventListeners implements Listener {
	private final BlueMapSkinSupport blueMapSkinSupport;

	public SkinsRestorerEventListeners(BlueMapSkinSupport sharedClassInstance) {
		blueMapSkinSupport = sharedClassInstance;
	}

	@EventHandler
	public void onSkinsRestorerSkinsMenuInventoryCloseEvent(InventoryCloseEvent inventoryCloseEvent) {
		// TODO: This is a pretty terrible hack for many, MANY reasons, but unfortunately SkinsRestorer / SkinsRestorerX does not appear to broadcast event notifications for skin change/reset.

		// Current deficiencies with this implementation include:
		// ① It only works if the player uses the /skins UI menu. Commands such as /skin skinName or /skin clear will not trigger an update.
		// ② It will break if any of the localised inventory menu titles below change, or if new localisations are added that are not in the below list.

		// I've joined their Discord and asked about implementing skin change event notifications — hopefully something comes out of that, as it would make this disaster of an implementation a LOT better.

		String[] localisedSkinsRestorerXInventoryMenuTitles = {
			"Skins Menu",
			"スキンメニュー",
			"스킨 메뉴",
			"皮肤菜单",
			"Menu de Skins",
			"Menu des Skins",
			"Izbornik Skinova",
			"Kinézetek Menü",
			"Menu Skin",
			"Išvaizdų Meniu",
			"Menu Skinów",
			"Меню скинов",
			"Ponuka skinov",
			"Skin menu",
			"Skin Menüsü",
			"Кыяфәт менюсы",
			"Меню скінів"
		};

		String inventoryWindowTitle = inventoryCloseEvent.getView().getTitle();
		boolean wasSkinsRestorerXMenuClosed = false;

		for (String iteratedInventoryWindowTitle : localisedSkinsRestorerXInventoryMenuTitles) {
			if (inventoryWindowTitle.contains(iteratedInventoryWindowTitle)) {
				wasSkinsRestorerXMenuClosed = true;
				break;
			}
		}

		if (wasSkinsRestorerXMenuClosed) {
			Player trueBukkitPlayerObject;
			if (inventoryCloseEvent.getPlayer() instanceof Player) {
				trueBukkitPlayerObject = (Player)inventoryCloseEvent.getPlayer();
			} else {
				blueMapSkinSupport.getLogger().severe("Could not determine the true Bukkit Player object for " + inventoryCloseEvent.getPlayer().getName() + "!");
				return;
			}

			blueMapSkinSupport.getLogger().info(trueBukkitPlayerObject.getName() + " opened and closed the SkinsRestorer / SkinsRestorerX menu! This action may have resulted in a skin change. Calling writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback()…");
			blueMapSkinSupport.writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(trueBukkitPlayerObject);
		}
	}
}
