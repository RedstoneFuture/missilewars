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

package de.butzlabben.missilewars.game.missile.paste;


import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.util.version.VersionUtil;

/**
 * @author Butzlabben
 * @since 23.09.2018
 */
public class PasteProvider {

    private static final Paster paster;

    static {
        if (MissileWars.getInstance().foundFAWE()) {
            if (VersionUtil.getVersion() < 16) {
                paster = new R1_13FawePasteProvider();
                Logger.DEBUG.log("Chose 1.13 FAWE paster");
            } else {
                paster = new R1_16FawePasteProvider();
                Logger.DEBUG.log("Chose 1.16 FAWE paster");
            }
        } else {
            paster = new R1_13WEPasteProvider();
            Logger.DEBUG.log("Chose 1.13 WE paster");
        }
    }

    private PasteProvider() {
    }

    public static Paster getPaster() {
        return paster;
    }
}
