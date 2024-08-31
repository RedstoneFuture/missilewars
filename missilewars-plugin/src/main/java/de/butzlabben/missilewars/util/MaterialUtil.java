package de.butzlabben.missilewars.util;

import de.butzlabben.missilewars.Logger;

import javax.annotation.Nullable;

import static org.bukkit.Material.valueOf;

public class MaterialUtil {
    
    public static org.bukkit.Material getMaterial(@Nullable String input) {
        if (input == null) {
            Logger.WARN.log("Material not defined!");
            return null;
        }
        
        String name = input.toUpperCase();
        try {
            return valueOf(name);
        } catch (Exception e) {
            Logger.WARN.log("Could not use '" + name + "' as bukkit material!");
        }
        return null;
    }
    
}
