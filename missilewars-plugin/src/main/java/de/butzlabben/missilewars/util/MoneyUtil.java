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

package de.butzlabben.missilewars.util;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.PluginMessages;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author Butzlabben
 * @since 13.08.2018
 */
public class MoneyUtil {

    private static Object economy = null;

    static {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (service != null)
                    economy = service.getProvider();
            } catch (Exception ignore) {
            }

        }

        if (economy == null)
            Logger.WARN.log("Couldn't find a Vault Economy extension");
    }

    private MoneyUtil() {
    }

    public static void giveMoney(UUID uuid, int money) {
        if (money < 0)
            return;
        if (uuid == null)
            return;
        if (economy == null)
            return;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        EconomyResponse r = ((Economy) economy).depositPlayer(op, money);
        if (!r.transactionSuccess()) {
            Logger.WARN.log("Couldn't give " + money + " to " + op.getName());
            Logger.WARN.log("Message: " + r.errorMessage);
        } else {
            if (Bukkit.getPlayer(uuid) != null)
                Bukkit.getPlayer(uuid).sendMessage(PluginMessages.getMessage(true, PluginMessages.MessageEnum.GAME_RESULT_MONEY).replace("%money%", Integer.toString(money)));
        }
    }
}
