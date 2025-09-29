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

package de.butzlabben.missilewars.game.signs;

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

    private static final File DIR = new File(MissileWars.getInstance().getDataFolder(), "data");
    private static final File FILE = new File(DIR, "signs.json");
    private static boolean configNew = false;

    private final List<MWSign> signs = new ArrayList<>();

    public static SignRepository load() {

        // check if the directory "/data" exists
        if (!DIR.exists()) {
            DIR.mkdirs();
        }

        // check if the config file exists
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException e) {
                Logger.ERROR.log("Could not create signs.json!");
                e.printStackTrace();
            }
            configNew = true;
        }

        // load data if it's existing
        if (!configNew) {

            Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Location.class, new LocationTypeAdapter(true)).create();

            try (InputStream in = new FileInputStream(FILE);
                 JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return gson.fromJson(reader, SignRepository.class);
            } catch (IOException e) {
                Logger.WARN.log("Could not load MissileWars signs: Error: " + e.getMessage());
            }

        }

        // create default data object
        SignRepository repository = new SignRepository();
        repository.saveData();
        return repository;
    }

    public MWSign getSign(Location location) {
        Optional<MWSign> optional = MissileWars.getInstance().getSignRepository().getSigns()
                .stream().filter(sign -> sign.isLocation(location)).findAny();

        return optional.orElse(null);
    }

    public void saveData() {
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Location.class, new LocationTypeAdapter(true)).create();

        try (OutputStream out = new FileOutputStream(FILE);
             JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.setIndent("  ");
            gson.toJson(this, SignRepository.class, writer);
        } catch (Exception e) {
            Logger.WARN.log("Could not save MissileWars signs: Error: " + e.getMessage());
        }
    }

    public List<MWSign> getSigns(Game game) {
        return signs.stream().filter(s -> s.getLobby().equals(game.getGameConfig().getName())).collect(Collectors.toList());
    }
    
    public void checkAllSigns() {
        getSigns().forEach(mwSign -> {
            if (!mwSign.isValid()) {
                Logger.WARN.log("The specified configuration options for the sign at " + mwSign.getLocation() 
                        + " for the lobby " + mwSign.getLobby() + " are not valid.");
            }
        });
    }
}
