package de.butzlabben.missilewars.game.schematics.objects;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.File;

public class Shield extends SchematicObject {


    public Shield(String schematic, String displayName, int occurrence) {
        super(schematic, displayName, occurrence);
    }

    @Override
    public File getSchematicFolder() {
        return new File(Config.getShieldsFolder());
    }

    public void paste(Snowball ball) {
        try {
            Location loc = ball.getLocation();
            Vector pastePos = new Vector(loc.getX(), loc.getY(), loc.getZ());
            
            MissileWars.getInstance().getSchematicPaster().pasteSchematic(getSchematic(), pastePos, 0, loc.getWorld());
            ball.remove();
        } catch (Exception e) {
            Logger.ERROR.log("Could not load " + getDisplayName());
            e.printStackTrace();
        }
    }

    /**
     * This method provides the shield spawn item based on the
     * snowball item specification in the arena configuration.
     *
     * @return ItemStack = the snowball with the shield name
     */
    @Override
    public ItemStack getItem() {
        ItemStack snowball = new ItemStack(Material.SNOWBALL);
        ItemMeta snowballMeta = snowball.getItemMeta();
        snowballMeta.setDisplayName(getDisplayName());
        snowball.setItemMeta(snowballMeta);
        return snowball;
    }
    
}