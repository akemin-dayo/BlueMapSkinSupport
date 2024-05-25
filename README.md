<p align="center"><img src="https://github.com/akemin-dayo/BlueMapSkinSupport/raw/master/BlueMapSkinSupport.png" alt="BlueMapSkinSupport icon" width="128"></p>

# BlueMapSkinSupport
###### Adds support to BlueMap for acquiring skins from custom skin provider plugins, as well as offline-mode servers.

### Table of Contents
* [**What is this?**](#what-is-this)
* [**Supported custom skin provider plugins**](#supported-custom-skin-provider-plugins)
* [**How do I install this on my Minecraft server?**](#how-do-i-install-this-on-my-minecraft-server)
	* [Prerequisites](#prerequisites)
	* [Installation](#installation)
* [**How do I build/compile this?**](#how-do-i-buildcompile-this)
	* [Prerequisites](#prerequisites-1)
	* [Building (IntelliJ IDEA)](#building-intellij-idea)
	* [Building (CLI)](#building-cli)
* [**Links**](#links)
* [**License**](#license)

---

## What is this?

BlueMapSkinSupport is a plugin that allows [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap/releases) to be able to acquire and display player heads for Minecraft servers (in both online-mode or offline-mode) that are using custom skin provider plugins for custom player-defined skins, as well as offline-mode Minecraft servers that have no custom skin provider plugins installed.

Player heads are generated in the exact same way as BlueMap's native player heads.

--

## Supported custom skin provider plugins

* [SkinsRestorer / SkinsRestorerX 14.x](https://github.com/SkinsRestorer/SkinsRestorerX/releases) (※ recommended)
	* Compatibility with 15.x will be added soon.
* [CustomSkinsManager](https://github.com/Nan1t/Custom-Skins-Manager/releases) (※ deprecated, project was abandoned — support was added for this plugin only because someone requested it anyway, as I personally have always used SkinsRestorerX)

※ The use of these plugins is _not_ required — BlueMapSkinSupport will work even without them, if you have some reason to want to do so.

---

## How do I install this on my Minecraft server?

### Prerequisites
* [Paper](https://papermc.io/downloads) (※ recommended) / [Spigot](https://spigotmc.org/wiki/buildtools) / [CraftBukkit](https://spigotmc.org/wiki/buildtools/#compile-craftbukkit) for Minecraft 1.8 or higher
	* This should also work with any other Minecraft server software capable of loading Bukkit plugins.
	* If you are using [Waterfall](https://papermc.io/downloads#Waterfall) / [BungeeCord](https://spigotmc.org/wiki/bungeecord-installation), you will need to install this plugin on each individual backend Minecraft server.
* [BlueMap 3.8 or higher](https://github.com/BlueMap-Minecraft/BlueMap/releases) (※ BlueMapAPI 2.3.0 or higher)
* Optionally, one of the supported custom skin provider plugins listed above

### Installation
1. Completely shut down your Minecraft server using `/stop`.
2. Download the latest *.jar file from the [GitHub releases page](https://github.com/akemin-dayo/BlueMapSkinSupport/releases).
3. Copy the *.jar file to your `/plugins/` directory.
4. If you want, you can change how BlueMapSkinSupport behaves by modifying its configuration file located at `/plugins/BlueMapSkinSupport/config.yml`. (※ For reference, [here are the default contents of `config.yml`](src/main/resources/config.yml).)
5. Edit BlueMap's `plugin.conf` file and disable its built-in skin downloader to prevent it from overwriting BMSS-generated skins.
6. Start your server and enjoy having the correct player heads appear on BlueMap's web UI!

---

## How do I build/compile this?

### Prerequisites
* [IntelliJ IDEA](https://jetbrains.com/idea/download) (Community Edition is free!)
* If you're experienced enough with the CLI, you can just simply use the Maven CLI directly
	* `brew install maven` on macOS (requires [Homebrew](https://brew.sh/) to be installed)
	* `sudo apt install maven` on Debian-based Linux distributions
	* Follow the instructions [here](https://maven.apache.org/install.html) if you use Windows. (Honestly, I'd recommend just using IntelliJ IDEA if you're on Windows.)
* [Adoptium Temurin 8 LTS OpenJDK + HotSpot JVM](https://adoptium.net/?variant=openjdk8&jvmVariant=hotspot) (newer JDK versions also work, but I targeted JDK 8 here for compatibility reasons)
	* Adoptium is the new name for AdoptOpenJDK.

### Building (IntelliJ IDEA)
1. Clone the Git repository by running `git clone https://github.com/akemin-dayo/BlueMapSkinSupport.git` in a Terminal instance, or use a Git frontend like [SourceTree](https://sourcetreeapp.com/).
2. Open the cloned project directory in IntelliJ IDEA.
3. Click on the Maven panel on the right side and go to "Lifecycle", then double-click on `package`.
4. You will find your newly-built JAR in the `/target/` folder.

### Building (CLI)
```shell
git clone https://github.com/akemin-dayo/BlueMapSkinSupport.git
cd BlueMapSkinSupport
mvn package
```

You will then find your newly-built JAR in the `/target/` folder.

---

## Links

* [GitHub Repository](https://github.com/akemin-dayo/BlueMapSkinSupport)
* [SpigotMC Resource Page](https://spigotmc.org/resources/bluemapskinsupport.91486)
* [BukkitDev Project Page](https://dev.bukkit.org/projects/bluemapskinsupport)

---

## License

Licensed under the [MIT License](https://opensource.org/licenses/MIT).
