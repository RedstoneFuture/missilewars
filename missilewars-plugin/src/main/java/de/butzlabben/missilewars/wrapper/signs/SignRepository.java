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

package de.butzlabben.missilewars.wrapper.signs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import de.butzlabben.missilewars.util.serialization.LocationTypeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class SignRepository {

    private final static String PATH = "data";
    private final static String FILE_NAME = "signs.json";

    private final List<MWSign> signs = new ArrayList<>();

    public static SignRepository load() {
        File dir = new File(MissileWars.getInstance().getDataFolder(), PATH);
        if (!dir.exists()) {
            dir.mkdirs();
            return null;
        }
        File file = new File(dir, FILE_NAME);
        if (!file.exists())
            return null;
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Location.class, new LocationTypeAdapter(true)).create();
        try (InputStream in = new FileInputStream(file);
             JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            return gson.fromJson(reader, SignRepository.class);
        } catch (IOException e) {
            Logger.WARN.log("Could not load missilewars signs: Error: " + e.getMessage());
        }
        return null;
    }

    public Optional<MWSign> getSign(Location location) {
        return MissileWars.getInstance().getSignRepository().getSigns()
                .stream().filter(sign -> sign.isLocation(location)).findAny();
    }

    public void save() {
        File dir = new File(MissileWars.getInstance().getDataFolder(), PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Location.class, new LocationTypeAdapter(true)).create();
        try (OutputStream out = new FileOutputStream(new File(dir, FILE_NAME));
             JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.setIndent("  ");
            gson.toJson(this, SignRepository.class, writer);
        } catch (Exception e) {
            Logger.WARN.log("Could not save missilewars signs: Error: " + e.getMessage());
        }
    }

    public List<MWSign> getSigns(Game game) {
        return signs.stream().filter(s -> s.getLobby().equals(game.getArena().getName())).collect(Collectors.toList());
    }
}
