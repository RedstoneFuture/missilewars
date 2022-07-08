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

package de.butzlabben.missilewars.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.butzlabben.missilewars.Config;
import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.inventory.CustomInv;
import de.butzlabben.missilewars.inventory.OrcItem;
import de.butzlabben.missilewars.inventory.pages.PageGUICreator;
import de.butzlabben.missilewars.util.stats.PlayerGuiFactory;
import de.butzlabben.missilewars.util.stats.PreFetcher;
import de.butzlabben.missilewars.util.stats.StatsUtil;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.stats.PlayerStats;
import de.butzlabben.missilewars.wrapper.stats.SavedStats;
import de.butzlabben.missilewars.wrapper.stats.StatsFetcher;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("mw|missilewars")
public class StatsCommands extends BaseCommand {

    private final static int MAX_FIGHT_DRAW_PERCENTAGE = 15;
    private final static int MIN_FIGHT_DURATION = 5;
    private final static double MAX_AVIATION_WIN = 0.1;
    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat preciseFormat = new SimpleDateFormat("hh:mm dd.MM.yyyy");

    @Subcommand("stats")
    @Description("Shows stats.")
    @Syntax("/mw stats [from] [arena]")
    @CommandPermission("mw.stats")
    public void onStats(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        StatsFetcher fetcher = getFetcher(player, args);
        if (fetcher == null) return;
        String arena = fetcher.getArena().replace("%", "");

        PreFetcher.PrePlayerFetchRunnable preFetchRunnable = PreFetcher.preFetchPlayers(fetcher);

        CustomInv inv = new CustomInv("§eMissileWars statistics", 3);
        List<String> criteriaLore = Arrays.asList("§7Statistics since: §e" + format.format(fetcher.getFrom()), "§7Specified arena: §e" + (arena.equals("") ? "any" : arena));
        inv.addItem(4, new OrcItem(Material.FEATHER, "§aStatistics search criteria", criteriaLore));

        int gameCount = fetcher.getGameCount();

        SavedStats avgStatsWithDraws = fetcher.getAverageSavedStats(false);
        SavedStats avgStatsWithoutDraws = fetcher.getAverageSavedStats(true);
        int draws = fetcher.getDrawFights();
        String duration = StatsUtil.formatDuration(Duration.ofMillis(avgStatsWithDraws.getTimeElapsed()));

        List<String> generalLore = Arrays.asList("§7Fights: §e" + gameCount, "§7Average game length: §e" + duration,
                "§7Games with a draw: §e" + draws,
                "§7Team1-wins ÷ Team2-wins: §e" + StatsUtil.formatDouble(avgStatsWithoutDraws.getTeamWon()),
                "§7Average player count: §e" + StatsUtil.formatDouble(avgStatsWithDraws.getPlayerCount()));
        inv.addItem(9, new OrcItem(Material.SLIME_BLOCK, "§aGeneral statistics", generalLore));

        List<String> playerLore = Arrays.asList("§7Unique players: §e" + fetcher.getUniquePlayers(), "", "§7Click to list players");
        OrcItem players = new OrcItem(VersionUtil.getPlayerSkullMaterial(), "§aPlayers", playerLore);
        players.setOnClick((p, inventory, item) -> {
            p.closeInventory();
            preFetchRunnable.stop();
            p.chat("/mw stats players " + format.format(fetcher.getFrom()) + " " + arena);
        });
        inv.addItem(13, players);

        List<String> gamesLore = Arrays.asList("", "§7Click to list games");
        OrcItem games = new OrcItem(Material.PAPER, "§aGames", gamesLore);
        games.setOnClick((p, inventory, item) -> {
            p.closeInventory();
            p.chat("/mw stats list " + format.format(fetcher.getFrom()) + " " + arena);
        });
        inv.addItem(17, games);

        inv.prettyFill();
        player.openInventory(inv.getInventory(player));
    }

    @Subcommand("stats recommendations")
    @Description("Shows recommendations.")
    @Syntax("/mw stats recommendations [from] [arena]")
    @CommandPermission("mw.stats.recommendations")
    public void onRecommendations(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        StatsFetcher fetcher = getFetcher(player, args);
        if (fetcher == null) return;
        SavedStats avgStatsWithDraws = fetcher.getAverageSavedStats(false);
        SavedStats avgStatsWithoutDraws = fetcher.getAverageSavedStats(true);
        List<String> recommendations = new ArrayList<>();
        int gameCount = fetcher.getGameCount();

        double avgWins = avgStatsWithoutDraws.getTeamWon();
        if (Math.abs(avgWins - 1) > MAX_AVIATION_WIN) {
            recommendations.add("It could be, that your map is biased to one team, as wins are not equally distributed");
        }

        int draws = fetcher.getDrawFights();
        if ((((double) draws / (double) gameCount) * 100) > MAX_FIGHT_DRAW_PERCENTAGE) {
            recommendations.add("Increase the game_length option. More than 15% of your games are draws");
        }

        Duration duration = Duration.ofMillis(avgStatsWithoutDraws.getTimeElapsed());
        if (((double) duration.getSeconds() / 60.0) <= MIN_FIGHT_DURATION) {
            recommendations.add("Remove some overpowered features. The average game length at won games is under 5 minutes");
        }
        // TODO implement more features

        if (recommendations.size() == 0) {
            player.sendMessage(MessageConfig.getPrefix() + "§aThere are currently no recommendations, everything seems fine :)");
        } else {
            player.sendMessage(MessageConfig.getPrefix() + "§7=====[ §eMissileWars recommendations §7]=====");
            recommendations.forEach(str -> player.sendMessage(MessageConfig.getPrefix() + str));
        }
    }

    @Subcommand("stats players")
    @Description("Shows player list.")
    @Syntax("/mw stats players [from] [arena]")
    @CommandPermission("mw.stats.players")
    public void onPlayers(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        StatsFetcher fetcher = getFetcher(player, args);
        if (fetcher == null) return;
        List<UUID> players = fetcher.getPlayers();
        List<PlayerStats> playerStats = players.stream().map(fetcher::getStatsFrom).collect(Collectors.toList());

        PlayerGuiFactory playerGuiFactory = new PlayerGuiFactory(playerStats);
        playerGuiFactory.openWhenReady(player);
    }

    @Subcommand("stats list")
    @Description("Lists history of games.")
    @Syntax("/mw stats list [from] [arena]")
    @CommandPermission("mw.stats.list")
    public void onList(CommandSender sender, String[] args) {

        if (!senderIsPlayer(sender)) return;
        Player player = (Player) sender;

        StatsFetcher fetcher = getFetcher(player, args);
        if (fetcher == null) return;
        List<SavedStats> players = fetcher.getAllStats();

        PageGUICreator<SavedStats> creator = new PageGUICreator<>("§eGame list", players, (item) -> {
            Duration duration = Duration.ofMillis(item.getTimeElapsed());
            return new OrcItem(Material.TNT, "§7" + players.indexOf(item),
                    "§7Started: §e" + preciseFormat.format(item.getTimeStart()),
                    "§7Duration: §e" + StatsUtil.formatDuration(duration), "§7Arena: §e" + item.getArena(),
                    "§7Players: §e" + (int) item.getPlayerCount(), "§7Team won: §e" + (int) item.getTeamWon());
        });
        creator.show(player);
    }

    private StatsFetcher getFetcher(Player player, String[] args) {
        if (!Config.isFightStatsEnabled()) {
            player.sendMessage(MessageConfig.getPrefix() + "§cFightStats are not enabled!");
            return null;
        }
        Date from = new Date(0);
        String arena = "";
        if (args.length > 0) {
            try {
                from = format.parse(args[0]);
            } catch (ParseException e) {
                player.sendMessage(MessageConfig.getPrefix() + "§cPlease use the date format dd.MM.yyyy");
                return null;
            }
            if (args.length > 1) {
                arena = args[1];
            }
        }

        StatsFetcher fetcher = new StatsFetcher(from, arena);
        if (fetcher.getGameCount() < 10) {
            player.sendMessage(MessageConfig.getPrefix() + "Please play more than 10 games to enable fight stats");
            return null;
        }
        player.sendMessage(MessageConfig.getPrefix() + "Loading data...");
        return fetcher;
    }

    private boolean senderIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return true;

        sender.sendMessage(MessageConfig.getPrefix() + "§cYou are not a player");
        return false;
    }
}
