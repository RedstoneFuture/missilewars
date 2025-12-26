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

package de.butzlabben.missilewars.listener;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.signs.MWSign;
import de.butzlabben.missilewars.game.signs.SignRepository;
import de.butzlabben.missilewars.util.MaterialHelper;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(MaterialHelper.isSignMaterial(block.getType()))) return;
        
        if (event.getPlayer().isSneaking()) return;
        
        MWSign sign = getSignRepository().getSign(block.getLocation());
        if (sign == null) return;
        
        Game game = GameManager.getInstance().getGame(sign.getLobby());
        if (game == null) return;
        
        // Cancel the event so that the Vanilla sign-edit GUI is not opened before the teleport.
        event.setCancelled(true);
        
        game.teleportToLobbySpawn(event.getPlayer());
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        if (!(MaterialHelper.isSignMaterial(block.getType()))) return;

        Player player = event.getPlayer();
        if (!hasManageSignPermission(player)) return;
        
        // Check Prefix (line 1):
        String headLine = event.getLine(0);
        if (headLine == null) return;
        if (!isPluginKeyword(headLine)) return;

        // Check Lobby name (line 2):
        
        /*
        * If a sign only contains one color-code at the beginning, this is retained. In difference, 
        * if it contains several color-codes, the colors are removed completely.
        * 
        * For the sake of completeness, the color is generally removed here so that the string search 
        * with the Lobby name always works correctly.
         */
        String gameName = ChatColor.stripColor(event.getLine(1));
        if ((gameName == null) || (gameName.isBlank())) {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.SIGNEDIT_EMPTY_GAME));
            event.setCancelled(true);
            return;
        }
        
        Game game = GameManager.getInstance().getGame(gameName);
        if (game != null) {
            
            // Removing old sign entry if exists:
            MWSign sign = getSignRepository().getSign(block.getLocation());
            if (sign != null) getSignRepository().getSigns().remove(sign);
            
            // Updating sign content:
            sign = new MWSign(event.getBlock().getLocation(), gameName);
            sign.update();
            
            // (Re-)Saving sign in MissileWars in '/data/signs.json':
            getSignRepository().getSigns().add(sign);
            getSignRepository().saveData();

            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.SIGNEDIT_SIGN_CREATED));
            
        } else {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.SIGNEDIT_GAME_NOT_FOUND).replace("%input%", gameName));
            event.setCancelled(true);
            
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(MaterialHelper.isSignMaterial(block.getType()))) return;

        Player player = event.getPlayer();
        if (!hasManageSignPermission(player)) return;
        
        MWSign sign = getSignRepository().getSign(block.getLocation());
        if (sign == null) return;

        if (player.isSneaking()) {
            getSignRepository().getSigns().remove(sign);
            getSignRepository().saveData();

            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.SIGNEDIT_SIGN_REMOVED));
        } else {
            player.sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.SIGNEDIT_SIGN_REMOVE_DESC));
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onSignDrop(BlockDropItemEvent event) {
        getSignRepository().getSigns().removeIf(mwSign -> !mwSign.isValid());
        getSignRepository().saveData();
    }
    
    private boolean hasManageSignPermission(Player player) {
        return player.hasPermission("mw.sign.manage");
    }

    /**
     * This method checks whether the input string corresponds to the 
     * MissileWars plugin keyword 'missilewars' or 'mw' (case-insensitive) 
     * in square brackets.
     * 
     * @param input (String) the target string
     * @return 'true', if it equals one of the plugin keywords
     */
    private boolean isPluginKeyword(String input) {
        return ((input.equalsIgnoreCase("[missilewars]")) || (input.equalsIgnoreCase("[mw]")));
    }
    
    private SignRepository getSignRepository() {
        return MissileWars.getInstance().getSignRepository();
    }
}
