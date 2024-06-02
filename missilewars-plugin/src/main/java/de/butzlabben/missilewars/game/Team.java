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
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.game.misc.TeamSpawnProtection;
import de.butzlabben.missilewars.menus.MenuItem;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.MoneyUtil;
import de.butzlabben.missilewars.util.version.ColorConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    private final transient TeamType teamType;
    private final transient ArrayList<MWPlayer> members = new ArrayList<>();
    @Setter private Location spawn;
    @Setter private transient GameResult gameResult = GameResult.DRAW;
    private transient int currentInterval = 0;
    ItemStack[] teamArmor;
    ItemStack menuItem;
    
    public void initialTeam() {
        createTeamArmor();
        createMenuItem();
    }
    
    public Team getEnemyTeam() {
        if (this == game.getTeamManager().getTeam1()) return game.getTeamManager().getTeam2();
        if (this == game.getTeamManager().getTeam2()) return game.getTeamManager().getTeam1();
        return null;
    }
    
    public void removeMember(MWPlayer mwPlayer) {
        if (!isMember(mwPlayer)) return;

        Player player = mwPlayer.getPlayer();
        mwPlayer.setTeam(null);

        if (player != null) {
            player.setDisplayName("§7" + player.getName() + "§r");
        }

        members.removeIf(mp -> mp.getUuid().equals(mwPlayer.getUuid()));
    }

    public void addMember(MWPlayer mwPlayer) {
        // Is the player already in a team?
        if (mwPlayer.getTeam() != null) return;
        
        Player player = mwPlayer.getPlayer();
        if (player == null) {
            Logger.WARN.log("Could not add player " + mwPlayer.getUuid().toString() + " to a team because he went offline");
            return;
        }

        members.add(mwPlayer);
        mwPlayer.setTeam(this);
        player.setDisplayName(getColorCode() + player.getName() + "§r");
        
        player.getInventory().setArmorContents(getTeamArmor());
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
    private void createTeamArmor() {
        // no armor for spectator
        if (teamType == TeamType.SPECTATOR) return;
        
        
        Color color = ColorConverter.getColorFromCode(getColorCode());

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setUnbreakable(true);
        bootsMeta.setColor(color);
        boots.setItemMeta(bootsMeta);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setUnbreakable(true);
        leggingsMeta.setColor(color);
        leggings.setItemMeta(leggingsMeta);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setUnbreakable(true);
        chestplateMeta.setColor(color);
        chestplate.setItemMeta(chestplateMeta);

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        helmetMeta.setUnbreakable(true);
        helmetMeta.setColor(color);
        helmet.setItemMeta(helmetMeta);

        teamArmor = new ItemStack[] {boots, leggings, chestplate, helmet};
    }
    
    /**
     * This method creates the team menu-item based on the team color.
     */
    private void createMenuItem() {
        Color color = ColorConverter.getColorFromCode(getColorCode());
        
        menuItem = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) menuItem.getItemMeta();
        helmetMeta.setColor(color);
        menuItem.setItemMeta(helmetMeta);
        MenuItem.hideMetaValues(menuItem);
        MenuItem.setDisplayName(menuItem, Config.TeamSelectionMenuItems.TEAM_ITEM.getMessage()
                .replace("{player-team-name}", getFullname()));
    }

    public ItemStack getMenuItem() {
        return menuItem.clone();
    }

    public boolean isMember(MWPlayer mwPlayer) {
        return members.contains(mwPlayer);
    }

    /**
     * This method sends all team members the money for playing the game
     * with a specific amount for win and lose.
     */
    public void sendMoney(MWPlayer mwPlayer) {
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

        MoneyUtil.giveMoney(mwPlayer.getUuid(), money);
    }

    /**
     * This method sends all team members the title / subtitle of the
     * game result.
     */
    public void sendGameResultTitle(MWPlayer mwPlayer) {
        String title;
        String subTitle;

        switch (gameResult) {
            case WIN:
                title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_WINNER);
                subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_WINNER);
                break;
            case LOSE:
                title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_LOSER);
                subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_LOSER);
                break;
            case DRAW:
                title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_DRAW);
                subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_DRAW);
                break;
            default:
                title = null;
                subTitle = null;
                break;
        }

        mwPlayer.getPlayer().sendTitle(title, subTitle);
    }

    /**
     * This method sends all team members the end-sound of the
     * game result.
     */
    public void sendGameResultSound(MWPlayer mwPlayer) {

        Player player = mwPlayer.getPlayer();

        switch (gameResult) {
            case WIN:
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100, 3);
                break;
            case LOSE:
            case DRAW:
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 100, 0);
                break;
            default:
                break;
        }
    }
    
    public void teleportToTeamSpawn(Player player) {
        TeamSpawnProtection.regenerateSpawn(this);
        Game.teleportSafely(player, spawn);
    }
    
}
