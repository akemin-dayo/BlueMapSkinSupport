<p align="center"><img src="https://github.com/akemin-dayo/BlueMapOfflineSkinSupport/raw/master/BlueMapOfflineSkinSupport.png" alt="BlueMapOfflineSkinSupport icon" width="128"></p>

# BlueMapOfflineSkinSupport
###### Adds proper offline and custom skin acquisition support to BlueMap's web UI.

### Table of Contents
* [**What is this?**](#what-is-this)
* [**How do I install this on my Minecraft server?**](#how-do-i-install-this-on-my-minecraft-server)
	* [Prerequisites](#prerequisites)
	* [Installation](#installation)
* [**How do I build/compile this?**](#how-do-i-buildcompile-this)
	* [Prerequisites](#prerequisites-1)
	* [Building (IntelliJ IDEA)](#building-intellij-idea)
	* [Building (CLI)](#building-cli)
* [**Links**](#links)
* [**License**](#license)
* [**What's the difference between this, and the plugin "BlueMap Skins"?**](#whats-the-difference-between-this-and-the-plugin-bluemap-skins)

## What is this?

BlueMapOfflineSkinSupport is a plugin that allows to [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap/releases) properly acquire and display player heads for offline-mode Minecraft servers, as well as online-mode (and offline-mode) servers that allow the use of player-defined custom skins using [SkinsRestorer](https://github.com/SkinsRestorer/SkinsRestorerX/releases) or [CustomSkinsManager](https://spigotmc.org/resources/57760). (※ The use of these plugins is not required.)

Player heads are generated in the exact same way as BlueMap's native player heads, and the web root directory, among other options are fully configurable.

---

## How do I install this on my Minecraft server?

### Prerequisites
* [Paper](https://papermc.io/downloads) (recommended) / [Spigot](https://spigotmc.org/wiki/buildtools) / [CraftBukkit](https://spigotmc.org/wiki/buildtools/#compile-craftbukkit) for Minecraft 1.8 or higher
	* This should also work with any other Minecraft server software capable of loading Bukkit plugins.
	* If you are using [Waterfall](https://papermc.io/downloads#Waterfall) / [BungeeCord](https://spigotmc.org/wiki/bungeecord-installation), you will need to install this plugin on each individual backend Minecraft server.
* Optionally: [SkinsRestorer](https://github.com/SkinsRestorer/SkinsRestorerX/releases) or [CustomSkinsManager](https://spigotmc.org/resources/57760)
	
### Installation
1. Completely shut down your Minecraft server using `/stop`.
2. Download the latest *.jar file from the [GitHub releases page](https://github.com/akemin-dayo/BlueMapOfflineSkinSupport/releases).
3. Copy the *.jar file to your `/plugins/` directory.
4. If you changed your BlueMap web root directory, you'll want to make sure that the value also matches in the configuration file located at `/plugins/BlueMapOfflineSkinSupport/config.yml`. ※ For reference, [here are the default contents of `config.yml`](src/main/resources/config.yml).
5. Edit BlueMap's `plugin.conf` file and disable its built-in skin downloader to prevent it from overwriting BMOSS-generated skins.
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
1. Clone the Git repository by running `git clone https://github.com/akemin-dayo/BlueMapOfflineSkinSupport.git` in a Terminal instance, or use a Git frontend like [SourceTree](https://sourcetreeapp.com/).
2. Open the cloned project directory in IntelliJ IDEA.
3. Click on the Maven panel on the right side and go to "Lifecycle", then double-click on `package`.
4. You will find your newly-built JAR in the `/target/` folder.

### Building (CLI)
```shell
git clone https://github.com/akemin-dayo/BlueMapOfflineSkinSupport.git
cd BlueMapOfflineSkinSupport
mvn package
```

You will then find your newly-built JAR in the `/target/` folder.

---

## Links

* [GitHub Repository](https://github.com/akemin-dayo/BlueMapOfflineSkinSupport)
* [SpigotMC Resource Page](https://spigotmc.org/resources/bluemapofflineskinsupport.91486)
* [BukkitDev Project Page](https://dev.bukkit.org/projects/bluemapofflineskinsupport)

---

## License

Licensed under the [MIT License](https://opensource.org/licenses/MIT).

---

## What's the difference between this, and the plugin "BlueMap Skins"?

When I initially noticed the problem with BlueMap and player skins on offline-mode Minecraft servers (or online-mode Minecraft servers with SkinsRestorer for custom skins), I looked around to see if there was already an existing solution and found a plugin written by [otDan](https://spigotmc.org/members/111443) called [BlueMap Skins](https://spigotmc.org/resources/90284) (which I will call BMS from hereon forward) that claimed to fix the problem.

While it _did_ technically do that, there were unfortunately… a few major issues with BMS that made it unsuitable for my use.

1. It is not possible to configure the web root directory that BMS writes to. It's hard-coded to use `bluemap/web`. I figured that this at least could be… _worked around_ via symlinks and was about to move on, but then I found another bug…
2. I have my Minecraft server (which uses [Multiverse](https://github.com/Multiverse/Multiverse-Core)) configured to use a world container, so that all the worlds on the server go in a subdirectory (`/worlds/` in my case), which makes everything much neater. Unfortunately, BMS appears to attempt to derive the root directory of the Minecraft server by using `getWorldContainer()`, which results in it incorrectly writing to the `/worlds/` subdirectory. (🍍˃̶͈̀ロ˂̶͈́)੭ꠥ⁾⁾ But again, with symlinks, this too can be worked around (as much of a bodge as it may be), I thought… until I discovered the next issue.
3. For any player that is using a custom SkinsRestorer skin, BMS will incorrectly treat the skin name as a player name(!?!?), causing a _completely_ incorrect player head to be generated. _This_ issue was really what made BMS unusable for me.

There were various other minor issues, too (for instance, the generated images are 20 times larger than they should be, and so on), but the above 3 were what bothered me the most. Of course, I looked around to see if the plugin was open-source, so I could help fix these issues, but it was not.

At this point, I figured it'd be easier to just write my own plugin from scratch, and so… that's what I did. After a few hours, this project was finished, and now you're seeing it here.

_**Disclaimer:** While BlueMapOfflineSkinSupport contains only my own original code/implementation, _technically_ the core idea of "just simply overwrite the player head images in BlueMap's webroot" _was_ otDan's to begin with, so I decided to speak with him first to notify him of this project and make sure he was okay with me releasing it publicly. He gave his full approval._
