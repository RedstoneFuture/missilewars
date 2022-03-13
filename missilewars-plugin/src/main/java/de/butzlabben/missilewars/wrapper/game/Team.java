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

package de.butzlabben.missilewars.wrapper.game;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.util.MoneyUtil;
import de.butzlabben.missilewars.util.version.ColorConverter;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.event.GameEndEvent;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */

@RequiredArgsConstructor
@ToString(of = {"name", "members"})
@Getter
public class Team {

    private final String name;
    private final String color;
    private final Game game;
    private final transient ArrayList<MWPlayer> members = new ArrayList<>();
    @Setter private Location spawn;
    private transient boolean won;
    private transient org.bukkit.scoreboard.Team scoreboardTeam;
    private transient int currentInterval = 0;
    ItemStack[] teamArmor;

    public ArrayList<MWPlayer> getMembers() {
        return members;
    }

    public Team getEnemyTeam() {
        if (this == game.getTeam1())
            return game.getTeam2();
        return game.getTeam1();
    }
    
    public void removeMember(MWPlayer player) {
        if (!isMember(player)) return;

        Player p = player.getPlayer();
        player.setTeam(null);

        if (p != null) {
            p.setDisplayName("§7" + p.getName() + "§r");
        }

        members.removeIf(mp -> mp.getUUID().equals(player.getUUID()));

        game.getScoreboardManager().resetScoreboard();
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
    public void addMember(MWPlayer player) {
        if (isMember(player)) return;

        // Already in a team?
        if (player.getTeam() != null) {
            player.getTeam().removeMember(player);
        }

        Player p = player.getPlayer();
        if (p == null) {
            Logger.WARN.log("Could not add player " + player.getUUID().toString() + " to a team because he went offline");
            return;
        }

        members.add(player);
        player.setTeam(this);
        p.setDisplayName(getColorCode() + p.getName() + "§r");
        p.getInventory().setArmorContents(getTeamArmor());

        game.getScoreboardManager().updateScoreboard();
        p.setScoreboard(game.getScoreboard());
    }

    public String getFullname() {
        return getColorCode() + name;
    }

    public String getColorCode() {
        if (!color.startsWith("§"))
            return "§" + color;
        return color;
    }

    /**
     * This method creates the team armor based on the team color.
     */
    public void createTeamArmor() {
        Color color = ColorConverter.getColorFromCode(getColorCode());

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(color);
        boots.setItemMeta(bootsMeta);
        VersionUtil.setUnbreakable(boots);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(color);
        leggings.setItemMeta(leggingsMeta);
        VersionUtil.setUnbreakable(leggings);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setColor(color);
        chestplate.setItemMeta(chestplateMeta);
        VersionUtil.setUnbreakable(chestplate);

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        helmetMeta.setColor(color);
        helmet.setItemMeta(helmetMeta);
        VersionUtil.setUnbreakable(helmet);

        teamArmor = new ItemStack[] {boots, leggings, chestplate, helmet};
    }

    public ItemStack[] getTeamArmor() {
        return this.teamArmor;
    }

    public boolean isMember(MWPlayer player) {
        return members.contains(player);
    }

    public void win() {
        int money = game.getArena().getMoney().getWin();
        for (MWPlayer player : members) {
            MoneyUtil.giveMoney(player.getUUID(), money);
        }
        for (MWPlayer p : game.getPlayers().values()) {
            Player player = p.getPlayer();
            if (player != null && player.isOnline())
                VersionUtil.sendTitle(player, MessageConfig.getNativeMessage("title_won").replace("%team%", getFullname()),
                        MessageConfig.getNativeMessage("subtitle_won"));
        }
        won = true;
        Bukkit.getPluginManager().callEvent(new GameEndEvent(game, this));
    }

    public void lose() {
        int money = game.getArena().getMoney().getLoss();
        for (MWPlayer player : members) {
            MoneyUtil.giveMoney(player.getUUID(), money);
        }
    }

    public boolean isWon() {
        return won;
    }

    public void updateIntervals(int newInterval) {
        if (newInterval < currentInterval && currentInterval != 0) {
            getGame().broadcast(MessageConfig.getMessage("team_buffed").replace("%team%", getFullname()));
        }
        if (newInterval > currentInterval && currentInterval != 0) {
            getGame().broadcast(MessageConfig.getMessage("team_nerved").replace("%team%", getFullname()));
        }
        for (MWPlayer mwPlayer : members) {
            mwPlayer.setPeriod(newInterval);
        }
        currentInterval = newInterval;
    }
}
