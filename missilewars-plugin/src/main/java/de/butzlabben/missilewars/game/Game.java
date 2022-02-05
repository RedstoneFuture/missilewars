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

package de.butzlabben.missilewars.game;

import com.google.common.base.Preconditions;
import de.butzlabben.missilewars.Config;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.timer.EndTimer;
import de.butzlabben.missilewars.game.timer.GameTimer;
import de.butzlabben.missilewars.game.timer.LobbyTimer;
import de.butzlabben.missilewars.game.timer.Timer;
import de.butzlabben.missilewars.listener.EndListener;
import de.butzlabben.missilewars.listener.GameBoundListener;
import de.butzlabben.missilewars.listener.GameListener;
import de.butzlabben.missilewars.listener.LobbyListener;
import de.butzlabben.missilewars.util.MoneyUtil;
import de.butzlabben.missilewars.util.MotdManager;
import de.butzlabben.missilewars.util.ScoreboardManager;
import de.butzlabben.missilewars.util.serialization.Serializer;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.Arena;
import de.butzlabben.missilewars.wrapper.abstracts.GameWorld;
import de.butzlabben.missilewars.wrapper.abstracts.Lobby;
import de.butzlabben.missilewars.wrapper.abstracts.MapChooseProcedure;
import de.butzlabben.missilewars.wrapper.event.GameEndEvent;
import de.butzlabben.missilewars.wrapper.event.GameStartEvent;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.game.Team;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import de.butzlabben.missilewars.wrapper.stats.FightStats;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */

@Getter
@ToString(of = {"gameWorld", "players", "lobby", "arena", "team1", "team2", "state"})
public class Game {

    private static final Map<String, Integer> cycles = new HashMap<>();
    private static int fights = 0;
    private final Map<UUID, MWPlayer> players = new HashMap<>();
    private final Map<String, Integer> votes = new HashMap<>(); // Votes for the maps.
    @Getter private final Lobby lobby;
    private final HashMap<UUID, BukkitTask> playerTasks = new HashMap<>();
    private Timer timer;
    private BukkitTask bt;
    private GameState state = GameState.LOBBY;
    private Team team1;
    private Team team2;
    @Setter private boolean draw = true;
    @Getter private boolean ready = false;
    private boolean restart = false;
    private GameWorld gameWorld;
    private long timestart;
    @Getter private Arena arena;
    private Scoreboard scoreboard;
    private ScoreboardManager scoreboardManager;
    private GameBoundListener listener;

    public Game(Lobby lobby) {
        Logger.BOOT.log("Loading game " + lobby.getDisplayName());
        this.lobby = lobby;
        if (lobby.getBukkitWorld() == null) {
            Logger.ERROR.log("Lobby world in arena \"" + lobby.getName() + "\" must not be null");
            return;
        }

        try {
            Serializer.setWorldAtAllLocations(lobby, lobby.getBukkitWorld());
        } catch (Exception exception) {
            Logger.ERROR.log("Could not inject world object at lobby " + lobby.getName());
            exception.printStackTrace();
            return;
        }

        if (lobby.getPossibleArenas().size() == 0) {
            Logger.ERROR.log(("At least one valid arena must be set at lobby " + lobby.getName()));
            return;
        }

        if (lobby.getPossibleArenas().stream().noneMatch(a -> Arenas.getFromName(a).isPresent())) {
            Logger.ERROR.log(("None of the specified arenas match a real arena for the lobby " + lobby.getName()));
            return;
        }

        gameWorld = new GameWorld(this, "");

        players.clear();

        GameBoundListener listener = new LobbyListener(this);
        Bukkit.getPluginManager().registerEvents(listener, MissileWars.getInstance());
        this.listener = listener;

        team1 = new Team(lobby.getTeam1Name(), lobby.getTeam1Color(), this);
        team2 = new Team(lobby.getTeam2Name(), lobby.getTeam2Color(), this);

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        org.bukkit.scoreboard.Team t = scoreboard.getTeam("0" + team1.getFullname());
        if (t != null)
            t.unregister();
        t = scoreboard.registerNewTeam("0" + team1.getFullname());
        t.setPrefix(team1.getColorCode());
        VersionUtil.setScoreboardTeamColor(t, ChatColor.getByChar(team1.getColorCode().charAt(1)));
        team1.setSBTeam(t);

        t = scoreboard.getTeam("1" + team2.getFullname());
        if (t != null)
            t.unregister();
        t = scoreboard.registerNewTeam("1" + team2.getFullname());
        t.setPrefix(team2.getColorCode());
        VersionUtil.setScoreboardTeamColor(t, ChatColor.getByChar(team2.getColorCode().charAt(1)));
        team2.setSBTeam(t);

        t = scoreboard.getTeam("2Guest§7");
        if (t != null)
            t.unregister();
        t = scoreboard.registerNewTeam("2Guest§7");
        t.setPrefix("§7");

        VersionUtil.setScoreboardTeamColor(t, ChatColor.GRAY);

        scoreboardManager = new ScoreboardManager(this, scoreboard);

        Logger.DEBUG.log("Registering, teleporting, etc. all players");

        for (Player all : Bukkit.getOnlinePlayers()) {
            if (!isIn(all.getLocation()))
                continue;
            Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(),
                    () -> Bukkit.getPluginManager().callEvent(new PlayerArenaJoinEvent(all, this)), 2);
        }

        // Change MOTD
        if (!Config.isMultipleLobbies()) MotdManager.getInstance().updateMOTD(this);

        Logger.DEBUG.log("Start timer");
        stopTimer();
        timer = new LobbyTimer(this, lobby.getLobbyTime());
        bt = Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), timer, 0, 20);

        if (Config.isSetup()) {
            Logger.WARN.log("Did not fully initialize lobby " + lobby.getName() + " as the plugin is in setup mode");
            return;
        }

        if (lobby.getMapChooseProcedure() == MapChooseProcedure.FIRST) {
            setArena(lobby.getArenas().get(0));
        } else if (lobby.getMapChooseProcedure() == MapChooseProcedure.MAPCYCLE) {
            final int lastMapIndex = cycles.getOrDefault(lobby.getName(), -1);
            List<Arena> arenas = lobby.getArenas();
            int index = lastMapIndex >= arenas.size() - 1 ? 0 : lastMapIndex + 1;
            cycles.put(lobby.getName(), index);
            setArena(arenas.get(index));
        } else if (lobby.getMapChooseProcedure() == MapChooseProcedure.MAPVOTING) {
            if (lobby.getArenas().size() == 1) {
                setArena(lobby.getArenas().get(0));
            }
            lobby.getArenas().forEach(arena -> votes.put(arena.getName(), 0));
        }


        Logger.DEBUG.log("Making game ready");
        ++fights;
        if (fights >= Config.getFightRestart())
            restart = true;

        FightStats.checkTables();
        Logger.DEBUG.log("Fights: " + fights);
    }

    public void startGame() {
        if (Config.isSetup()) {
            Logger.WARN.log("Did not start game. Setup mode is still enabled");
            return;
        }

        World world = gameWorld.getWorld();

        if (world == null) {
            Logger.ERROR.log("Could not start game in arena \"" + arena.getName() + "\". World is null");
            return;
        }

        stopTimer();
        timer = new GameTimer(this);
        bt = Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), timer, 5, 20);

        HandlerList.unregisterAll(listener);

        GameBoundListener listener = new GameListener(this);
        Bukkit.getPluginManager().registerEvents(listener, MissileWars.getInstance());
        this.listener = listener;

        state = GameState.INGAME;
        timestart = System.currentTimeMillis();

        applyForAllPlayers(this::startForPlayer);

        // Set intervals
        team1.updateIntervals(arena.getInterval(team1.getMembers().size()));
        team2.updateIntervals(arena.getInterval(team2.getMembers().size()));

        // Change MOTD
        if (!Config.isMultipleLobbies())
            MotdManager.getInstance().updateMOTD(this);

        Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
    }

    private void stopTimer() {
        if (bt != null)
            bt.cancel();
    }

    public void stopGame() {
        if (Config.isSetup())
            return;

        Logger.DEBUG.log("Stopping");
        state = GameState.END;
        for (BukkitTask bt : playerTasks.values()) {
            bt.cancel();
        }

        Logger.DEBUG.log("Stopping for players");
        int money = arena.getMoney().getDraw();
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (!isIn(all.getLocation()))
                continue;
            Logger.DEBUG.log("Stopping for: " + all.getName());

            all.setGameMode(GameMode.SPECTATOR);
            all.teleport(arena.getSpectatorSpawn());
            all.setHealth(all.getMaxHealth());

            VersionUtil.playDraw(all);
            if (draw) {
                if (getPlayer(all).getTeam() == null)
                    continue;
                MoneyUtil.giveMoney(all.getUniqueId(), money);
            }
        }

        stopTimer();

        HandlerList.unregisterAll(listener);
        GameBoundListener listener = new EndListener(this);
        try {
            Bukkit.getPluginManager().registerEvents(listener, MissileWars.getInstance());
        } catch (Exception ignored) {
        }
        this.listener = listener;

        timer = new EndTimer(this);
        bt = Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), timer, 5, 20);
        scoreboardManager.removeScoreboard();

        // Change MOTD
        if (!Config.isMultipleLobbies())
            MotdManager.getInstance().updateMOTD(this);

        if (getArena().isSaveStatistics()) {
            FightStats stats = new FightStats(this);
            stats.insert();
        }

        if (draw) Bukkit.getPluginManager().callEvent(new GameEndEvent(this, null));

        Logger.DEBUG.log("Stopped completely");
    }

    public void reset() {
        if (Config.isSetup())
            return;

        if (restart) {
            Bukkit.getServer().spigot().restart();
            return;
        }

        GameManager.getInstance().restartGame(lobby);
    }

    public void appendRestart() {
        restart = true;
    }

    public void draw(boolean draw) {
        this.draw = draw;
    }

    public void disable() {
        if (state == GameState.INGAME) stopGame();

        HandlerList.unregisterAll(listener);

        stopTimer();

        applyForAllPlayers(player -> player.teleport(lobby.getAfterGameSpawn()));
        if (gameWorld.getWorldName() != null) {
            gameWorld.sendPlayersBack();
            gameWorld.unload();
            gameWorld.delete();
        }
        scoreboardManager.removeScoreboard();
        team1 = null;
        team2 = null;
    }

    public boolean isInLobbyArea(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        if (world.getName().equals(lobby.getWorld())) return lobby.getArea().isInArea(location);
        return false;
    }


    public boolean isInGameArea(Location location) {
        if (isInGameWorld(location)) return arena.getGameArea().isInArea(location);
        return false;
    }

    public boolean isInGameWorld(Location location) {
        World world = location.getWorld();
        return gameWorld.isWorld(world);
    }

    public boolean isIn(Location location) {
        return isInLobbyArea(location) || isInGameWorld(location);
    }

    public MWPlayer getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public void broadcast(String message) {
        for (MWPlayer player : players.values()) {
            Player p = player.getPlayer();
            if (p != null && p.isOnline()) p.sendMessage(message);
        }
    }

    public void startForPlayer(Player player) {
        MWPlayer mwPlayer = getPlayer(player);
        if (mwPlayer == null) {
            System.err.println("[MissileWars] Error starting game at player " + player.getName());
            return;
        }

        player.teleport(mwPlayer.getTeam().getSpawn());
        ItemStack air = new ItemStack(Material.AIR);
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
        ItemMeta im = bow.getItemMeta();
        im.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
        bow.setItemMeta(im);
        VersionUtil.setUnbreakable(bow);

        player.getInventory().setItem(0, air);
        player.getInventory().setItem(8, air);
        player.getInventory().addItem(bow);
        mwPlayer.getTeam().setTeamArmor(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setLevel(0);
        player.setFireTicks(0);
        playerTasks.put(player.getUniqueId(),
                Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), mwPlayer, 0, 20));

    }

    public void setArena(Arena arena) {
        Preconditions.checkNotNull(arena);
        if (this.arena != null) {
            throw new IllegalStateException("Arena already set");
        }

        arena.getMissileConfiguration().check();
        if (arena.getMissileConfiguration().getMissiles().size() == 0) {
            throw new IllegalStateException("The game cannot be started, when 0 missiles are configured");
        }

        this.arena = arena.toBuilder().build();
        this.arena.setSpectatorSpawn(arena.getSpectatorSpawn().clone());
        this.arena.setTeam1Spawn(arena.getTeam1Spawn().clone());
        this.arena.setTeam2Spawn(arena.getTeam2Spawn().clone());

        // Load world
        this.gameWorld = new GameWorld(this, this.arena.getTemplateWorld());
        this.gameWorld.load();

        try {
            Serializer.setWorldAtAllLocations(this.arena, gameWorld.getWorld());
            team1.setSpawn(this.arena.getTeam1Spawn());
            team2.setSpawn(this.arena.getTeam2Spawn());
        } catch (Exception exception) {
            Logger.ERROR.log("Could not inject world object at arena " + this.arena.getName());
            exception.printStackTrace();
            return;
        }

        if (lobby.getMapChooseProcedure() == MapChooseProcedure.MAPVOTING) {
            this.broadcast(MessageConfig.getMessage("vote.finished").replace("%map%", this.arena.getDisplayName()));
        }
        applyForAllPlayers(p -> p.getInventory().setItem(4, new ItemStack(Material.AIR)));

        ready = true;
    }

    public void applyForAllPlayers(Consumer<Player> consumer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isIn(player.getLocation())) continue;
            consumer.accept(player);
        }
    }

    public MWPlayer addPlayer(Player player) {
        if (players.containsKey(player.getUniqueId())) return players.get(player.getUniqueId());
        MWPlayer mwPlayer = new MWPlayer(player, this);
        players.put(player.getUniqueId(), mwPlayer);
        return mwPlayer;
    }

    public void sendGameResult(Game game) {

        // Send all player of both teams her money, even of offline or online.
        team1.sendMoney();
        team2.sendMoney();

        // Send all online players of the game world her own game result message
        // as title / subtitle.
        for (Player player : gameWorld.getWorld().getPlayers()) {
            MWPlayer missileWarsPlayer = game.getPlayer(player);

            if (team1.isMember(missileWarsPlayer)) {
                team1.sendGameResultTitle(player);

            } else if (team2.isMember(missileWarsPlayer)) {
                team2.sendGameResultTitle(player);

            } else {

                if (team1.isWon()) {
                    team1.sendNeutralGameResultTitle(player);
                } else if (team2.isWon()) {
                    team2.sendNeutralGameResultTitle(player);
                }

            }
        }

    }
}
