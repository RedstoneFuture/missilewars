package de.butzlabben.missilewars.initialization;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.PluginMessages;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigLoader {
    
    public static void loadConfigs() {
        
        Config.load();
        PluginMessages.load();
        
    }
    
    public static YamlConfiguration loadConfigFile(File file) {
        
        // check if the directory and the file exists or create it new
        FileManager.createNewConfig(file);
        
        // try to load the config
        YamlConfiguration cfg = getLoadedConfig(file);
        
        // copy the config input
        cfg.options().copyDefaults(true);
        
        return cfg;
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
    
}