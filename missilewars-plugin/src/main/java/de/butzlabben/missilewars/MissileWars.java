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
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.misc.MissileWarsPlaceholder;
import de.butzlabben.missilewars.game.schematics.paste.FawePasteProvider;
import de.butzlabben.missilewars.game.schematics.paste.Paster;
import de.butzlabben.missilewars.game.signs.SignRepository;
import de.butzlabben.missilewars.game.stats.StatsFetcher;
import de.butzlabben.missilewars.initialization.FileManager;
import de.butzlabben.missilewars.initialization.GamesInitialization;
import de.butzlabben.missilewars.listener.PlayerListener;
import de.butzlabben.missilewars.listener.SignListener;
import de.butzlabben.missilewars.util.ConnectionHolder;
import de.butzlabben.missilewars.util.MoneyUtil;
import de.butzlabben.missilewars.util.stats.PreFetcher;
import de.butzlabben.missilewars.util.version.VersionUtil;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */
@Getter
public class MissileWars extends JavaPlugin {

    @Getter
    private static MissileWars instance;
    
    public final String version = getDescription().getVersion();
    private SignRepository signRepository;
    public PaperCommandManager commandManager;
    
    @Getter private PlayerListener playerListener;
    @Getter private SignListener signListener;
    
    @Getter private Paster schematicPaster;
    
    public MissileWars() {
        instance = this;
    }

    @Override
    public void onEnable() {
        long startTime;
        long endTime;

        startTime = System.currentTimeMillis();

        sendPluginInfo();

        Logger.BOOT.log("Loading properties...");
        
        FileManager.setupRoutine();
        
        signRepository = SignRepository.load();
        
        registerEvents();
        registerCommands();

        // special Dependency-Management:
        new Metrics(this, 3749);
        
        initialWeSupport();
        initialPapiSupport();
        MoneyUtil.giveMoney(null, -1);
        
        GamesInitialization.initialize();
        
        // Warm-up for Stats:
        if (Config.isPrefetchPlayers()) {
            PreFetcher.preFetchPlayers(new StatsFetcher(new Date(0L), ""));
        }
        
        endTime = System.currentTimeMillis();
        Logger.SUCCESS.log("MissileWars was enabled in " + (endTime - startTime) + "ms");
        
    }

    @Override
    public void onDisable() {
        
        GameManager.getInstance().disableAll();
        FileManager.shotDownRoutine();
        ConnectionHolder.close();
    }
    
    /**
     * This method checks which kind of WorldEdit Solution is installed. The paste 
     * supplier is prepared on the basis of this.
     */
    private void initialWeSupport() {
        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            schematicPaster = new FawePasteProvider();
            Logger.NORMAL.log("FastAsyncWorldEdit is installed. The Schematic Paster is prepared for the behavior of FAWE.");
        } else {
            schematicPaster = new FawePasteProvider();
            Logger.NORMAL.log("(Normal) WorldEdit is installed. The Schematic Paster is prepared for the behavior of WE.");
        }
    }

    /**
     * This method checks if the PlaceholderAPI is installed. When it is
     * installed, a message is sent to the log.
     */
    private void initialPapiSupport() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MissileWarsPlaceholder(this).register();
            Logger.NORMAL.log("The PlaceholderAPI is installed. New placeholders are provided by MissileWars.");
        }
    }
    
    /**
     * This method registers all events of the MissileWars event listener.
     */
    private void registerEvents() {
        playerListener = new PlayerListener();
        signListener = new SignListener();

        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(signListener, this);
    }

    /**
     * This method loads the command manager and registers the MissileWars commands.
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
     * This method sends information about the version, version
     * warnings (if necessary) and authors in the console.
     */
    private void sendPluginInfo() {

        Logger.BOOT.log("This server is running MissileWars v" + version + " by Butzlabben");

        if (VersionUtil.getVersion() < 20) {
            Logger.WARN.log("====================================================");
            Logger.WARN.log("It seems that you are using version older than 1.20.");
            Logger.WARN.log("There is no guarantee for this to work.");
            Logger.WARN.log("====================================================");
        }

        if (version.contains("snapshot") || version.contains("dev")) {
            Logger.WARN.log("NOTE: This is a snapshot for testing. Errors may occur in new or revised modules. " +
                    "Do not use this version on a production server!");
        }
        
    }
    
}
