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

package de.butzlabben.missilewars.listener.signs;

import de.butzlabben.missilewars.MessageConfig;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.util.version.VersionUtil;
import de.butzlabben.missilewars.wrapper.signs.MWSign;
import de.butzlabben.missilewars.wrapper.signs.SignRepository;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Optional;

public class ManageListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (!VersionUtil.isWallSignMaterial(event.getBlock().getType()))
            return;
        if (player.hasPermission("mw.sign.manage")) {
            String headLine = event.getLine(0).toLowerCase();
            if (headLine.equals("[missilewars]")) {
                String lobbyName = event.getLine(1);
                Game game = GameManager.getInstance().getGame(lobbyName);
                if (game == null) {
                    player.sendMessage(MessageConfig.getPrefix() + "§cCould not find lobby \"" + lobbyName + "\"");
                    event.setCancelled(true);
                } else {
                    SignRepository signRepository = MissileWars.getInstance().getSignRepository();
                    MWSign sign = new MWSign(event.getBlock().getLocation(), lobbyName);
                    sign.update();
                    signRepository.getSigns().add(new MWSign(event.getBlock().getLocation(), lobbyName));
                    signRepository.saveData();
                    player.sendMessage(MessageConfig.getPrefix() + "Sign was successfully created and connected");
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("mw.sign.manage")) {
            Block block = event.getBlock();
            if (VersionUtil.isWallSignMaterial(block.getType())) {
                SignRepository repository = MissileWars.getInstance().getSignRepository();
                Optional<MWSign> optional = repository.getSign(block.getLocation());
                if (optional.isPresent()) {
                    if (player.isSneaking()) {
                        MWSign sign = optional.get();
                        repository.getSigns().remove(sign);
                        repository.saveData();
                        player.sendMessage(MessageConfig.getPrefix() + "You have successfully removed this missilewars sign");
                    } else {
                        player.sendMessage(MessageConfig.getPrefix() + "§cYou have to be sneaking in order to remove this sign");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
