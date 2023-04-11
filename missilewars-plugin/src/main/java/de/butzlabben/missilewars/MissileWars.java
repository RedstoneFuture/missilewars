/*
 * This file is part of MissileWars (https://github.com/Butzlabben/missilewars).
 * Copyright (c) 2018-2021 Daniel Nägele.
 *
 * MissileWars is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MissileWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MissileWars.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.butzlabben.missilewars;

import co.aikar.commands.PaperCommandManager;
import de.butzlabben.missilewars.commands.*;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.misc.MissileWarsPlaceholder;
import de.butzlabben.missilewars.game.signs.CheckRunnable;
import de.butzlabben.missilewars.game.signs.SignRepository;
import de.butzlabben.missilewars.game.stats.StatsFetcher;
import de.butzlabben.missilewars.listener.PlayerListener;
import de.butzlabben.missilewars.listener.SignListener;
import de.butzlabben.missilewars.player.PlayerData;
import de.butzlabben.missilewars.util.ConnectionHolder;
import de.butzlabben.missilewars.util.MoneyUtil;
import de.butzlabben.missilewars.util.SetupUtil;
import de.butzlabben.missilewars.util.stats.PreFetcher;
import de.butzlabben.missilewars.util.version.VersionUtil;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Date;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */
@Getter
public class MissileWars extends JavaPlugin {

    private static MissileWars instance;
    public final String version = getDescription().getVersion();
    private SignRepository signRepository;
    public PaperCommandManager commandManager;

    private boolean foundFAWE;

    private PlayerListener playerListener;
    private SignListener signListener;

    public MissileWars() {
        instance = this;
    }

    /**
     * @return the instance of the plugin
     */
    public static MissileWars getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        long startTime;
        long endTime;

        startTime = System.currentTimeMillis();

        sendPluginInfo();

        Logger.BOOT.log("Loading properties...");

        // delete old missile wars temp-worlds from the last server session
        deleteTempWorlds();

        Config.load();
        Messages.load();
        SetupUtil.checkMissiles();

        new File(Config.getLobbiesFolder()).mkdirs();

        this.signRepository = SignRepository.load();

        registerEvents();
        registerCommands();

        Arenas.load();
        SetupUtil.checkShields();

        GameManager.getInstance().loadGamesOnStartup();

        new Metrics(this, 3749);

        // Check if FAWE is installed
        foundFAWE = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;

        GameManager.getInstance().getGames().values().forEach(game -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!game.isIn(player.getLocation())) continue;
                game.teleportToLobbySpawn(player);
            }
        });

        MoneyUtil.giveMoney(null, -1);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new CheckRunnable(), 20, 20 * 10);

        if (Config.isPrefetchPlayers()) {
            PreFetcher.preFetchPlayers(new StatsFetcher(new Date(0L), ""));
        }
        
        checkPlaceholderAPI();

        ConfigurationSerialization.registerClass(PlayerData.class);
        
        endTime = System.currentTimeMillis();
        Logger.SUCCESS.log("MissileWars was enabled in " + (endTime - startTime) + "ms");
    }

    @Override
    public void onDisable() {
        GameManager.getInstance().disableAll();
        deleteTempWorlds();

        ConnectionHolder.close();
    }
    
    /**
     * This method checks if the PlaceholderAPI is installed. When it is 
     * installed, a message is sent to the log.
     */
    private void checkPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MissileWarsPlaceholder(this).register();
            Logger.NORMAL.log("The PlaceholderAPI is installed. New placeholders are provided by MissileWars.");
        }
    }
    
    /**
     * This method registers all events of the missilewars event listener.
     */
    private void registerEvents() {
        playerListener = new PlayerListener();
        signListener = new SignListener();

        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(signListener, this);
    }

    /**
     * This method loads the command manager and registers the missilewars commands.
     */
    private void registerCommands() {
        Logger.BOOT.log("Registering commands");

        // Using the Paper Command Manager does not mean the plugin requires Paper.
        // It simply lets it take advantage of Paper specific features if available,
        // such as Asynchronous Tab Completions.
        commandManager = new PaperCommandManager(this);

        new MWCommandCompletions(commandManager);
        
        commandManager.registerCommand(new MWCommands());
        commandManager.registerCommand(new StatsCommands());
        commandManager.registerCommand(new UserCommands());
        commandManager.registerCommand(new SetupCommands());
    }

    /**
     * This method checks if FAWE (FastAsyncWorldEdit) is installed.
     *
     * @return true, if it's installed
     */
    public boolean foundFAWE() {
        return foundFAWE;
    }

    /**
     * This methode deletes the temp arena worlds of the MW game.
     */
    private void deleteTempWorlds() {
        File[] dirs = Bukkit.getWorldContainer().listFiles();
        if (dirs == null) return;

        for (File dir : dirs) {
            if (dir.getName().startsWith("mw-")) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * This method sends information about the version, version
     * warnings (if necessary) and authors in the console.
     */
    private void sendPluginInfo() {

        Logger.BOOT.log("This server is running MissileWars v" + version + " by Butzlabben");

        if (VersionUtil.getVersion() < 8) {
            Logger.WARN.log("====================================================");
            Logger.WARN.log("It seems that you are using version older than 1.8");
            Logger.WARN.log("There is no guarantee for this to work");
            Logger.WARN.log("Proceed with extreme caution");
            Logger.WARN.log("====================================================");
        }

        if (version.contains("beta")) {
            Logger.WARN.log("NOTE: This is a beta version which means, that it may not be fully stable");
        }

        if (getDescription().getAuthors().size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (String author : getDescription().getAuthors()) {
                if (author.equals("Butzlabben"))
                    continue;
                sb.append(author);
                sb.append(" ");
            }
            Logger.BOOT.log("Other authors: " + sb);
        }
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public SignListener getSignListener() {
        return signListener;
    }
}
