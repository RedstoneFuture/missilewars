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
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.game.signs.MWSign;
import de.butzlabben.missilewars.game.signs.SignRepository;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class SignListener implements Listener {

    private static final String KEY_SIGN_HEADLINE = "[missilewars]";

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (!(block.getBlockData() instanceof WallSign)) return;

        SignRepository repository = MissileWars.getInstance().getSignRepository();
        Optional<MWSign> optional = repository.getSign(block.getLocation());
        if (optional.isEmpty()) return;

        MWSign sign = optional.get();
        Game game = GameManager.getInstance().getGame(sign.getLobby());
        if (game == null) return;

        event.getPlayer().teleport(game.getLobby().getSpawnPoint());
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof WallSign)) return;

        Player player = event.getPlayer();
        if (!hasManageSignPermission(player)) return;

        String headLine = event.getLine(0).toLowerCase();
        if (!headLine.equals(KEY_SIGN_HEADLINE)) return;

        String lobbyName = event.getLine(1);
        Game game = GameManager.getInstance().getGame(lobbyName);

        if (game != null) {
            MWSign sign = new MWSign(event.getBlock().getLocation(), lobbyName);
            sign.update();

            SignRepository signRepository = MissileWars.getInstance().getSignRepository();
            signRepository.getSigns().add(sign);
            signRepository.saveData();

            player.sendMessage(Messages.getPrefix() + "Sign was successfully created and connected");
        } else {
            player.sendMessage(Messages.getPrefix() + "§cCould not find lobby \"" + lobbyName + "\"");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof WallSign)) return;

        Player player = event.getPlayer();
        if (!hasManageSignPermission(player)) return;

        SignRepository repository = MissileWars.getInstance().getSignRepository();
        Optional<MWSign> optional = repository.getSign(block.getLocation());
        if (optional.isEmpty()) return;

        if (player.isSneaking()) {
            MWSign sign = optional.get();

            repository.getSigns().remove(sign);
            repository.saveData();

            player.sendMessage(Messages.getPrefix() + "You have successfully removed this missilewars sign");
        } else {
            player.sendMessage(Messages.getPrefix() + "§cYou have to be sneaking in order to remove this sign");
            event.setCancelled(true);
        }
    }

    private boolean hasManageSignPermission(Player player) {
        return player.hasPermission("mw.sign.manage");
    }
}
