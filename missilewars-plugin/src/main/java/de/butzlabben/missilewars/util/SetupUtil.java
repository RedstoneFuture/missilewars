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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    
    /**
     * Extracts a ZIP file from the plugin's resource folder and saves the contents
     * into the specified targetPath.
     * 
     * @param targetPath The directory where the extracted files should be saved.
     * @param defaultFile The name of the ZIP file in the plugin's resource folder.
     */
    public static void saveDefaultFiles(String targetPath, String defaultFile, JavaPlugin plugin) {
        // Ensure the target directory exists
        File targetDir = new File(targetPath);
        
        // Check if the target directory already exists; if so, skip extraction
        if (targetDir.exists()) {
            Logger.NORMAL.log("Directory '" + targetDir.getPath() + "' already exists. Skipping extraction.");
            return;
        }
        
        // Create the target directory if it does not exist
        if (!targetDir.mkdirs()) {
            Logger.ERROR.log("Failed to create directory '" + targetDir.getPath() + "'");
            return;
        }
        
        // Get the resource as an InputStream
        try (InputStream in = plugin.getResource(defaultFile)) {
            if (in == null) {
                Logger.ERROR.log("Unable to find resource '" + defaultFile + "'!");
                return;
            }
    
            // Unzip the resource to the target directory
            Logger.NORMAL.log("Unzipping resource '" + defaultFile + "' to directory: " + targetDir.getPath());
            unzip(in, targetDir);
        } catch (IOException e) {
            Logger.ERROR.log("Failed to unzip resource '" + defaultFile + "'!");
            e.printStackTrace();
        }
    }
    
    private static void unzip(InputStream in, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(targetDir, entry.getName());
    
                if (entry.isDirectory()) {
                    // Handle directory entries
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // Check if file already exists and decide whether to overwrite or not
                    if (newFile.exists()) {
                        Logger.WARN.log("File " + newFile.getName() + " already exists. Skipping.");
                        continue; // Skip if the file exists
                    }
    
                    // Ensure parent directory exists
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
    
                    // Write file content
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

}