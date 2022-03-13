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
import de.butzlabben.missilewars.game.GameResult;
import de.butzlabben.missilewars.util.MoneyUtil;
import de.butzlabben.missilewars.util.version.ColorConverter;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.player.MWPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Scoreboard;

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
    private transient GameResult gameResult;
    private transient org.bukkit.scoreboard.Team scoreboardTeam;
    private transient int currentInterval = 0;

    public ArrayList<MWPlayer> getMembers() {
        return members;
    }

    public Team getEnemyTeam() {
        if (this == game.getTeam1())
            return game.getTeam2();
        return game.getTeam1();
    }

    @SuppressWarnings("deprecation")
    public boolean removeMember(MWPlayer player) {
        if (!isMember(player))
            return false;

        Player p = player.getPlayer();
        player.setTeam(null);
        if (p != null) {
            if (scoreboardTeam.hasPlayer(p))
                scoreboardTeam.removePlayer(p);

            game.getScoreboard().getTeam("2Guest§7").addPlayer(p);
            p.setDisplayName("§7" + p.getName() + "§r");
        }
        return members.removeIf(mp -> mp.getUUID().equals(player.getUUID()));
    }

    @SuppressWarnings("deprecation")
    public void addMember(MWPlayer player) {
        if (isMember(player))
            return;
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
        Scoreboard sb = game.getScoreboard();
        if (sb.getPlayerTeam(p) != null)
            sb.getPlayerTeam(p).removePlayer(p);
        scoreboardTeam.addPlayer(p);
        setTeamArmor(p);
    }

    public org.bukkit.scoreboard.Team getSBTeam() {
        return scoreboardTeam;
    }

    public void setSBTeam(org.bukkit.scoreboard.Team team) {
        scoreboardTeam = team;
    }

    public String getFullname() {
        return getColorCode() + name;
    }

    public String getColorCode() {
        if (!color.startsWith("§"))
            return "§" + color;
        return color;
    }

    public void setTeamArmor(Player p) {
        Color c = ColorConverter.getColorFromCode(getColorCode());
        ItemStack is = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
        lam.setColor(c);
        is.setItemMeta(lam);
        VersionUtil.setUnbreakable(is);

        ItemStack is1 = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta lam1 = (LeatherArmorMeta) is1.getItemMeta();
        lam1.setColor(c);
        is1.setItemMeta(lam1);
        VersionUtil.setUnbreakable(is1);

        ItemStack is2 = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta lam2 = (LeatherArmorMeta) is2.getItemMeta();
        lam2.setColor(c);
        is2.setItemMeta(lam2);
        VersionUtil.setUnbreakable(is2);

        ItemStack is3 = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta lam3 = (LeatherArmorMeta) is3.getItemMeta();
        lam3.setColor(c);
        is3.setItemMeta(lam3);
        VersionUtil.setUnbreakable(is3);

        ItemStack[] armor = new ItemStack[] {is, is1, is2, is3};
        p.getInventory().setArmorContents(armor);
    }

    public boolean isMember(MWPlayer player) {
        return members.contains(player);
    }

    public void setGameResult(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    /**
     * This method sends all team members the money for playing the game
     * with a specific amount for win and lose.
     */
    public void sendMoney(MWPlayer missileWarsPlayer) {
        int money;

        switch (gameResult) {
            case WIN:
                money = game.getArena().getMoney().getWin();
                break;
            case LOSE:
                money = game.getArena().getMoney().getLoss();
                break;
            case DRAW:
                money = game.getArena().getMoney().getDraw();
                break;
            default:
                money = 0;
                break;
        }

        MoneyUtil.giveMoney(missileWarsPlayer.getUUID(), money);
    }

    /**
     * This method sends all team members the title / subtitle of the
     * game result.
     */
    public void sendGameResultTitle(MWPlayer missileWarsPlayer) {
        String title;
        String subTitle;

        switch (gameResult) {
            case WIN:
                title = MessageConfig.getNativeMessage("title_winner");
                subTitle = MessageConfig.getNativeMessage("subtitle_winner");
                break;
            case LOSE:
                title = MessageConfig.getNativeMessage("title_loser");
                subTitle = MessageConfig.getNativeMessage("subtitle_loser");
                break;
            case DRAW:
                title = MessageConfig.getNativeMessage("title_draw");
                subTitle = MessageConfig.getNativeMessage("subtitle_draw");
                break;
            default:
                title = null;
                subTitle = null;
                break;
        }

        VersionUtil.sendTitle(missileWarsPlayer.getPlayer(), title, subTitle);
    }

    /**
     * This method sends all team members the end-sound of the
     * game result.
     */
    public void sendGameResultSound(MWPlayer missileWarsPlayer) {

        switch (gameResult) {
            case WIN:
                VersionUtil.playPling(missileWarsPlayer.getPlayer());
                break;
            case LOSE:
            case DRAW:
                VersionUtil.playDraw(missileWarsPlayer.getPlayer());
                break;
            default:
                break;
        }
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
