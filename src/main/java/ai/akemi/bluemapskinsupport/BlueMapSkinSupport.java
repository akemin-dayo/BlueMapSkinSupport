package ai.akemi.bluemapskinsupport;

import org.apache.commons.io.IOUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;

// Bukkit
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// BlueMapAPI >= 2.0.0
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.WebApp;

// SkinsRestorerX >= 15.0.0
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;

// CustomSkinsManager (※ deprecated)
import ru.csm.api.services.SkinsAPI;

public class BlueMapSkinSupport extends JavaPlugin {
	private FileConfiguration preferences;

	private SkinsRestorer skinsRestorerAPI; // SkinsRestorerX 15.x
	private SkinsAPI customSkinsManagerAPI;
	private BlueMapAPI blueMapAPI;

	public SkinsRestorer getSkinsRestorerAPI() {
		return skinsRestorerAPI;
	}

	public SkinsAPI getCustomSkinsManagerAPI() {
		return customSkinsManagerAPI;
	}

	public BlueMapAPI getBlueMapAPI() {
		return blueMapAPI;
	}

	@Override
	public void onEnable() {
		getLogger().info("BlueMapSkinSupport");
		getLogger().info("(C) 2021-2024 Karen/あけみ (akemin_dayo)");
		getLogger().info("Version " + this.getDescription().getVersion());
		getLogger().info("https://github.com/akemin-dayo/BlueMapSkinSupport");

		// Initialise preferences
		initialisePreferences();

		// Get BlueMapAPI instance upon initialisation callback
		BlueMapAPI.onEnable(blueMapAPIInstance -> {
			getLogger().info("Initialising BlueMap API…");
			blueMapAPI = blueMapAPIInstance;
		});

		// Get custom skin provider plugin API instance (※ only one can be present at a time)
		if (getServer().getPluginManager().getPlugin("SkinsRestorer") != null) {
			getLogger().info("SkinsRestorer / SkinsRestorerX detected! Using SkinsRestorer / SkinsRestorerX API…");
			// SkinsRestorerX 15.x
			skinsRestorerAPI = SkinsRestorerProvider.get();
			if (!VersionProvider.isCompatibleWith("15")) {
				getLogger().warning("The version of SkinsRestorer / SkinsRestorerX that is currently installed (" + VersionProvider.getVersionInfo() + ") is not fully supported! BlueMapSkinSupport may or may not work correctly on this version.");
			}
		} else if (getServer().getPluginManager().getPlugin("CustomSkinsManager") != null) {
			getLogger().info("CustomSkinsManager detected! Using CustomSkinsManager API…");
			customSkinsManagerAPI = getServer().getServicesManager().getRegistration(SkinsAPI.class).getProvider();
		}

		// Register custom skin provider plugin event listeners
		if (skinsRestorerAPI != null) {
			getLogger().info("Registering SkinsRestorer / SkinsRestorerX event listeners…");
			getSkinsRestorerAPI().getEventBus().subscribe(this, SkinApplyEvent.class, new SkinsRestorerSkinApplyEventEventBusListener(this));
		} else if (customSkinsManagerAPI != null) {
			getLogger().info("Registering CustomSkinsManager event listeners…");
			getServer().getPluginManager().registerEvents(new CustomSkinsManagerEventListeners(this), this);
		}

		// Register native Bukkit event listeners
		getLogger().info("Registering native Bukkit event listeners…");
		getServer().getPluginManager().registerEvents(new NativeBukkitEventListeners(this), this);
	}

	private void initialisePreferences() {
		getLogger().info("Initialising preferences…");

		if (!getDataFolder().exists()) {
			getLogger().info("Creating preferences data folder…");
			getDataFolder().mkdirs();
		}

		if (!new File(getDataFolder(), "config.yml").exists()) {
			getLogger().info("Writing a fresh copy of config.yml…");
			saveDefaultConfig();
		}

		preferences = getConfig();

		// Define default preference values
		preferences.addDefault("alsoWriteToLegacyUnifiedDirectory", false);
		preferences.addDefault("alwaysUseCustomSkinProviderPluginForSkinLookup", false);
		preferences.addDefault("verboseLogging", false);

		// Configure preference writer
		preferences.options().copyHeader(true);
		preferences.options().copyDefaults(true);

		// Migrate preference values
		if (preferences.getBoolean("alwaysUseSkinsRestorerForSkinLookup") || preferences.getBoolean("alwaysUseCustomSkinsManagerForSkinLookup")) {
			preferences.set("alwaysUseCustomSkinProviderPluginForSkinLookup", true);
		}

		// Remove deprecated preference keys
		preferences.set("webroot", null);
		preferences.set("alwaysUseSkinsRestorerForSkinLookup", null);
		preferences.set("alwaysUseCustomSkinsManagerForSkinLookup", null);

		// Set internal preferences file format revision number
		preferences.set("prefsRevision", 2);

		// Write preferences to disk
		saveConfig();

		getLogger().info("Preferences initialisation complete!");
	}

	public void logInfo(String logString) {
		if (preferences.getBoolean("verboseLogging")) {
			getLogger().info(logString);
		}
	}

	public boolean doesPlayerHaveCustomSkinSetViaSkinsRestorerX(Player targetPlayer) {
		// SkinsRestorerX 15.x
		return getSkinsRestorerAPI().getPlayerStorage().getSkinIdOfPlayer(targetPlayer.getUniqueId()).isPresent();
	}

	public void writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(Player targetPlayer) {
		logInfo("Notification callback received! Waiting 120 ticks (~6 seconds at 20 TPS) before actually executing…");
		getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
			UUID playerUUID;
			String playerUUIDString;
			try {
				playerUUID = targetPlayer.getUniqueId();
				playerUUIDString = playerUUID.toString();
			} catch (NullPointerException e) {
				getLogger().severe("targetPlayer is null! This usually happens when a player joins and leaves quickly within 120 ticks (~6 seconds at 20 TPS).");
				e.printStackTrace();
				return;
			}

			if (getServer().getPlayer(playerUUID) == null) {
				getLogger().severe("Underlying true Player for targetPlayer is null! This usually happens when a player joins and leaves quickly within 120 ticks (~6 seconds at 20 TPS).");
				return;
			}

			if (getSkinsRestorerAPI() != null && (preferences.getBoolean("alwaysUseCustomSkinProviderPluginForSkinLookup") || doesPlayerHaveCustomSkinSetViaSkinsRestorerX(targetPlayer))) {
				try {
					logInfo(((preferences.getBoolean("alwaysUseCustomSkinProviderPluginForSkinLookup")) ? "Using the SkinsRestorer / SkinsRestorerX API to derive " + targetPlayer.getName() + "'s true skin." : "The player " + targetPlayer.getName() + " has a custom skin set via SkinsRestorer / SkinsRestorerX! Proceeding to use the SkinsRestorer / SkinsRestorerX API to derive their true skin…"));

					// SkinsRestorerX 15.x
					Optional<SkinProperty> skinsRestorerSkin = getSkinsRestorerAPI().getPlayerStorage().getSkinForPlayer(playerUUID, targetPlayer.getName());
					Optional<SkinIdentifier> skinsRestorerSkinID = getSkinsRestorerAPI().getPlayerStorage().getSkinIdOfPlayer(playerUUID);
					if (!skinsRestorerSkin.isPresent()) {
						throw new Exception("Call to SkinsRestorerX method selector getSkinForPlayer() failed and returned nil!");
					}
					String skinsRestorerSkinName = (skinsRestorerSkinID.isPresent()) ? skinsRestorerSkinID.get().getIdentifier() : deriveMojangUUIDFromMojangUsername(targetPlayer.getName());
					String skinsRestorerSkinBase64Blob = skinsRestorerSkin.get().getValue();

					logInfo("skinsRestorerSkinBase64Blob for " + targetPlayer.getName() + " is " + skinsRestorerSkinBase64Blob);
					String skinTextureURL = deriveSkinTextureURLStringFromBase64Blob(skinsRestorerSkinBase64Blob);
					logInfo("skinTextureURL for " + targetPlayer.getName() + "'s skin is " + skinTextureURL + "!");
					logInfo("Processing true composited 8x8@1x head+head2 image for " + targetPlayer.getName() + " with player UUID " + playerUUIDString + " using the player's SkinsRestorer / SkinsRestorerX skin with name or ID " + skinsRestorerSkinName + "…");
					writeFinalCompositedHeadImageToDiskForPlayerUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString(skinTextureURL), playerUUIDString);
					return;
				} catch (Exception e) {
					getLogger().severe("An error occurred while attempting to acquire the SkinsRestorer / SkinsRestorerX skin data for " + targetPlayer.getName() + "'s true skin!");
					e.printStackTrace();
				}
			} else if (getCustomSkinsManagerAPI() != null && (preferences.getBoolean("alwaysUseCustomSkinProviderPluginForSkinLookup") || getCustomSkinsManagerAPI().getPlayer(targetPlayer.getName()).hasCustomSkin())) {
				logInfo(((preferences.getBoolean("alwaysUseCustomSkinProviderPluginForSkinLookup")) ? "Using the CustomSkinsManager API to derive " + targetPlayer.getName() + "'s true skin." : "The player " + targetPlayer.getName() + " has a custom skin set via CustomSkinsManager! Proceeding to use the CustomSkinsManager API to derive their true skin…"));
				String skinTextureURL = getCustomSkinsManagerAPI().getPlayer(targetPlayer.getName()).getCurrentSkin().getURL();
				logInfo("skinTextureURL for " + targetPlayer.getName() + "'s skin is " + skinTextureURL + "!");
				logInfo("Processing true composited 8x8@1x head+head2 image for " + targetPlayer.getName() + " with player UUID " + playerUUIDString + " using the player's CustomSkinsManager skin…");
				writeFinalCompositedHeadImageToDiskForPlayerUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString(skinTextureURL), playerUUIDString);
				return;
			} else {
				logInfo("The player " + targetPlayer.getName() + " is using a native Mojang skin!");
				logInfo("Using " + targetPlayer.getName() + "'s native Mojang UUID to derive their true skin.");
				String effectiveDerivedUUID = deriveMojangUUIDFromMojangUsername(targetPlayer.getName());
				if (effectiveDerivedUUID == null) {
					getLogger().warning("effectiveDerivedUUID is null! This usually happens when the username " + targetPlayer.getName() + " is not actually a valid Mojang username.");
					// getLogger().warning("Writing default fallback 8x8@1x head+head2 image for " + player.getName() + " with player UUID " + playerUUIDString + " instead…");
					// writeFinalCompositedHeadImageToDiskForPlayerUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString("http://assets.mojang.com/SkinTemplates/alex.png"), playerUUIDString);
					return;
				}
				logInfo("Native Mojang UUID for " + targetPlayer.getName() + " is " + effectiveDerivedUUID + "!");
				logInfo("Processing true composited 8x8@1x head+head2 image for " + targetPlayer.getName() + " with player UUID " + playerUUIDString + " using effective derived Mojang UUID " + effectiveDerivedUUID + "…");
				writeFinalCompositedHeadImageToDiskForPlayerUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString(deriveSkinTextureURLStringFromMojangUUID(effectiveDerivedUUID)), playerUUIDString);
			}
		}, 120);
	}

	public String getCurrentBukkitServerRootDirectoryWithoutTrailingSlash() {
		return new File("").getAbsolutePath();
	}

	public String getCurrentBukkitServerRootDirectoryWithTrailingSlash() {
		return getCurrentBukkitServerRootDirectoryWithoutTrailingSlash() + "/";
	}

	public String getConfiguredWebrootDirectoryWithoutTrailingSlash() {
		return getBlueMapAPI().getWebApp().getWebRoot().toAbsolutePath().toString();
	}

	public String getConfiguredWebrootDirectoryWithTrailingSlash() {
		return getConfiguredWebrootDirectoryWithoutTrailingSlash() + "/";
	}

	public String deriveSkinTextureURLStringFromBase64Blob(String base64Blob) {
		try {
			String skinJSONString = new String(Base64.getDecoder().decode(base64Blob), StandardCharsets.UTF_8);
			JSONObject skinJSONRoot = (JSONObject)JSONValue.parseWithException(skinJSONString);
			JSONObject skinJSONTexturesDict = (JSONObject)skinJSONRoot.get("textures");
			JSONObject skinJSONSkinElement = (JSONObject)skinJSONTexturesDict.get("SKIN");
			return skinJSONSkinElement.get("url").toString();
		} catch (ParseException e) {
			getLogger().severe("A JSON parser error occurred while attempting to parse the skin's JSON data in order to derive the skinTextureURL. This usually happens when the player does not have a native Mojang skin set at all.");
			e.printStackTrace();
		}
		return null;
	}

	public String deriveMojangUUIDFromMojangUsername(String mojangUsername) {
		try {
			logInfo("Deriving Mojang UUID for the username " + mojangUsername + "…");
			String userProfileAPIURL = IOUtils.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + mojangUsername));
			JSONObject userProfileJSON = (JSONObject)JSONValue.parseWithException(userProfileAPIURL);
			String derivedMojangUUID = userProfileJSON.get("id").toString();
			logInfo("Mojang UUID for Mojang username " + mojangUsername + " is " + derivedMojangUUID + "!");
			return derivedMojangUUID;
		} catch (ParseException e) {
			getLogger().severe("An JSON parser error occurred while attempting to parse the Mojang user profile API response for " + mojangUsername + "! This usually happens when the specified username is not a valid Mojang username, or the Mojang API is inaccessible or down.");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			getLogger().severe("An invalid API request URL was somehow derived for " + mojangUsername + "'s user profile! This should never happen.");
			e.printStackTrace();
		} catch (IOException e) {
			getLogger().severe("A network error occurred while attempting to receive a response from the Mojang user profile API in order to derive the Mojang UUID for the username " + mojangUsername + "! This usually happens when the specified username is not a valid Mojang username, or the Mojang API is inaccessible or down.");
			e.printStackTrace();
		}
		return null;
	}

	public String deriveSkinTextureURLStringFromMojangUUID(String mojangUUID) {
		try {
			logInfo("Deriving skinTextureURL for Mojang UUID " + mojangUUID + " via Mojang session API response…");
			String mojangSessionAPIResponse = IOUtils.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + mojangUUID));
			JSONObject mojangJSONRoot = (JSONObject)JSONValue.parseWithException(mojangSessionAPIResponse);
			JSONArray mojangJSONPropertiesArray = (JSONArray)mojangJSONRoot.get("properties");
			JSONObject mojangJSONPropertiesUnderlyingElement = (JSONObject)mojangJSONPropertiesArray.get(0);
			String mojangSkinBase64Blob = mojangJSONPropertiesUnderlyingElement.get("value").toString();
			logInfo("mojangSkinBase64Blob for Mojang UUID " + mojangUUID + " is " + mojangSkinBase64Blob);
			String skinTextureURL = deriveSkinTextureURLStringFromBase64Blob(mojangSkinBase64Blob);
			logInfo("Derived skin texture URL for Mojang UUID " + mojangUUID + " is " + skinTextureURL + "!");
			return skinTextureURL;
		} catch (ParseException e) {
			getLogger().severe("A JSON parser error occurred while attempting to parse the Mojang session API response for the Mojang UUID " + mojangUUID + " in order to derive the skinTextureURL!");
			e.printStackTrace();
		} catch (IOException e) {
			getLogger().severe("A network error occurred while attempting to receive a response from the Mojang session API for the Mojang UUID " + mojangUUID + "!");
			e.printStackTrace();
		}
		return null;
	}

	public BufferedImage compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString(String skinTextureURL) {
		// BlueMap generates 8x8@1x player head images that composite the head and head2 together.
		// The stock functionality is replicated 100% exactly here. (Including saving to a 32-bit PNG!)
		try {
			logInfo("Processing raw skin for " + skinTextureURL + "…");
			BufferedImage rawSkin = ImageIO.read(new URL(skinTextureURL));
			logInfo("Extracting head1 texture from " + skinTextureURL + "…");
			BufferedImage head1 = rawSkin.getSubimage(8, 8, 8, 8);
			logInfo("Extracting head2 texture from " + skinTextureURL + "…");
			BufferedImage head2 = rawSkin.getSubimage(40, 8, 8, 8);
			logInfo("Compositing head1+head2 textures into an unified head texture for " + skinTextureURL + "…");
			BufferedImage composite = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
			Graphics compositeGraphics = composite.getGraphics();
			compositeGraphics.drawImage(head1, 0, 0, null);
			compositeGraphics.drawImage(head2, 0, 0, null);
			logInfo("Composition of head1+head2 textures completed for " + skinTextureURL + "!");
			return composite;
		} catch (IOException e) {
			getLogger().severe("A network error occurred while attempting to read the Minecraft skin texture located at " + skinTextureURL + "!");
			e.printStackTrace();
		}
		return null;
	}

	public void writeFinalCompositedHeadImageToDiskForPlayerUUID(BufferedImage headImage, String playerUUIDString) {
		// BlueMap >= 3.8 (BlueMapAPI >= 2.3.0) changed the way how playerheads are stored — instead of in a unified common directory, they're now stored per map for some reason.
		// ※ If you want to use this plugin with BlueMap versions all the way down to BlueMap 2.1 for some reason, just comment this entire for loop out, change the BlueMapAPI version to `v2.0.0` in pom.xml, and make sure `alsoWriteToLegacyUnifiedDirectory` is enabled.
		for (BlueMapMap iteratedMap : getBlueMapAPI().getMaps()) {
			logInfo("Writing final composited 8x8@1x head+head2 image for player UUID " + playerUUIDString + " to asset storage directory for map ID " + iteratedMap.getId() + "…");
			try (OutputStream finalCompositedHeadImage = iteratedMap.getAssetStorage().writeAsset("playerheads/" + playerUUIDString + ".png")) {
				ImageIO.write(headImage, "png", finalCompositedHeadImage);
			} catch (IOException e) {
				getLogger().severe("An I/O error occurred while attempting to write the composited 8x8@1x head+head2 image for player UUID " + playerUUIDString + "!");
				getLogger().severe("Please make sure that your filesystem permissions are set correctly!");
				e.printStackTrace();
			}
		}

		// If the preference is enabled, also write to the legacy unified directory.
		// This exists mostly just for my own use-case, I don't think anyone else would find this useful.
		if (preferences.getBoolean("alsoWriteToLegacyUnifiedDirectory")) {
			String blueMapWebUIPlayerHeadsPathWithTrailingSlash = getConfiguredWebrootDirectoryWithTrailingSlash() + "assets/playerheads/";

			File blueMapWebUIPlayerHeadsDirectory = new File(blueMapWebUIPlayerHeadsPathWithTrailingSlash);
			if (!blueMapWebUIPlayerHeadsDirectory.exists()) {
				blueMapWebUIPlayerHeadsDirectory.mkdirs();
			}

			File finalCompositedHeadImage = new File(blueMapWebUIPlayerHeadsPathWithTrailingSlash, playerUUIDString + ".png");
			logInfo("Writing final composited 8x8@1x head+head2 image for player UUID " + playerUUIDString + " to " + finalCompositedHeadImage.getAbsolutePath() + "…");
			try {
				ImageIO.write(headImage, "png", finalCompositedHeadImage);
			} catch (IOException e) {
				getLogger().severe("An I/O error occurred while attempting to write the composited 8x8@1x head+head2 image for player UUID " + playerUUIDString + " to legacy unified player head directory at " + finalCompositedHeadImage.getAbsolutePath() + "!");
				getLogger().severe("Please make sure that your filesystem permissions are set correctly!");
				e.printStackTrace();
			}
		}
	}
}
