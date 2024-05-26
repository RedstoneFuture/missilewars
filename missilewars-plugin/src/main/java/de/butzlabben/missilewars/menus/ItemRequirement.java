package de.butzlabben.missilewars.menus;

import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.player.MWPlayer;
import org.bukkit.configuration.ConfigurationSection;

public class ItemRequirement {
    
    private final Type type;
    
    private String permission = "";
    private String input = "";
    private String output = "";
    private boolean negateRequirement;
    
    public ItemRequirement(ConfigurationSection cfg) {
        String type = cfg.getString("type");
        this.type = getType(type);
        
        getCfgValues(cfg);
    }
    
    public ItemRequirement() {
        this.type = Type.NULL;
    }
    
    public boolean isRequirementsGiven(MWPlayer mwPlayer) {
        if (type == Type.NULL) return true;

        boolean result = false;
        String finalPermission = Messages.getPapiMessage(permission, mwPlayer.getPlayer());
        String finalInput = Messages.getPapiMessage(input, mwPlayer.getPlayer());
        String finalOutput = Messages.getPapiMessage(output, mwPlayer.getPlayer());
        
        if (type == Type.HAS_PERMISSION) {
            if ((finalPermission.isEmpty()) || (finalPermission.isBlank())) return false;
            
            result = mwPlayer.getPlayer().hasPermission(finalPermission);
            
        } else if (type == Type.STRING_EQUALS) {
            if ((finalInput.isEmpty()) || (finalInput.isBlank())) return false;
            if (finalOutput.isEmpty()) return false;
            
            result = finalInput.equals(finalOutput);
            
        } else if (type == Type.STRING_EQUALS_IGNORE_CASE) {
            if ((finalInput.isEmpty()) || (finalInput.isBlank())) return false;
            if (finalOutput.isEmpty()) return false;
            
            result = finalInput.equalsIgnoreCase(finalOutput);
            
        } else if (type == Type.STRING_CONTAINS) {
            if ((finalInput.isEmpty()) || (finalInput.isBlank())) return false;
            if (finalOutput.isEmpty()) return false;
            
            result = finalInput.contains(finalOutput);
            
        }
        
        if (negateRequirement) return !result;
        return result;
    }
    
    private Type getType(String input) {
        switch (input) {
            case "!has permission":
                negateRequirement = true;
            case "has permission":
                return Type.HAS_PERMISSION;
                
            case "!string equals":
                negateRequirement = true;
            case "string equals":
                return Type.STRING_EQUALS;
                
            case "!string equals ignorecase":
                negateRequirement = true;
            case "string equals ignorecase":
                return Type.STRING_EQUALS_IGNORE_CASE;
                
            case "!string contains":
                negateRequirement = true;
            case "string contains":
                return Type.STRING_CONTAINS;
                
            default: return Type.NULL;
        }
    }
    
    private void getCfgValues(ConfigurationSection cfg) {
        switch (type) {
            case HAS_PERMISSION:
                permission = cfg.getString("permission");
                break;
            case STRING_EQUALS:
            case STRING_EQUALS_IGNORE_CASE:
            case STRING_CONTAINS: 
                input = cfg.getString("input");
                output = cfg.getString("output");
        }
    }
    
    enum Type {
        NULL,
        HAS_PERMISSION,
        STRING_EQUALS,
        STRING_EQUALS_IGNORE_CASE,
        STRING_CONTAINS,
    }
    
}
