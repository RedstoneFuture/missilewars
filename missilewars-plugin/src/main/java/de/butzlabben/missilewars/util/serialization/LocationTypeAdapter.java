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

package de.butzlabben.missilewars.util.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.butzlabben.missilewars.Logger;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationTypeAdapter extends TypeAdapter<Location> {

    private final boolean serializeWorld;

    public LocationTypeAdapter(boolean serializeWorld) {
        this.serializeWorld = serializeWorld;
    }

    public LocationTypeAdapter() {
        this(false);
    }

    @Override
    public void write(JsonWriter out, Location value) throws IOException {
        out.beginObject();
        if (serializeWorld && value.getWorld() != null) {
            out.name("world").value(value.getWorld().getName());
        }
        out.name("x").value(value.getX());
        out.name("y").value(value.getY());
        out.name("z").value(value.getZ());
        out.name("yaw").value(value.getYaw());
        out.name("pitch").value(value.getPitch());
        out.endObject();
    }

    @Override
    public Location read(JsonReader in) throws IOException {
        Location location = new Location(null, 0, 0, 0);

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "world":
                    if (!serializeWorld) break;
                    String worldName = in.nextString();
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        Logger.WARN.log("Could not find world \"" + worldName + "\" which is specified at one MissileWars sign");
                    }
                    location.setWorld(world);
                    break;
                case "x":
                    location.setX(in.nextDouble());
                    break;
                case "y":
                    location.setY(in.nextDouble());
                    break;
                case "z":
                    location.setZ(in.nextDouble());
                    break;
                case "yaw":
                    location.setYaw((float) in.nextDouble());
                    break;
                case "pitch":
                    location.setPitch((float) in.nextDouble());
                    break;
            }
        }

        in.endObject();
        return location;
    }
}
