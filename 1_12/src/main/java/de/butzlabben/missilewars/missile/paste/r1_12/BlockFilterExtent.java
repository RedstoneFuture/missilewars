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

package de.butzlabben.missilewars.missile.paste.r1_12;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;

/**
 * @author Butzlabben
 * @since 28.09.2018
 */
public class BlockFilterExtent extends AbstractDelegateExtent {

    private byte data;

    protected BlockFilterExtent(Extent extent) {
        super(extent);
    }

    public BlockFilterExtent(Extent extent, byte data) {
        this(extent);
        this.data = data;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        BaseBlock block = super.getBlock(position);
        if (block.getId() == BlockID.STAINED_GLASS_PANE)
            block.setData(data);
        if (block.getId() == BlockID.STAINED_GLASS)
            block.setData(data);
        return block;
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        if (block.getId() == 160)
            block.setData(data);
        return super.setBlock(location, block);
    }
}
