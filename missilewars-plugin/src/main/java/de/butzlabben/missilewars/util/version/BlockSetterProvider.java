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

package de.butzlabben.missilewars.util.version;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.block.Block;

/**
 * @author Butzlabben
 * @since 04.09.2018
 */
public class BlockSetterProvider {

    private static final BlockDataSetter blockSetter;

    static {
        blockSetter = new NewBlockSetter();
    }

    private BlockSetterProvider() {
    }

    public static BlockDataSetter getBlockDataSetter() {
        return blockSetter;
    }

    public static Object getBlockData(Block block) {
        try {
            Method m = block.getClass().getMethod("getBlockData");
            return m.invoke(block);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void changeBlockData(String method, Object object, Object... args) {
        try {
            Method m = object.getClass().getMethod(method);
            m.invoke(object, args);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static class NewBlockSetter implements BlockDataSetter {

        @Override
        public void setData(Block block, Object data) {
            try {
                Method m = block.getClass().getMethod("setBlockData", org.bukkit.block.data.BlockData.class);
                m.invoke(block, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void setData(Block block, Object data, boolean update) {
            try {
                Method m = block.getClass().getMethod("setBlockData", org.bukkit.block.data.BlockData.class, boolean.class);
                m.invoke(block, data, update);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
