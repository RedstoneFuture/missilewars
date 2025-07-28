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

package de.butzlabben.missilewars.configuration;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.initialization.ConfigLoader;
import de.butzlabben.missilewars.initialization.FileManager;
import de.butzlabben.missilewars.menus.MenuItem;
import de.butzlabben.missilewars.util.MaterialUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

import static org.bukkit.Material.*;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */
public class Config {
    
    @Getter private static final File FILE = new File(MissileWars.getInstance().getDataFolder(), "config.yml");
    @Setter private static YamlConfiguration cfg;
    
    private final static boolean isNewConfig = !FILE.exists();

    public static void load() {

        cfg = ConfigLoader.loadConfigFile(FILE);
        
        // Validate the settings and re-save the cleaned config-file.
        addDefaults();
        
        save();
    }
    
    public static void save() {
        FileManager.safeFile(FILE, cfg);
        cfg = ConfigLoader.getLoadedConfig(FILE);
    }
    
    private static void addDefaults() {
        cfg.addDefault("debug", false);
        if (debug()) {
            Logger.DEBUG.log("Debug enabled");
        }

        cfg.addDefault("setup_mode", false);
        
        cfg.addDefault("antispam_intervall.team_change_command", 25);

        cfg.addDefault("contact_auth_server", true);
        cfg.addDefault("prefetch_players", true);

        cfg.addDefault("restart_after_fights", -1);

        cfg.addDefault("arenas.folder", "plugins/MissileWars/arenas");

        cfg.addDefault("games.multiple_games", false);
        cfg.addDefault("games.folder", "plugins/MissileWars/games");
        cfg.addDefault("games.default_game", "game1.yml");

        cfg.addDefault("missiles.folder", "plugins/MissileWars/schematics/missiles");
        cfg.addDefault("shields.folder", "plugins/MissileWars/schematics/shields");

        cfg.addDefault("temp_block.enable", true);
        cfg.addDefault("temp_block.material", NOTE_BLOCK.name());
        cfg.addDefault("temp_block.remove_after_ticks", 0);
        cfg.addDefault("temp_block.radius", 8);
        
        cfg.addDefault("game_result.firework", true);

        cfg.addDefault("motd.enable", true);
        cfg.addDefault("motd.lobby", "&6•&e● MissileWars &7| &eLobby");
        cfg.addDefault("motd.ingame", "&6•&e● MissileWars &7| &bIngame");
        cfg.addDefault("motd.ended", "&6•&e● MissileWars &7| &cRestarting...");

        cfg.addDefault("fightstats.enable", false);
        cfg.addDefault("fightstats.show_real_skins", true);

        Location worldSpawnLoc = Bukkit.getWorlds().get(0).getSpawnLocation();
        cfg.addDefault("fallback_spawn.world", worldSpawnLoc.getWorld().getName());
        cfg.addDefault("fallback_spawn.x", worldSpawnLoc.getX());
        cfg.addDefault("fallback_spawn.y", worldSpawnLoc.getY());
        cfg.addDefault("fallback_spawn.z", worldSpawnLoc.getZ());
        cfg.addDefault("fallback_spawn.yaw", worldSpawnLoc.getYaw());
        cfg.addDefault("fallback_spawn.pitch", worldSpawnLoc.getPitch());

        cfg.addDefault("mysql.host", "localhost");
        cfg.addDefault("mysql.database", "db");
        cfg.addDefault("mysql.port", "3306");
        cfg.addDefault("mysql.user", "root");
        cfg.addDefault("mysql.password", "");
        cfg.addDefault("mysql.fights_table", "mw_fights");
        cfg.addDefault("mysql.fightmember_table", "mw_fightmember");

        cfg.addDefault("sidebar.title", "&eInfo ●&6•");
        cfg.addDefault("sidebar.member_list_style", "%team_color%%playername%");
        cfg.addDefault("sidebar.member_list_max", 4);

        if (isNewConfig) {
            
            cfg.set("sidebar.entries", new ArrayList<String>() {{
                add("&7Time left:");
                add("&e» %time%m");
                add("");
                add("%team1% &7» %team1_color%%team1_amount%");
                add("");
                add("%team2% &7» %team2_color%%team2_amount%");
            }});
            
        }
        
        cfg.addDefault("actionbar_msg.spectator.delay", 6);
        
        if (isNewConfig) {
            
            cfg.set("actionbar_msg.spectator.messages", new ArrayList<String>() {{
                add("&eChoose your team to join: &7/mw teammenu");
            }});
            
        }

        String gameJoinMenu = "menus.hotbar_menu.game_join_menu";
        
        if (isNewConfig) {
            
            // team-selection menu link:
            
            cfg.addDefault(gameJoinMenu + ".items.team_selection.display_name", "&eTeam Selection");
            cfg.addDefault(gameJoinMenu + ".items.team_selection.material", "{player-team-item}");
            cfg.addDefault(gameJoinMenu + ".items.team_selection.slot", 2);
            
            cfg.addDefault(gameJoinMenu + ".items.team_selection.priority", 0);

            cfg.set(gameJoinMenu + ".items.team_selection.lore", new ArrayList<String>() {{
                add("&2Right click to open the");
                add("&2team selection menu!");
            }});
            
            cfg.set(gameJoinMenu + ".items.team_selection.left_click_actions", new ArrayList<String>());
            cfg.set(gameJoinMenu + ".items.team_selection.right_click_actions", new ArrayList<String>() {{
                add("[player-cmd] mw teammenu");
            }});
            
            
            // map-voting menu link (A: Map-Vote active):
            
            cfg.addDefault(gameJoinMenu + ".items.mapVote_active.display_name", "&eMap Voting");
            cfg.addDefault(gameJoinMenu + ".items.mapVote_active.material", "basehead-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMyNzEwNTI3MTllZjY0MDc5ZWU4YzE0OTg5NTEyMzhhNzRkYWM0YzI3Yjk1NjQwZGI2ZmJkZGMyZDZiNWI2ZSJ9fX0=");
            cfg.addDefault(gameJoinMenu + ".items.mapVote_active.slot", 4);

            cfg.addDefault(gameJoinMenu + ".items.mapVote_active.priority", 1);
            cfg.addDefault(gameJoinMenu + ".items.mapVote_active.view_requirement.type", "string equals");
            cfg.addDefault(gameJoinMenu + ".items.mapVote_active.view_requirement.input", "%missilewars_game_mapvote_state_this%");
            cfg.addDefault(gameJoinMenu + ".items.mapVote_active.view_requirement.output", "RUNNING");
            
            cfg.set(gameJoinMenu + ".items.mapVote_active.lore", new ArrayList<String>() {{
                add("&2Right click to open the");
                add("&2map vote menu!");
            }});
            
            cfg.set(gameJoinMenu + ".items.mapVote_active.left_click_actions", new ArrayList<String>());
            cfg.set(gameJoinMenu + ".items.mapVote_active.right_click_actions", new ArrayList<String>() {{
                add("[player-cmd] mw mapmenu");
            }});
            
            
            // map-voting menu link (B: Map-Vote inactive):
            
            cfg.addDefault(gameJoinMenu + ".items.mapVote_inactive.display_name", "&2Map Voting");
            cfg.addDefault(gameJoinMenu + ".items.mapVote_inactive.material", "basehead-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjFkZDRmZTRhNDI5YWJkNjY1ZGZkYjNlMjEzMjFkNmVmYTZhNmI1ZTdiOTU2ZGI5YzVkNTljOWVmYWIyNSJ9fX0=");
            cfg.addDefault(gameJoinMenu + ".items.mapVote_inactive.slot", 4);

            cfg.addDefault(gameJoinMenu + ".items.mapVote_inactive.priority", 0);
            
            cfg.set(gameJoinMenu + ".items.mapVote_inactive.lore", new ArrayList<String>() {{
                add("&2Voted-Map: &7%missilewars_arena_displayname_this%");
            }});
            
            cfg.set(gameJoinMenu + ".items.mapVote_inactive.left_click_actions", new ArrayList<String>());
            cfg.set(gameJoinMenu + ".items.mapVote_inactive.right_click_actions", new ArrayList<String>() {{
                add("[player-cmd] mw mapmenu");
            }});
            
            
            // area info item:
            
            cfg.addDefault(gameJoinMenu + ".items.areaInfo.display_name", "&eArena Info");
            cfg.addDefault(gameJoinMenu + ".items.areaInfo.material", "basehead-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjhlYTU3Yzc1NTFjNmFiMzNiOGZlZDM1NGI0M2RmNTIzZjFlMzU3YzRiNGY1NTExNDNjMzRkZGVhYzViNmM4ZCJ9fX0=");
            cfg.addDefault(gameJoinMenu + ".items.areaInfo.slot", 6);
            
            cfg.addDefault(gameJoinMenu + ".items.areaInfo.priority", 0);

            cfg.set(gameJoinMenu + ".items.areaInfo.lore", new ArrayList<String>() {{
                add("&e> &fLobby: &7%missilewars_game_displayname_this%");
                add("&e> &fArena: &7%missilewars_arena_displayname_this%");
                add("&e> &fGame-Time: &7%missilewars_game_gameduration_this% min");
                add("&e> &fMissiles: &7%missilewars_arena_missileamount_this%x");
                add("&e> &fArena-Size: &7%missilewars_game_arenasize_X_this% x %missilewars_game_arenasize_Z_this% blocks");
            }});
            
            cfg.set(gameJoinMenu + ".items.areaInfo.left_click_actions", new ArrayList<String>());
            cfg.set(gameJoinMenu + ".items.areaInfo.right_click_actions", new ArrayList<String>());
        }
        
        
        String teamSelectionMenu = "menus.inventory_menu.team_selection_menu";
        cfg.addDefault(teamSelectionMenu + ".title", "&eTeam Selection Menu");
        cfg.addDefault(teamSelectionMenu + ".team_item", "{player-team-name}");
        
        String mapVoteMenu = "menus.inventory_menu.map_vote_menu";
        cfg.addDefault(mapVoteMenu + ".title", "&eMap Vote Menu");
        cfg.addDefault(mapVoteMenu + ".map_item", "&e{arena-name}");
        cfg.addDefault(mapVoteMenu + ".vote_result_bar", "&7{vote-percent}%");
        cfg.addDefault(mapVoteMenu + ".navigation.backwards_item.active", "&eprevious page");
        cfg.addDefault(mapVoteMenu + ".navigation.backwards_item.inactive", "&7previous page");
        cfg.addDefault(mapVoteMenu + ".navigation.forwards_item.active", "&enext page");
        cfg.addDefault(mapVoteMenu + ".navigation.forwards_item.inactive", "&7next page");
    }
    
    public static void setFallbackSpawn(Location spawnLocation) {
        cfg.set("fallback_spawn.world", spawnLocation.getWorld().getName());
        cfg.set("fallback_spawn.x", spawnLocation.getX());
        cfg.set("fallback_spawn.y", spawnLocation.getY());
        cfg.set("fallback_spawn.z", spawnLocation.getZ());
        cfg.set("fallback_spawn.yaw", spawnLocation.getYaw());
        cfg.set("fallback_spawn.pitch", spawnLocation.getPitch());

        // re-save the config with only validated options
        FileManager.safeFile(FILE, cfg);
    }
    
    public static YamlConfiguration getConfig() {
        return cfg;
    }

    public static boolean debug() {
        return cfg.getBoolean("debug");
    }

    public static boolean isSetup() {
        return cfg.getBoolean("setup_mode");
    }
    
    public static int getTeamChangeCmdIntervall() {
        return cfg.getInt("antispam_intervall.team_change_command");
    }

    public static boolean isContactAuth() {
        return cfg.getBoolean("contact_auth_server");
    }

    public static boolean isPrefetchPlayers() {
        return cfg.getBoolean("prefetch_players");
    }

    public static int getFightRestart() {
        return cfg.getInt("restart_after_fights");
    }

    public static String getArenasFolder() {
        return cfg.getString("arenas.folder");
    }

    public static boolean useMultipleGames() {
        return cfg.getBoolean("games.multiple_games");
    }

    public static String getGamesFolder() {
        return cfg.getString("games.folder");
    }

    public static String getDefaultGame() {
        return cfg.getString("games.default_game");
    }

    public static String getMissilesFolder() {
        return cfg.getString("missiles.folder");
    }

    public static String getShieldsFolder() {
        return cfg.getString("shields.folder");
    }
    
    public static boolean isTempBlockEnabled() {
        return cfg.getBoolean("temp_block.enable");
    }
    
    public static Material getTempBlockMaterial() {
        return MaterialUtil.getMaterial(cfg.getString("temp_block.material"));
    }

    public static int getUpdateDelay() {
        return cfg.getInt("temp_block.remove_after_ticks");
    }

    public static int getUpdateRadius() {
        return cfg.getInt("temp_block.radius");
    }
    
    public static boolean isGameResultFirework() {
        return cfg.getBoolean("game_result.firework");
    }

    public static String motdEnded() {
        return PluginMessages.getConvertedMsg(cfg.getString("motd.ended"));
    }

    public static String motdGame() {
        return PluginMessages.getConvertedMsg(cfg.getString("motd.ingame"));
    }

    public static String motdLobby() {
        return PluginMessages.getConvertedMsg(cfg.getString("motd.lobby"));
    }

    public static boolean motdEnabled() {
        return cfg.getBoolean("motd.enable");
    }

    public static boolean isFightStatsEnabled() {
        return cfg.getBoolean("fightstats.enable");
    }

    public static boolean isShowRealSkins() {
        return cfg.getBoolean("fightstats.show_real_skins");
    }
    
    public static Location getFallbackSpawn() {
        ConfigurationSection cfg = Config.cfg.getConfigurationSection("fallback_spawn");
        World world = Bukkit.getWorld(cfg.getString("world"));
        if (world == null) {
            Logger.WARN.log("The world configured at \"fallback_location.world\" couldn't be found. Using the default one");
            world = Bukkit.getWorlds().get(0);
        }
        Location location = new Location(world,
                cfg.getDouble("x"),
                cfg.getDouble("y"),
                cfg.getDouble("z"),
                (float) cfg.getDouble("yaw"),
                (float) cfg.getDouble("pitch"));
        if (GameManager.getInstance().getGame(location) != null) {
            Logger.WARN.log("Your fallback spawn is inside a game area. This plugins functionality can no longer be guaranteed");
        }

        return location;
    }
    
    public static String getHost() {
        return cfg.getString("mysql.host");
    }

    public static String getDatabase() {
        return cfg.getString("mysql.database");
    }

    public static String getPort() {
        return cfg.getString("mysql.port");
    }

    public static String getUser() {
        return cfg.getString("mysql.user");
    }

    public static String getPassword() {
        return cfg.getString("mysql.password");
    }

    public static String getFightsTable() {
        return cfg.getString("mysql.fights_table");
    }

    public static String getFightMembersTable() {
        return cfg.getString("mysql.fightmember_table");
    }

    public static String getScoreboardTitle() {
        return PluginMessages.getConvertedMsg(cfg.getString("sidebar.title"));
    }

    public static String getScoreboardMembersStyle() {
        return PluginMessages.getConvertedMsg(cfg.getString("sidebar.member_list_style"));
    }

    public static int getScoreboardMembersMax() {
        return cfg.getInt("sidebar.member_list_max");
    }
    
    public static List<String> getScoreboardEntries() {
        return PluginMessages.getConvertedMsgList(cfg.getStringList("sidebar.entries"));
    }
    
    public static int getActionbarForSpecDelay() {
        return cfg.getInt("actionbar_msg.spectator.delay");
    }
    
    public static String[] getActionbarForSpecEntries() {
        return PluginMessages.getConvertedMsgArray(cfg.getStringList("actionbar_msg.spectator.messages"));
    }
    
    public static Map<Integer, Map<Integer, MenuItem>> getGameJoinMenuItems() {
        // Config keys inspired by DeluxeMenus https://wiki.helpch.at/helpchat-plugins/deluxemenus/options-and-configurations/item
        
        String gameJoinMenu = "menus.hotbar_menu.game_join_menu";
        Set<String> items = Config.cfg.getConfigurationSection(gameJoinMenu + ".items").getKeys(false);

        Map<Integer, Map<Integer, MenuItem>> menuItems = new HashMap<>();
        
        for (String item : items) {
            ConfigurationSection cfg = Config.cfg.getConfigurationSection(gameJoinMenu + ".items." + item);
            MenuItem menuItem = new MenuItem(cfg.getInt("slot"), cfg.getInt("priority"));

            menuItem.setDisplayName(PluginMessages.getConvertedMsg(cfg.getString("display_name")));
            menuItem.setMaterialName(cfg.getString("material"));
            menuItem.setItemRequirement(cfg);
            menuItem.setLoreList(PluginMessages.getConvertedMsgList(cfg.getStringList("lore")));
            menuItem.setLeftClickActions(new ActionSet(PluginMessages.getConvertedMsgList(cfg.getStringList("left_click_actions"))));
            menuItem.setRightClickActions(new ActionSet(PluginMessages.getConvertedMsgList(cfg.getStringList("right_click_actions"))));
            
            int slot = menuItem.getSlot();
            Map<Integer, MenuItem> itemsInSlot = new HashMap<>();
            
            if (!menuItems.containsKey(slot)) {
                itemsInSlot.put(menuItem.getPriority(), menuItem);
                menuItems.put(slot, itemsInSlot);
            } else {
                itemsInSlot = menuItems.get(slot);
                itemsInSlot.put(menuItem.getPriority(), menuItem);
                menuItems.replace(slot, itemsInSlot);
            }
        }
        
        return menuItems;
    }
    
    public static String getTeamSelectionMenuTitle() {
        return PluginMessages.getConvertedMsg(cfg.getString("menus.inventory_menu.team_selection_menu.title"));
    }
    
    @Getter
    public enum TeamSelectionMenuItems {
        TEAM_ITEM("menus.inventory_menu.team_selection_menu.team_item");
        
        private final String path;

        TeamSelectionMenuItems(String path) {
            this.path = path;
        }
        
        public String getMessage() {
            return PluginMessages.getConvertedMsg(cfg.getString(getPath()));
        }
    }
    
    public static String getMapVoteMenuTitle() {
        return PluginMessages.getConvertedMsg(cfg.getString("menus.inventory_menu.map_vote_menu.title"));
    }
    
    @Getter
    public enum MapVoteMenuItems {
        MAP_ITEM("menus.inventory_menu.map_vote_menu.map_item"),
        VOTE_RESULT_BAR("menus.inventory_menu.map_vote_menu.vote_result_bar"),
        BACKWARDS_ITEM_ACTIVE("menus.inventory_menu.map_vote_menu.navigation.backwards_item.active"),
        BACKWARDS_ITEM_INACTIVE("menus.inventory_menu.map_vote_menu.navigation.backwards_item.inactive"),
        FORWARDS_ITEM_ACTIVE("menus.inventory_menu.map_vote_menu.navigation.forwards_item.active"),
        FORWARDS_ITEM_INACTIVE("menus.inventory_menu.map_vote_menu.navigation.forwards_item.inactive");
        
        private final String path;

        MapVoteMenuItems(String path) {
            this.path = path;
        }
        
        public String getMessage() {
            return PluginMessages.getConvertedMsg(cfg.getString(getPath()));
        }
    }
    
}
