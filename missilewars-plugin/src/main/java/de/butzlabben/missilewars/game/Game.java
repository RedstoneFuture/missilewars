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

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.Lobby;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.event.GameStartEvent;
import de.butzlabben.missilewars.event.GameStopEvent;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.GameState;
import de.butzlabben.missilewars.game.enums.MapChooseProcedure;
import de.butzlabben.missilewars.game.enums.VoteState;
import de.butzlabben.missilewars.game.equipment.EquipmentManager;
import de.butzlabben.missilewars.game.equipment.PlayerEquipmentRandomizer;
import de.butzlabben.missilewars.game.misc.MotdManager;
import de.butzlabben.missilewars.game.misc.ScoreboardManager;
import de.butzlabben.missilewars.game.missile.Missile;
import de.butzlabben.missilewars.game.missile.MissileFacing;
import de.butzlabben.missilewars.game.signs.MWSign;
import de.butzlabben.missilewars.game.stats.FightStats;
import de.butzlabben.missilewars.game.timer.EndTimer;
import de.butzlabben.missilewars.game.timer.GameTimer;
import de.butzlabben.missilewars.game.timer.LobbyTimer;
import de.butzlabben.missilewars.game.timer.TaskManager;
import de.butzlabben.missilewars.inventory.OrcItem;
import de.butzlabben.missilewars.listener.game.EndListener;
import de.butzlabben.missilewars.listener.game.GameBoundListener;
import de.butzlabben.missilewars.listener.game.GameListener;
import de.butzlabben.missilewars.listener.game.LobbyListener;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.PlayerDataProvider;
import de.butzlabben.missilewars.util.geometry.GameArea;
import de.butzlabben.missilewars.util.geometry.Geometry;
import de.butzlabben.missilewars.util.serialization.Serializer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

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
    private final MapVoting mapVoting = new MapVoting(this);
    private final Lobby lobby;
    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();
    private GameState state = GameState.LOBBY;
    private Team team1;
    private Team team2;
    private boolean ready = false;
    private boolean restart = false;
    private GameWorld gameWorld;
    private GameArea gameArea;
    private GameArea innerGameArea;
    private long timestart;
    private Arena arena;
    private ScoreboardManager scoreboardManager;
    private GameBoundListener listener;
    private EquipmentManager equipmentManager;
    private TaskManager taskManager;
    private int remainingGameDuration;

    public Game(Lobby lobby) {
        Logger.BOOT.log("Loading lobby \"" + lobby.getName() + "\".");
        this.lobby = lobby;

        if (lobby.getBukkitWorld() == null) {
            Logger.ERROR.log("Lobby world \"" + lobby.getName() + "\" must not be null");
            return;
        }

        try {
            Serializer.setWorldAtAllLocations(lobby, lobby.getBukkitWorld());
        } catch (Exception exception) {
            Logger.ERROR.log("Could not inject world object at lobby \"" + lobby.getName() + "\".");
            exception.printStackTrace();
            return;
        }

        if (lobby.getPossibleArenas().isEmpty()) {
            Logger.ERROR.log("At least one valid arena must be set at lobby \"" + lobby.getName() + "\".");
            return;
        }

        if (lobby.getPossibleArenas().stream().noneMatch(Arenas::existsArena)) {
            Logger.ERROR.log("None of the specified arenas match a real arena for the lobby \"" + lobby.getName() + "\".");
            return;
        }

        team1 = new Team(lobby.getTeam1Name(), lobby.getTeam1Color(), this);
        team2 = new Team(lobby.getTeam2Name(), lobby.getTeam2Color(), this);

        team1.createTeamArmor();
        team2.createTeamArmor();

        Logger.DEBUG.log("Registering, teleporting, etc. all players");

        updateMOTD();

        Logger.DEBUG.log("Start timer");

        taskManager = new TaskManager(this);
        taskManager.stopTimer();
        updateGameListener(new LobbyListener(this));
        taskManager.setTimer(new LobbyTimer(this, lobby.getLobbyTime()));
        taskManager.runTimer(0, 20);
        state = GameState.LOBBY;

        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> applyForAllPlayers(this::runTeleportEventForPlayer), 2);

        if (Config.isSetup()) {
            Logger.WARN.log("Did not fully initialize lobby \"" + lobby.getName() + "\" as the plugin is in setup mode");
            return;
        }

        scoreboardManager = new ScoreboardManager(this);

        // choose the game arena
        if (lobby.getMapChooseProcedure() == MapChooseProcedure.FIRST) {
            setArena(lobby.getArenas().get(0));
            prepareGame();

        } else if (lobby.getMapChooseProcedure() == MapChooseProcedure.MAPCYCLE) {
            final int lastMapIndex = cycles.getOrDefault(lobby.getName(), -1);
            List<Arena> arenas = lobby.getArenas();
            int index = (lastMapIndex + 1) % arenas.size();
            cycles.put(lobby.getName(), index);
            setArena(arenas.get(index));
            prepareGame();

        } else if (lobby.getMapChooseProcedure() == MapChooseProcedure.MAPVOTING) {
            if (mapVoting.onlyOneArenaFound()) {
                setArena(lobby.getArenas().get(0));
                Logger.WARN.log("Only one arena was found for the lobby \"" + lobby.getName() + "\". The configured map voting was skipped.");
                prepareGame();
            } else {
                mapVoting.startVote();
                scoreboardManager.resetScoreboard();
            }
        }

        // Add players if they are in the game area
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isIn(player.getLocation())) {
                continue;
            }

            playerJoinInGame(player, false);
        }
    }

    /**
     * This method performs the final preparations for the game start.
     * <p>
     * It is necessary that the arena - even in the case of a map vote - is
     * now already defined.
     */
    public void prepareGame() {
        if (this.arena == null) {
            throw new IllegalStateException("The arena is not yet set");
        }

        // Clear the player inventory
        applyForAllPlayers(player -> player.getInventory().setItem(4, new ItemStack(Material.AIR)));

        scoreboardManager.resetScoreboard();

        equipmentManager = new EquipmentManager(this);
        equipmentManager.createGameItems();

        Logger.DEBUG.log("Making game ready");
        ++fights;
        checkFightRestart();

        FightStats.checkTables();
        Logger.DEBUG.log("Fights: " + fights);

        ready = true;
    }

    private void checkFightRestart() {
        if (Config.getFightRestart() <= 0) return;

        if (fights >= Config.getFightRestart()) restart = true;
    }

    private void updateGameListener(GameBoundListener newListener) {
        if (listener != null) HandlerList.unregisterAll(listener);

        Bukkit.getPluginManager().registerEvents(newListener, MissileWars.getInstance());
        this.listener = newListener;
    }

    private void updateMOTD() {
        if (!Config.isMultipleLobbies()) {
            MotdManager.getInstance().updateMOTD(this);
        }
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

        taskManager.stopTimer();
        updateGameListener(new GameListener(this));
        taskManager.setTimer(new GameTimer(this));
        taskManager.runTimer(5, 20);
        state = GameState.INGAME;

        timestart = System.currentTimeMillis();

        applyForAllPlayers(this::startForPlayer);

        updateMOTD();

        Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
    }

    public void stopGame() {
        if (Config.isSetup()) return;

        Logger.DEBUG.log("Stopping");

        for (BukkitTask bt : playerTasks.values()) {
            bt.cancel();
        }

        Logger.DEBUG.log("Stopping for players");
        for (Player player : gameWorld.getWorld().getPlayers()) {

            Logger.DEBUG.log("Stopping for: " + player.getName());
            player.setGameMode(GameMode.SPECTATOR);
            teleportToArenaSpectatorSpawn(player);

        }

        // Save the remaining game duration.
        remainingGameDuration = taskManager.getTimer().getSeconds();

        taskManager.stopTimer();
        updateGameListener(new EndListener(this));
        taskManager.setTimer(new EndTimer(this));
        taskManager.runTimer(5, 20);
        state = GameState.END;

        updateMOTD();

        if (arena.isSaveStatistics()) {
            FightStats stats = new FightStats(this);
            stats.insert();
        }

        Logger.DEBUG.log("Stopped completely");
        Bukkit.getPluginManager().callEvent(new GameStopEvent(this));
    }

    public void triggerRestart() {
        if (Config.isSetup()) return;

        if (restart) {
            Bukkit.getServer().spigot().restart();
            return;
        }

        GameManager.getInstance().restartGame(lobby, false);
    }

    public void appendRestart() {
        restart = true;
    }

    public void disableGameOnServerStop() {

        for (MWPlayer mwPlayer : players.values()) {
            teleportToFallbackSpawn(mwPlayer.getPlayer());
        }

        gameWorld.unload();
    }

    /**
     * This method adds the player to the game.
     *
     * @param player          the target Player
     * @param isSpectatorJoin should the player join as spectator or as normal player
     */
    public void playerJoinInGame(Player player, boolean isSpectatorJoin) {

        PlayerDataProvider.getInstance().storeInventory(player);
        MWPlayer mwPlayer = addPlayer(player);

        if (state == GameState.LOBBY) {
            assert !isSpectatorJoin : "wrong syntax";

            teleportToLobbySpawn(player);
            player.setGameMode(GameMode.ADVENTURE);
        }

        if (isSpectatorJoin) {
            Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> teleportToArenaSpectatorSpawn(player), 2);
            Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> player.setGameMode(GameMode.SPECTATOR), 35);

            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.ARENA_SPECTATOR));
            player.setDisplayName("§7" + player.getName() + "§r");

        } else {
            player.getInventory().clear();
            player.setFoodLevel(20);
            player.setHealth(player.getMaxHealth());

            Team team = getSmallerTeam();
            team.addMember(mwPlayer);
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.TEAM_TEAM_ASSIGNED).replace("%team%", team.getFullname()));

            String message = null;
            if (state == GameState.LOBBY) {
                message = Messages.getMessage(true, Messages.MessageEnum.LOBBY_PLAYER_JOINED);
            } else if (state == GameState.INGAME) {
                message = Messages.getMessage(true, Messages.MessageEnum.GAME_PLAYER_JOINED);
            }

            if (message != null) {
                broadcast(message.replace("%max_players%", Integer.toString(lobby.getMaxSize()))
                        .replace("%players%", Integer.toString(players.values().size()))
                        .replace("%player%", player.getName())
                        .replace("%team%", team.getFullname()));
            }

        }

        player.setScoreboard(scoreboardManager.getBoard());

        if (state == GameState.LOBBY) {

            // team change menu:
            if (player.hasPermission("mw.change")) {
                player.getInventory().setItem(0, team1.getGlassPlane());
                player.getInventory().setItem(8, team2.getGlassPlane());
            }

            // map choose menu:
            if (mapVoting.getState() == VoteState.RUNNING) {
                if (player.hasPermission("mw.vote")) {
                    player.getInventory().setItem(4, new OrcItem(Material.NETHER_STAR, "§3Vote Map").getItemStack());
                }
            }

        } else if ((state == GameState.INGAME) && (!isSpectatorJoin)) {
            startForPlayer(player);
        }
    }

    /**
     * This method handles the removal of the player from the game.
     *
     * @param mwPlayer the target missilewars player
     */
    public void playerLeaveFromGame(MWPlayer mwPlayer) {
        Player player = mwPlayer.getPlayer();
        Team team = mwPlayer.getTeam();
        boolean playerWasTeamMember = false;

        BukkitTask task = playerTasks.get(mwPlayer.getUuid());
        if (task != null) task.cancel();

        PlayerDataProvider.getInstance().loadInventory(player);

        if (team != null) {
            playerWasTeamMember = true;
            team.removeMember(mwPlayer);
            if (state == GameState.INGAME) checkTeamSize(team);
        }

        removePlayer(mwPlayer);

        if (playerWasTeamMember) {

            String message = null;
            if (state == GameState.LOBBY) {
                message = Messages.getMessage(true, Messages.MessageEnum.LOBBY_PLAYER_LEFT);
            } else if (state == GameState.INGAME) {
                message = Messages.getMessage(true, Messages.MessageEnum.GAME_PLAYER_LEFT);
            }

            if (message != null) {
                broadcast(message.replace("%max_players%", Integer.toString(lobby.getMaxSize()))
                        .replace("%players%", Integer.toString(players.values().size()))
                        .replace("%player%", player.getName())
                        .replace("%team%", team.getFullname()));
            }

        }

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        if (state == GameState.LOBBY) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.LOBBY_LEFT).replace("%lobby_name%", lobby.getDisplayName()));
        } else if (state == GameState.INGAME) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.GAME_LEFT).replace("%arena_name%", arena.getDisplayName()));
        }

    }

    /**
     * This method executes the PlayerTeleportEvent to run the basic game join process
     * after the game is restarted.
     *
     * @param player target player
     */
    private void runTeleportEventForPlayer(Player player) {
        Bukkit.getPluginManager().callEvent(new PlayerTeleportEvent(player,
                Config.getFallbackSpawn(), lobby.getSpawnPoint()));
    }

    private void checkTeamSize(Team team) {
        int teamSize = team.getMembers().size();
        if (teamSize == 0) {
            Bukkit.getScheduler().runTask(MissileWars.getInstance(), () -> {
                team.getEnemyTeam().setGameResult(GameResult.WIN);
                team.setGameResult(GameResult.LOSE);
                sendGameResult();
                stopGame();
            });
            broadcast(Messages.getMessage(true, Messages.MessageEnum.TEAM_ALL_TEAMMATES_OFFLINE).replace("%team%", team.getFullname()));
        }
    }

    public void resetGame() {
        // Teleporting players; the event listener will handle the teleport event
        applyForAllPlayers(this::teleportToAfterGameSpawn);

        // Deactivation of all event handlers
        HandlerList.unregisterAll(listener);
        taskManager.stopTimer();

        // Just in case this wasn't executed in stopGame() already
        // This can happen if a game restart gets issued while it's still active
        for (BukkitTask bt : playerTasks.values()) {
            bt.cancel();
        }

        if (gameWorld != null) {
            gameWorld.unload();
            gameWorld.delete();
        }

        if (scoreboardManager != null) {
            scoreboardManager.removeScoreboard();
        }
    }

    /**
     * This method checks if the location is inside in the Lobby-Area.
     *
     * @param location (Location) the location to be checked
     *
     * @return true, if it's in the Lobby-Area
     */
    public boolean isInLobbyArea(Location location) {
        return Geometry.isInsideIn(location, lobby.getArea());
    }

    /**
     * This method checks if the location is inside in the Game-Area.
     *
     * @param location (Location) the location to be checked
     *
     * @return true, if it's in the Game-Area
     */
    public boolean isInGameArea(Location location) {
        return Geometry.isInsideIn(location, gameArea);
    }

    /**
     * This method checks if the location is inside in the Inner Game-Area.
     * It's the arena from the Team 1 spawn position to the Team 2 spawn
     * position ("length") with the same "width" of the (major) Game-Area.
     *
     * @param location (Location) the location to be checked
     *
     * @return true, if it's in the Inner Game-Area
     */
    public boolean isInInnerGameArea(Location location) {
        return Geometry.isInsideIn(location, innerGameArea);
    }

    /**
     * This method checks if the location is in the game world.
     *
     * @param location (Location) the location to be checked
     *
     * @return true, if it's in the game world
     */
    public boolean isInGameWorld(Location location) {
        // Is possible during the map voting phase:
        if (gameArea == null) return false;

        return Geometry.isInWorld(location, gameArea.getWorld());
    }

    /**
     * This (shortcut) method checks if the location is inside in the
     * Lobby-Area or inside in the game world.
     *
     * @param location (Location) the location to be checked
     *
     * @return true, if the statement is correct
     */
    public boolean isIn(Location location) {
        return isInLobbyArea(location) || isInGameWorld(location);
    }

    private MWPlayer addPlayer(Player player) {
        if (players.containsKey(player.getUniqueId())) return players.get(player.getUniqueId());
        MWPlayer mwPlayer = new MWPlayer(player, this);
        players.put(player.getUniqueId(), mwPlayer);
        return mwPlayer;
    }

    public MWPlayer getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    /**
     * This method finally removes the player from the game player array. Besides former
     * team members, it also affects spectators.
     */
    private void removePlayer(MWPlayer mwPlayer) {
        players.remove(mwPlayer.getUuid());
    }

    public void broadcast(String message) {
        for (MWPlayer mwPlayer : players.values()) {
            Player player = mwPlayer.getPlayer();
            if (player != null && player.isOnline()) player.sendMessage(message);
        }
    }

    public void startForPlayer(Player player) {
        MWPlayer mwPlayer = getPlayer(player);
        if (mwPlayer == null) {
            Logger.ERROR.log("Error starting game at player " + player.getName());
            return;
        }

        player.teleport(mwPlayer.getTeam().getSpawn());

        equipmentManager.sendGameItems(player, false);
        setPlayerAttributes(player);

        mwPlayer.setRandomGameEquipment(new PlayerEquipmentRandomizer(mwPlayer, this));

        playerTasks.put(player.getUniqueId(),
                Bukkit.getScheduler().runTaskTimer(MissileWars.getInstance(), mwPlayer, 40, 20));
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

    /**
     * This method respawns the player after short time.
     *
     * @param player the target player
     */
    public void autoRespawnPlayer(Player player) {
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> player.spigot().respawn(), 20L);
    }

    /**
     * This method spawns the missile for the player.
     *
     * @param player the executing player
     */
    public void spawnMissile(Player player, ItemStack itemStack) {

        // Are missiles only allowed to spawn inside the arena, between the two arena spawn points?
        boolean isOnlyBetweenSpawnPlaceable = this.arena.getMissileConfiguration().isOnlyBetweenSpawnPlaceable();
        if (isOnlyBetweenSpawnPlaceable) {
            if (!isInInnerGameArea(player.getLocation())) {
                player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.ARENA_MISSILE_PLACE_DENY));
                return;
            }
        }

        Missile missile = this.arena.getMissileConfiguration().getMissileFromName(itemStack.getItemMeta().getDisplayName());
        if (missile == null) {
            player.sendMessage(Messages.getMessage(true, Messages.MessageEnum.COMMAND_INVALID_MISSILE));
            return;
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
        player.setItemInHand(itemStack);
        missile.paste(player, MissileFacing.getFacingPlayer(player, this.arena.getMissileConfiguration()), this);
    }

    /**
     * This method spawns the fireball for the player.
     *
     * @param player the executing player
     */
    public void spawnFireball(Player player, ItemStack itemStack) {
        int amount = itemStack.getAmount();
        itemStack.setAmount(amount - 1);

        Fireball fb = player.launchProjectile(Fireball.class);
        fb.setDirection(player.getLocation().getDirection().multiply(2.5D));
        player.playSound(fb.getLocation(), Sound.BLOCK_ANVIL_LAND, 100.0F, 2.0F);
        player.playSound(fb.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 100.0F, 1.0F);
        fb.setYield(3F);
        fb.setIsIncendiary(true);
        fb.setBounce(false);
    }

    public void setArena(Arena arena) {
        if (this.arena != null) {
            throw new IllegalStateException("Arena already set");
        }

        arena.getMissileConfiguration().check();
        if (arena.getMissileConfiguration().getMissiles().isEmpty()) {
            throw new IllegalStateException("The game cannot be started, when 0 missiles are configured");
        }

        this.arena = arena.clone();
        gameWorld = new GameWorld(this, arena.getTemplateWorld());
        gameWorld.load();
        gameArea = new GameArea(gameWorld.getWorld(), arena.getAreaConfig());

        try {
            Serializer.setWorldAtAllLocations(this.arena, gameWorld.getWorld());
            team1.setSpawn(this.arena.getTeam1Spawn());
            team2.setSpawn(this.arena.getTeam2Spawn());
        } catch (Exception exception) {
            Logger.ERROR.log("Could not inject world object at arena " + this.arena.getName());
            exception.printStackTrace();
            return;
        }

        createInnerGameArea();
    }

    private void createInnerGameArea() {

        // Depending on the rotation of the (major) Game-Area, the spawn points 
        // of both teams are primarily on the X or Z axis opposite each other.
        // The Inner Game-Area is a copy of the (major) Game-Area, with the X or Z 
        // axis going only to spawn. The X or Z distance is thus reduced.
        // So this algorithm allows the spawn points to face each other even if 
        // they are offset.

        int x1, x2, z1, z2;
        Location position1, position2;

        if (gameArea.getDirection() == GameArea.Direction.NORTH_SOUTH) {

            x1 = gameArea.getMinX();
            x2 = gameArea.getMaxX();

            z1 = team1.getSpawn().getBlockZ();
            z2 = team2.getSpawn().getBlockZ();

        } else {

            z1 = gameArea.getMinZ();
            z2 = gameArea.getMaxZ();

            x1 = team1.getSpawn().getBlockX();
            x2 = team2.getSpawn().getBlockX();

        }

        position1 = new Location(gameArea.getWorld(), x1, gameArea.getMinY(), z1);
        position2 = new Location(gameArea.getWorld(), x2, gameArea.getMaxY(), z2);

        innerGameArea = new GameArea(position1, position2);
    }

    public void applyForAllPlayers(Consumer<Player> consumer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isIn(player.getLocation())) continue;
            consumer.accept(player);
        }
    }

    /**
     * This method manages the message output of the game result.
     * Each player who is currently in the arena world gets a
     * customized message.
     */
    public void sendGameResult() {

        for (Player player : gameWorld.getWorld().getPlayers()) {
            MWPlayer mwPlayer = getPlayer(player);

            // team member of team 1
            if (team1.isMember(mwPlayer)) {
                team1.sendMoney(mwPlayer);
                team1.sendGameResultTitle(mwPlayer);
                team1.sendGameResultSound(mwPlayer);
                continue;
            }

            // team member of team 2
            if (team2.isMember(mwPlayer)) {
                team2.sendMoney(mwPlayer);
                team2.sendGameResultTitle(mwPlayer);
                team2.sendGameResultSound(mwPlayer);
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
            title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_WON).replace("%team%", team1.getName());
            subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_WON);

        } else if (team2.getGameResult() == GameResult.WIN) {
            title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_WON).replace("%team%", team2.getName());
            subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_WON);

        } else {
            title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_DRAW);
            subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_DRAW);

        }

        player.sendTitle(title, subTitle);
    }

    /**
     * This method updates the MissileWars signs and the scoreboard.
     */
    public void updateGameInfo() {
        MissileWars.getInstance().getSignRepository().getSigns(this).forEach(MWSign::update);
        scoreboardManager.resetScoreboard();
        Logger.DEBUG.log("Updated signs and scoreboard.");
    }

    /**
     * This method returns the next matching team for the next player to
     * join. It is always the smaller team.
     *
     * @return (Team) the smaller team
     */
    public Team getSmallerTeam() {
        if (team1.getMembers().size() > team2.getMembers().size()) {
            return team2;
        } else {
            return team1;
        }
    }

    /**
     * This method checks whether a team switch would be fair based on
     * the new team size. If no empty team results or if the team size
     * difference does not exceed a certain value, the switch is
     * considered acceptable.
     *
     * @param targetTeam the new team
     *
     * @return (boolean) 'true' if it's a fair team switch
     */
    public boolean isValidTeamSwitch(Team targetTeam) {

        // original team sizes
        int targetTeamSize = targetTeam.getMembers().size();
        int currentTeamSize = targetTeam.getEnemyTeam().getMembers().size();

        // Preventing an empty team when previously both teams had at least one player:
        if ((currentTeamSize == 1) && (targetTeamSize >= 1)) return false;

        int diff = getSmallerTeam().getEnemyTeam().getMembers().size() - getSmallerTeam().getMembers().size();

        // max team difference: 30% (rounded) of target team size
        float maxDiff = Math.max(1, Math.round(targetTeamSize * 0.3));

        return diff <= maxDiff;
    }

    public boolean isPlayersMax() {
        int maxSize = lobby.getMaxSize();
        int currentSize = team1.getMembers().size() + team2.getMembers().size();
        return currentSize >= maxSize;
    }

    public boolean isSpectatorsMax() {
        int maxSize = arena.getMaxSpectators();

        if (maxSize == -1) return false;

        int currentSize = players.size() - (team1.getMembers().size() + team2.getMembers().size());
        return currentSize >= maxSize;
    }

    public static void knockbackEffect(Player player, Location from, Location to) {
        Vector addTo = from.toVector().subtract(to.toVector()).multiply(3);
        addTo.setY(0);
        player.teleport(from.add(addTo));
    }

    public void teleportToFallbackSpawn(Player player) {
        player.teleport(Config.getFallbackSpawn());
    }

    public void teleportToLobbySpawn(Player player) {
        player.teleport(lobby.getSpawnPoint());
    }

    public void teleportToArenaSpectatorSpawn(Player player) {
        player.teleport(arena.getSpectatorSpawn());
    }

    public void teleportToAfterGameSpawn(Player player) {
        player.teleport(lobby.getAfterGameSpawn());
    }
}
