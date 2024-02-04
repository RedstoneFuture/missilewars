package de.butzlabben.missilewars.game.schematics.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.io.File;

@RequiredArgsConstructor
public class SchematicObject {
    
    // Class is not abstract because of Serializer:
    // "com.google.gson.JsonIOException: Abstract classes can't be instantiated! 
    // Register an InstanceCreator or a TypeAdapter for this type."

    @SerializedName("schematic") private final String schematicName;
    @SerializedName("name") private final String displayName;
    @Getter private final int occurrence;
    
    
    public File getSchematicFolder() {
        return null;
    }
    
    public File getSchematic() {
        File file = new File(getSchematicFolder(), getSchematicName(false));
        return file;
    }
    
    public String getSchematicName(boolean withoutExtension) {
        if (withoutExtension) {
            return schematicName.replace(".schematic", "")
                    .replace(".schem", "");
        }
        return schematicName;
    }
    
    public String getDisplayName() {
        String name = displayName;
        name = name.replace("%schematic_name%", getSchematicName(false))
                .replace("%schematic_name_compact%", getSchematicName(true));
        return name;
    }

    public ItemStack getItem() {
        return null;
    }
    
    public enum schematicType {
        MISSILE,
        SHIELD
    }

}
