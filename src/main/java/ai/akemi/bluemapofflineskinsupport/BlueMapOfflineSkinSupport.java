package ai.akemi.bluemapofflineskinsupport;

import de.bluecolored.bluemap.api.AssetStorage;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import ru.csm.api.services.SkinsAPI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class BlueMapOfflineSkinSupport extends JavaPlugin {
    private FileConfiguration preferences;

    private SkinsRestorerAPI skinsRestorerAPI;
    private SkinsAPI customSkinsManagerAPI;
    private BlueMapAPI blueMapAPI;

    public SkinsRestorerAPI getSkinsRestorerAPI() {
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
        getLogger().info("BlueMapOfflineSkinSupport");
        getLogger().info("(C) 2023 Karen/あけみ");
        getLogger().info("Version " + this.getDescription().getVersion());
        getLogger().info("https://github.com/akemin-dayo/BlueMapOfflineSkinSupport");

        initialisePreferences();

        if (getServer().getPluginManager().getPlugin("SkinsRestorer") != null) {
            getLogger().info("SkinsRestorer detected! Using SkinsRestorer API...");
            skinsRestorerAPI = SkinsRestorerAPI.getApi();
        } else if (getServer().getPluginManager().getPlugin("CustomSkinsManager") != null) {
            getLogger().info("CustomSkinsManager detected! Using CustomSkinsManager API...");
            customSkinsManagerAPI = getServer().getServicesManager().getRegistration(SkinsAPI.class).getProvider();
        }

        if (skinsRestorerAPI != null) {
            getLogger().info("Registering SkinsRestorer event listeners...");
            getServer().getPluginManager().registerEvents(new SkinsRestorerEventListeners(this), this);
        } else if (customSkinsManagerAPI != null) {
            getLogger().info("Registering CustomSkinsManager event listeners...");
            getServer().getPluginManager().registerEvents(new CustomSkinsManagerEventListeners(this), this);
        }

        BlueMapAPI.onEnable(api -> {
            getLogger().info("Registering BlueMap API...");
            blueMapAPI = api;
        });

        getLogger().info("Registering native Bukkit event listeners...");
        getServer().getPluginManager().registerEvents(new NativeBukkitEventListeners(this), this);
    }

    private void initialisePreferences() {
        getLogger().info("Initialising preferences...");

        if (!getDataFolder().exists()) {
            getLogger().info("Creating preferences data folder...");
            getDataFolder().mkdirs();
        }

        if (!new File(getDataFolder(), "config.yml").exists()) {
            getLogger().info("Writing a fresh copy of config.yml...");
            saveDefaultConfig();
        }

        preferences = getConfig();
        preferences.addDefault("webroot", "bluemap/web");
        preferences.addDefault("alwaysUseSkinsRestorerForSkinLookup", false);
        preferences.addDefault("alwaysUseCustomSkinsManagerForSkinLookup", false);
        preferences.addDefault("verboseLogging", false);
        preferences.addDefault("prefsRevision", 1);
        preferences.options().copyHeader(true);
        preferences.options().copyDefaults(true);
        saveConfig();

        getLogger().info("Preferences initialisation complete!");
    }

    public void logInfo(String logString) {
        if (preferences.getBoolean("verboseLogging")) {
            getLogger().info(logString);
        }
    }

    public void writeTrueCompositedPlayerHeadForBukkitPlayerAsynchronousCallback(Player targetPlayer) {
        logInfo("Notification callback received! Waiting 120 ticks (~6 seconds at 20 TPS) before actually executing...");
        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
            String offlineUUID = null;
            try {
                offlineUUID = targetPlayer.getUniqueId().toString();
            } catch (NullPointerException e) {
                getLogger().severe("targetPlayer is null! This usually happens when a player joins and leaves quickly within 120 ticks (~6 seconds at 20 TPS).");
                e.printStackTrace();
                return;
            }

            if (getServer().getPlayer(UUID.fromString(offlineUUID)) == null) {
                getLogger().severe("Underlying true Player for targetPlayer is null! This usually happens when a player joins and leaves quickly within 120 ticks (~6 seconds at 20 TPS).");
                return;
            }

            if (getSkinsRestorerAPI() != null && (preferences.getBoolean("alwaysUseSkinsRestorerForSkinLookup") || getSkinsRestorerAPI().hasSkin(targetPlayer.getName()))) {
                try {
                    logInfo(((preferences.getBoolean("alwaysUseSkinsRestorerForSkinLookup")) ? "Using the SkinsRestorer API to derive " + targetPlayer.getName() + "'s true skin." : "The player " + targetPlayer.getName() + " has a custom skin set via SkinsRestorer! Proceeding to use the SkinsRestorer API to derive their true skin..."));
                    String skinsRestorerSkinName = ((skinsRestorerSkinName = getSkinsRestorerAPI().getSkinName(targetPlayer.getName())) != null) ? skinsRestorerSkinName : targetPlayer.getName();
                    String skinsRestorerSkinBase64Blob = getSkinsRestorerAPI().getSkinData(skinsRestorerSkinName).getValue();
                    logInfo("skinsRestorerSkinBase64Blob for " + targetPlayer.getName() + " is " + skinsRestorerSkinBase64Blob);
                    String skinTextureURL = deriveSkinTextureURLStringFromBase64Blob(skinsRestorerSkinBase64Blob);
                    logInfo("skinTextureURL for " + targetPlayer.getName() + "'s skin is " + skinTextureURL + "!");
                    logInfo("Processing true composited 8x8@1x head+head2 image for " + targetPlayer.getName() + " with offline UUID " + offlineUUID + " using the player's SkinsRestorer skin, " + getSkinsRestorerAPI().getSkinName(targetPlayer.getName()) + "...");
                    writeFinalCompositedHeadImageToDiskForOfflineUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString(skinTextureURL), offlineUUID);
                    return;
                } catch (Exception e) {
                    getLogger().severe("An error occurred while attempting to acquire the SkinsRestorer skin data for " + targetPlayer.getName() + "'s true skin!");
                    e.printStackTrace();
                }
            } else if (getCustomSkinsManagerAPI() != null && (preferences.getBoolean("alwaysUseCustomSkinsManagerForSkinLookup") || getCustomSkinsManagerAPI().getPlayer(targetPlayer.getName()).hasCustomSkin())) {
                logInfo(((preferences.getBoolean("alwaysUseCustomSkinsManagerForSkinLookup")) ? "Using the CustomSkinsManager API to derive " + targetPlayer.getName() + "'s true skin." : "The player " + targetPlayer.getName() + " has a custom skin set via CustomSkinsManager! Proceeding to use the CustomSkinsManager API to derive their true skin..."));
                String skinTextureURL = getCustomSkinsManagerAPI().getPlayer(targetPlayer.getName()).getCurrentSkin().getURL();
                logInfo("skinTextureURL for " + targetPlayer.getName() + "'s skin is " + skinTextureURL + "!");
                logInfo("Processing true composited 8x8@1x head+head2 image for " + targetPlayer.getName() + " with offline UUID " + offlineUUID + " using the player's CustomSkinsManager skin...");
                writeFinalCompositedHeadImageToDiskForOfflineUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString(skinTextureURL), offlineUUID);
                return;
            } else {
                logInfo("The player " + targetPlayer.getName() + " is using a native Mojang skin!");
                logInfo("Using " + targetPlayer.getName() + "'s native Mojang UUID to derive their true skin.");
                String effectiveDerivedUUID = deriveMojangUUIDFromMojangUsername(targetPlayer.getName());
                if (effectiveDerivedUUID == null) {
                    getLogger().warning("effectiveDerivedUUID is null! This usually happens when the username " + targetPlayer.getName() + " is not actually a valid Mojang username.");
                    // getLogger().warning("Writing default fallback 8x8@1x head+head2 image for " + player.getName() + " with offline UUID " + offlineUUID + " instead...");
                    // writeFinalCompositedHeadImageToDiskForOfflineUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString("http://assets.mojang.com/SkinTemplates/alex.png"), offlineUUID);
                    return;
                }
                logInfo("Native Mojang UUID for " + targetPlayer.getName() + " is " + effectiveDerivedUUID + "!");
                logInfo("Processing true composited 8x8@1x head+head2 image for " + targetPlayer.getName() + " with offline UUID " + offlineUUID + " using effective derived Mojang UUID " + effectiveDerivedUUID + "...");
                writeFinalCompositedHeadImageToDiskForOfflineUUID(compositeUnifiedPlayerHeadTextureViaHeadAndHead2ForSkinTextureURLString(deriveSkinTextureURLStringFromMojangUUID(effectiveDerivedUUID)), offlineUUID);
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
        return new File(preferences.getString("webroot")).getAbsolutePath();
    }

    public String getConfiguredWebrootDirectoryWithTrailingSlash() {
        return getConfiguredWebrootDirectoryWithoutTrailingSlash() + "/";
    }

    public String deriveSkinTextureURLStringFromBase64Blob(String base64Blob) {
        try {
            String skinJSONString = new String(Base64.getDecoder().decode(base64Blob), StandardCharsets.UTF_8);
            JSONObject skinJSONRoot = (JSONObject) JSONValue.parseWithException(skinJSONString);
            JSONObject skinJSONTexturesDict = (JSONObject) skinJSONRoot.get("textures");
            JSONObject skinJSONSkinElement = (JSONObject) skinJSONTexturesDict.get("SKIN");
            return skinJSONSkinElement.get("url").toString();
        } catch (ParseException e) {
            getLogger().severe("A JSON parser error occurred while attempting to parse the skin's JSON data in order to derive the skinTextureURL. This usually happens when the player does not have a native Mojang skin set at all.");
            e.printStackTrace();
        }
        return null;
    }

    public String deriveMojangUUIDFromMojangUsername(String mojangUsername) {
        try {
            logInfo("Deriving Mojang UUID for the username " + mojangUsername + "...");
            String userProfileAPIURL = IOUtils.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + mojangUsername));
            JSONObject userProfileJSON = (JSONObject) JSONValue.parseWithException(userProfileAPIURL);
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
            logInfo("Deriving skinTextureURL for Mojang UUID " + mojangUUID + " via Mojang session API response...");
            String mojangSessionAPIResponse = IOUtils.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + mojangUUID));
            JSONObject mojangJSONRoot = (JSONObject) JSONValue.parseWithException(mojangSessionAPIResponse);
            JSONArray mojangJSONPropertiesArray = (JSONArray) mojangJSONRoot.get("properties");
            JSONObject mojangJSONPropertiesUnderlyingElement = (JSONObject) mojangJSONPropertiesArray.get(0);
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
            logInfo("Processing raw skin for " + skinTextureURL + "...");
            BufferedImage rawSkin = ImageIO.read(new URL(skinTextureURL));
            logInfo("Extracting head1 texture from " + skinTextureURL + "...");
            BufferedImage head1 = rawSkin.getSubimage(8, 8, 8, 8);
            logInfo("Extracting head2 texture from " + skinTextureURL + "...");
            BufferedImage head2 = rawSkin.getSubimage(40, 8, 8, 8);
            logInfo("Compositing head1+head2 textures into an unified head texture for " + skinTextureURL + "...");
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

    public void writeFinalCompositedHeadImageToDiskForOfflineUUID(BufferedImage headImage, String offlineUUID) {
        // Adding headImage to BlueMap worlds assets using BlueMapApi methods.
        for (BlueMapMap map : getBlueMapAPI().getMaps()) {
            AssetStorage assets = map.getAssetStorage();

            logInfo("Writing final composited 8x8@1x head+head2 image for offline UUID " + offlineUUID);
            try (OutputStream out = assets.writeAsset("playerheads/" + offlineUUID + ".png")) {
                ImageIO.write(headImage, "png", out);
            } catch (IOException e) {
                getLogger().severe("An I/O error occurred while attempting to write the composited 8x8@1x head+head2 image for offline UUID " + offlineUUID + "!");
                e.printStackTrace();
            }
        }
    }
}
