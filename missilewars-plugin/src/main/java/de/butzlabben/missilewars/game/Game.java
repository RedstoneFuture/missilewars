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
import de.butzlabben.missilewars.util.MotdManager;
import de.butzlabben.missilewars.util.ScoreboardManager;
import de.butzlabben.missilewars.util.serialization.Serializer;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.abstracts.Arena;
import de.butzlabben.missilewars.wrapper.abstracts.GameWorld;
import de.butzlabben.missilewars.wrapper.abstracts.Lobby;
import de.butzlabben.missilewars.wrapper.abstracts.MapChooseProcedure;
import de.butzlabben.missilewars.wrapper.event.GameStartEvent;
import de.butzlabben.missilewars.wrapper.event.PlayerArenaJoinEvent;
import de.butzlabben.missilewars.wrapper.game.MissileGameEquipment;
import de.butzlabben.missilewars.wrapper.game.SpecialGameEquipment;
import de.butzlabben.missilewars.wrapper.game.Team;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import de.butzlabben.missilewars.wrapper.stats.FightStats;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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
    private final Lobby lobby;
    private final HashMap<UUID, BukkitTask> playerTasks = new HashMap<>();
    private Timer timer;
    private BukkitTask bt;
    private GameState state = GameState.LOBBY;
    private Team team1;
    private Team team2;
    private boolean ready = false;
    private boolean restart = false;
    private GameWorld gameWorld;
    private long timestart;
    private Arena arena;
    private ScoreboardManager scoreboardManager;
    private GameBoundListener listener;
    private ItemStack customBow;
    private ItemStack customPickaxe;
    private MissileGameEquipment missileEquipment;
    private SpecialGameEquipment specialEquipment;

    public Game(Lobby lobby) {
        Logger.BOOT.log("Loading lobby " + lobby.getName());
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
      
        team1.createTeamArmor();
        team2.createTeamArmor();

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

        // choose the game arena
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

        scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.createScoreboard();

        missileEquipment = new MissileGameEquipment(this);
        specialEquipment = new SpecialGameEquipment(this);

        Logger.DEBUG.log("Making game ready");
        ++fights;
        if (fights >= Config.getFightRestart())
            restart = true;

        FightStats.checkTables();
        Logger.DEBUG.log("Fights: " + fights);

        createGameItems();
    }

    public Scoreboard getScoreboard() {
        return scoreboardManager.board;
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
        for (Player player : gameWorld.getWorld().getPlayers()) {

            Logger.DEBUG.log("Stopping for: " + player.getName());
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(arena.getSpectatorSpawn());
            player.setHealth(player.getMaxHealth());

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

        // Change MOTD
        if (!Config.isMultipleLobbies()) {
            MotdManager.getInstance().updateMOTD(this);
        }

        if (getArena().isSaveStatistics()) {
            FightStats stats = new FightStats(this);
            stats.insert();
        }

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

    public void disableGameOnServerStop() {
        sendPlayerToFallbackSpawn();
        gameWorld.unload();
    }

    private void sendPlayerToFallbackSpawn() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isIn(player.getLocation())) continue;

            player.teleport(Config.getFallbackSpawn());

            player.getInventory().clear();
            player.setFoodLevel(20);
            player.setHealth(player.getMaxHealth());
        }
    }

    public void resetGame() {
        if (state == GameState.INGAME) stopGame();

        HandlerList.unregisterAll(listener);

        stopTimer();

        applyForAllPlayers(player -> player.teleport(lobby.getAfterGameSpawn()));

        if (gameWorld != null) {
            gameWorld.sendPlayersBack();
            gameWorld.unload();
            gameWorld.delete();
        }

        if (scoreboardManager != null) {
            scoreboardManager.removeScoreboard();
        }

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
        if (world == null) return false;

        if (gameWorld != null) {
            return gameWorld.isWorld(world);
        }
        return false;
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

        sendGameItems(player, false);
        setPlayerAttributes(player);

        playerTasks.put(player.getUniqueId(),
                Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), mwPlayer, 40, 20));

    }

    /**
     * This method is used to create the game items for the player kit.
     */
    private void createGameItems() {

        // Will it be used ?
        if (this.getArena().getSpawn().isSendBow() || this.getArena().getRespawn().isSendBow()) {

            ItemStack bow = new ItemStack(Material.BOW);
            bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
            bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
            bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
            ItemMeta im = bow.getItemMeta();
            im.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
            bow.setItemMeta(im);
            VersionUtil.setUnbreakable(bow);
            this.customBow = bow;
        }

        // Will it be used ?
        if (this.getArena().getSpawn().isSendPickaxe() || this.getArena().getRespawn().isSendPickaxe()) {

            ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
            VersionUtil.setUnbreakable(pickaxe);
            this.customPickaxe = pickaxe;
        }

    }

    /**
     * This method gives the player the starter item set, based on the config.yml
     * configuration for spawn and respawn.
     *
     * @param player the target player
     * @param isRespawn true, if the player should receive it after a respawn
     */
    public void sendGameItems(Player player, boolean isRespawn) {

        // clear inventory
        player.getInventory().clear();

        // send armor
        ItemStack[] armor = getPlayer(player).getTeam().getTeamArmor();
        player.getInventory().setArmorContents(armor);

        // send kit items
        if (isRespawn) {

            if (this.getArena().getRespawn().isSendBow()) {
                player.getInventory().addItem(this.customBow);
            }

            if (this.getArena().getRespawn().isSendPickaxe()) {
                player.getInventory().addItem(this.customPickaxe);
            }

        } else {

            if (this.getArena().getSpawn().isSendBow()) {
                player.getInventory().addItem(this.customBow);
            }

            if (this.getArena().getSpawn().isSendPickaxe()) {
                player.getInventory().addItem(this.customPickaxe);
            }

        }

    }

    /**
     * This method sets the player attributes (game mode, level, enchantments, ...).
     *
     * @param player the target player
     */
    public void setPlayerAttributes(Player player) {

        player.setGameMode(GameMode.SURVIVAL);
        player.setLevel(0);
        player.setFireTicks(0);

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

    /**
     * This method manages the message output of the game result.
     * Each player who is currently in the arena world gets a
     * customized message.
     */
    public void sendGameResult() {

        for (Player player : gameWorld.getWorld().getPlayers()) {
            MWPlayer missileWarsPlayer = getPlayer(player);

            // team member of team 1
            if (team1.isMember(missileWarsPlayer)) {
                team1.sendMoney(missileWarsPlayer);
                team1.sendGameResultTitle(missileWarsPlayer);
                team1.sendGameResultSound(missileWarsPlayer);
                continue;
            }

            // team member of team 2
            if (team2.isMember(missileWarsPlayer)) {
                team2.sendMoney(missileWarsPlayer);
                team2.sendGameResultTitle(missileWarsPlayer);
                team2.sendGameResultSound(missileWarsPlayer);
                continue;
            }

            // spectator
            if (player.isOnline()) {
                sendNeutralGameResultTitle(player);
            }

        }
    }


    /**
     * This method sends the players the title / subtitle of the
     * game result there are not in a team (= spectator).
     */
    public void sendNeutralGameResultTitle(Player player) {
        String title;
        String subTitle;

        if (team1.getGameResult() == GameResult.WIN) {
            title = MessageConfig.getNativeMessage("game_result.title_won").replace("%team%", team1.getName());
            subTitle = MessageConfig.getNativeMessage("game_result.subtitle_won");

        } else if (team2.getGameResult() == GameResult.WIN) {
            title = MessageConfig.getNativeMessage("game_result.title_won").replace("%team%", team2.getName());
            subTitle = MessageConfig.getNativeMessage("game_result.subtitle_won");

        } else {
            title = MessageConfig.getNativeMessage("game_result.title_draw");
            subTitle = MessageConfig.getNativeMessage("game_result.subtitle_draw");

        }

        VersionUtil.sendTitle(player, title, subTitle);
    }


    /**
     * This method removes players from the game. Besides former
     * team members, it also affects spectators.
     */
    public void removePlayer(MWPlayer mwPlayer) {
        if (mwPlayer.getTeam() != null) mwPlayer.getTeam().removeMember(mwPlayer);
        players.remove(mwPlayer);
    }
}
