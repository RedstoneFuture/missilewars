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

package de.butzlabben.missilewars.game.schematics.paste;


import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import lombok.Getter;

/**
 * @author Butzlabben
 * @since 23.09.2018
 */
public class PasteProvider {

    @Getter
    private static final Paster paster;

    static {
        if (MissileWars.getInstance().foundFAWE()) {
            paster = new FawePasteProvider();
            Logger.DEBUG.log("Chose FAWE paster.");
        } else {
            // FAWE Paster works also for (normal) WorldEdit
            paster = new FawePasteProvider();
            Logger.DEBUG.log("Chose FAWE paster.");
        }
    }

    private PasteProvider() {
    }

}
