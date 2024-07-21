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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.butzlabben.missilewars.Logger;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bukkit.Location;
import org.bukkit.World;

public class Serializer {

    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationTypeAdapter())
//                .registerTypeAdapter(EntityType.class, new EntityTypeTypeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public static void serialize(File file, Object object) throws IOException {
        try {
            correctEnums(object);
        } catch (Exception e) {
            Logger.WARN.log("Could not correct null enum values");
            e.printStackTrace();
        }
        String json = gson.toJson(object);
        String yaml = jsonToYaml(json);
        yaml = replaceColorStrings('§', '&', yaml);
        String oldYaml = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        if (!oldYaml.equals(yaml))
            Files.write(Paths.get(file.getAbsolutePath()), yaml.getBytes());
    }

    public static <T> T deserialize(File file, Class<T> clazz) throws IOException {
        String yaml = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        yaml = replaceColorStrings('&', '§', yaml);
        String json = yamlToJson(yaml);
        json = json.replace("max_X", "max_x");
        return gson.fromJson(json, clazz);
    }

    public static void setWorldAtAllLocations(Object object, World world) throws Exception {
        setWorldAtAllLocations(object, world, 0);
    }

    public static void setWorldAtAllLocations(Object object, World world, int depthCount) throws Exception {
        if (object == null) return;
        Preconditions.checkNotNull(world);

        Class<?> clazz = object.getClass();
        // Return here, as we only need to deserialize our own projects
        if (!clazz.getName().contains("de.butzlabben")) return;

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType() != Location.class) {
                if (depthCount < 5)
                    setWorldAtAllLocations(field.get(object), world, depthCount + 1);
                continue;
            }
            Location location = (Location) field.get(object);
            if (location == null) continue;
            if (location.getWorld() != null) continue;

            location.setWorld(world);
        }
    }

    // Sets all null enums to their first value
    private static void correctEnums(Object object) throws Exception {
        Preconditions.checkNotNull(object);

        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getType().isEnum()) continue;
            if (field.get(object) != null) continue;
            field.set(object, getEnumValues(field.getType())[0]);
        }
    }

    private static String jsonToYaml(String json) throws IOException {
        JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }

    private static String yamlToJson(String yaml) throws IOException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);
        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    private static Object[] getEnumValues(Class<?> enumClass)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = enumClass.getDeclaredField("$VALUES");
        f.setAccessible(true);
        Object o = f.get(null);
        return (Object[]) o;
    }

    private static String replaceColorStrings(char replace, char replacement, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; ++i) {
            if (b[i] == replace && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = replacement;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);

    }
}
