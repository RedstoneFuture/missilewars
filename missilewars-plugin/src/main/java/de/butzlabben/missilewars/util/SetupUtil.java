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
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.arena.Arena;
import de.butzlabben.missilewars.game.Arenas;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Butzlabben
 * @since 14.08.2018
 */
public class SetupUtil {

    private static final int BUFFER_SIZE = 4096;

    private SetupUtil() {
    }

    public static boolean isNewConfig(File dir, File file) {
        String fileName = file.getName();

        // check if the directory exists
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // check if the config file exists
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Logger.ERROR.log("Could not create " + fileName + "!");
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    public static YamlConfiguration getLoadedConfig(File file) {
        String fileName = file.getName();
        YamlConfiguration cfg;

        try {
            cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            Logger.ERROR.log("Couldn't load " + fileName + "!");
            e.printStackTrace();
            return null;
        }
        return cfg;
    }

    public static void safeFile(File file, YamlConfiguration cfg) {
        String fileName = file.getName();

        try {
            cfg.save(file);
        } catch (IOException e) {
            Logger.ERROR.log("Could not save " + fileName + "!");
            e.printStackTrace();
        }
    }

    public static void checkShields() {
        File shieldFolder = new File(MissileWars.getInstance().getDataFolder(), "shields");
        if (!shieldFolder.exists()) {
            shieldFolder.mkdir();
        }

        for (Arena arena : Arenas.getARENAS().values()) {
            File shield = new File(shieldFolder, arena.getShieldConfiguration().getSchematic());
            if (!shield.isFile()) {
                String resource = "shield.schematic";

                Logger.BOOT.log("Copying default shield schematic (" + resource + ")");
                copyFile(resource, shield.getPath());

            }
        }
    }

    public static void checkMap(String worldName) {
        File arenasFolder = new File(Config.getArenasFolder());
        File file = new File(arenasFolder, worldName);
        if (!file.isDirectory()) {
            String resource = "MissileWars-Arena.zip";

            Logger.WARN.log("There was no map found with the name \"" + worldName + "\"");
            Logger.BOOT.log("Copying default map (" + resource + ")");

            try {
                copyAndUnzip(resource, file.getPath());
            } catch (IOException e) {
                Logger.ERROR.log("Unable to copy new map!");
                e.printStackTrace();
            }
        }
    }

    public static void checkMissiles() {
        File file = new File(Config.getMissilesFolder());
        if (!file.isDirectory()) {
            String resource = "missiles.zip";

            Logger.BOOT.log("Copying default missiles folder (" + resource + ")");

            try {
                copyAndUnzip(resource, file.getPath());
            } catch (IOException e) {
                Logger.ERROR.log("Unable to copy missiles!");
                e.printStackTrace();
            }
        }
    }

    private static void copyFile(String resource, String out) {
        File file = new File(out);
        if (!file.exists()) {
            try {
                InputStream in = MissileWars.getInstance().getResource(resource);
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                Logger.ERROR.log("Wasn't able to create Config");
                e.printStackTrace();
            }
        }
    }

    public static void copyAndUnzip(String resource, String outputFolder) throws IOException {
        InputStream in = JavaPlugin.getPlugin(MissileWars.class).getResource(resource);

        unzip(in, outputFolder);
    }

    public static void unzip(InputStream inputStream, String destDirectory) throws IOException {
        if (inputStream == null) {
            return;
        }

        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        ZipInputStream zipIn = new ZipInputStream(inputStream);

        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn    the input stream
     * @param filePath the path to extract it to
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
