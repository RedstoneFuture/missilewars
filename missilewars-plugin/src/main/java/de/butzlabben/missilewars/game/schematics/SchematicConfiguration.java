package de.butzlabben.missilewars.game.schematics;

import de.butzlabben.missilewars.game.schematics.objects.SchematicObject;

import java.util.List;

public abstract class SchematicConfiguration {

    public String getObjectNameSingular() {
        return "Schematic";
    }

    public String getObjectNamePlural() {
        return "Schematics";
    }

    public abstract List<String> getSchematicNames();

    public abstract SchematicObject getSchematicFromFileName(String name);
    
    public abstract SchematicObject getSchematicFromDisplayName(String name);

    public abstract void check();
    
}
