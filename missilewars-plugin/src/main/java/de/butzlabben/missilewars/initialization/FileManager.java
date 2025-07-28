package de.butzlabben.missilewars.initialization;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.player.PlayerData;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileManager {
    
    public static void setupRoutine() {
        
        deleteTempWorlds();
        
        ConfigLoader.loadMainConfigs();
        
        saveDefaultResource(Config.getMissilesFolder(), "missiles.zip", MissileWars.getInstance());
        saveDefaultResource(Config.getShieldsFolder(), "shields.zip", MissileWars.getInstance());
        
        ConfigurationSerialization.registerClass(PlayerData.class);
    }
    
    public static void shotDownRoutine() {
        
        deleteTempWorlds();
        
    }
    
    /**
     * This methode deletes the old MissileWars (temporary) arena world from the last server session, if still exists.
     */
    private static void deleteTempWorlds() {
        File[] dirs = Bukkit.getWorldContainer().listFiles();
        if (dirs == null) return;

        for (File dir : dirs) {
            if (dir.getName().startsWith("mw-")) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (Exception ignored) {
                }
            }
        }
    }
    
    /**
     * Extracts a ZIP file from the plugin's resource folder and saves the contents
     * into the specified targetPath.
     * 
     * @param targetPath The directory where the extracted files should be saved.
     * @param defaultArchiveFile The name of the ZIP file in the plugin's resource folder.
     */
    public static void saveDefaultResource(String targetPath, String defaultArchiveFile, JavaPlugin plugin) {
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
        try (InputStream in = plugin.getResource(defaultArchiveFile)) {
            if (in == null) {
                Logger.ERROR.log("Unable to find resource '" + defaultArchiveFile + "'!");
                return;
            }
    
            // Unzip the resource to the target directory
            Logger.NORMAL.log("Unzipping resource '" + defaultArchiveFile + "' to directory: " + targetDir.getPath());
            unzip(in, targetDir);
        } catch (IOException e) {
            Logger.ERROR.log("Failed to unzip resource '" + defaultArchiveFile + "'!");
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
     * Creates a new configuration file in the specified directory if it does not already exist.
     * 
     * @param file The File object representing the configuration file to be created.
     */
    public static void createNewConfig(File file) {
        String fileName = file.getName();
        File dir = file.getParentFile(); // Get the parent directory of the file

        // Check if the directory exists; if not, create it
        if (dir != null && !dir.exists()) {
            if (dir.mkdirs()) {
                Logger.NORMAL.log("Directory '" + dir.getPath() + "' created.");
            } else {
                Logger.ERROR.log("Failed to create directory '" + dir.getPath() + "'.");
                return;
            }
        }

        // Check if the config file exists; if not, create it
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    Logger.NORMAL.log("Configuration file '" + fileName + "' created successfully.");
                } else {
                    Logger.ERROR.log("Failed to create configuration file '" + fileName + "'.");
                }
            } catch (IOException e) {
                Logger.ERROR.log("Could not create " + fileName + " due to an IOException!");
                e.printStackTrace();
            }
        } else {
            Logger.NORMAL.log("Configuration file '" + fileName + "' already exists. Skipping creation.");
        }
    }
    
}