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

import com.pro_crafting.mc.commandframework.CommandFramework;
import de.butzlabben.missilewars.cmd.MWCommands;
import de.butzlabben.missilewars.cmd.StatsCommands;
import de.butzlabben.missilewars.cmd.UserCommands;
import de.butzlabben.missilewars.game.Arenas;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.listener.PlayerListener;
import de.butzlabben.missilewars.listener.signs.ClickListener;
import de.butzlabben.missilewars.listener.signs.ManageListener;
import de.butzlabben.missilewars.util.ConnectionHolder;
import de.butzlabben.missilewars.util.MoneyUtil;
import de.butzlabben.missilewars.util.SetupUtil;
import de.butzlabben.missilewars.util.stats.PreFetcher;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.signs.CheckRunnable;
import de.butzlabben.missilewars.wrapper.signs.SignRepository;
import de.butzlabben.missilewars.wrapper.stats.StatsFetcher;
import java.io.File;
import java.util.Date;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */
@Getter
public class MissileWars extends JavaPlugin {

    private static MissileWars instance;
    public final String version = getDescription().getVersion();
    private CommandFramework framework;
    private SignRepository signRepository;

    private boolean foundFAWE;

    public MissileWars() {
        instance = this;
    }

    /**
     * @return the instance
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
        MessageConfig.load();
        SetupUtil.checkMissiles();

        new File(Config.getArenaFolder()).mkdirs();
        new File(Config.getLobbiesFolder()).mkdirs();

        SignRepository repository = SignRepository.load();
        if (repository == null) {
            repository = new SignRepository();
            repository.save();
        }
        this.signRepository = repository;

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClickListener(), this);
        Bukkit.getPluginManager().registerEvents(new ManageListener(), this);

        Logger.BOOT.log("Registering commands");
        framework = new CommandFramework(this);
        framework.registerCommands(new MWCommands());
        framework.registerCommands(new StatsCommands());
        framework.registerCommands(new UserCommands());

        Arenas.load();
        SetupUtil.checkShields();

        GameManager.getInstance().loadGames();

        new Metrics(this, 3749);

        // Check if FAWE is installed
        foundFAWE = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;

        GameManager.getInstance().getGames().values().forEach(game -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!game.isIn(player.getLocation())) continue;
                game.addPlayer(player);
            }
        });

        MoneyUtil.giveMoney(null, -1);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new CheckRunnable(), 20, 20 * 10);

        if (Config.isPrefetchPlayers()) {
            PreFetcher.preFetchPlayers(new StatsFetcher(new Date(0L), ""));
        }

        endTime = System.currentTimeMillis();
        Logger.SUCCESS.log("MissileWars was enabled in " + (endTime - startTime) + "ms");
    }

    @Override
    public void onDisable() {
        GameManager.getInstance().disableAll();
        deleteTempWorlds();

        // TODO
        File missiles = new File(getDataFolder(), "missiles.zip");
        File arena = new File(getDataFolder(), "MissileWars-Arena.zip");
        FileUtils.deleteQuietly(missiles);
        FileUtils.deleteQuietly(arena);

        ConnectionHolder.close();
    }

    /**
     * @return true, of FAWE is installed
     */
    public boolean foundFAWE() {
        return foundFAWE;
    }

    /**
     * This methode delete the temp arena worlds of the MW game.
     */
    private void deleteTempWorlds() {
        File[] dirs = Bukkit.getWorldContainer().listFiles();
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
     * This methode send info about the version, version warnings (if needed) and the autors
     * in the console.
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
}
