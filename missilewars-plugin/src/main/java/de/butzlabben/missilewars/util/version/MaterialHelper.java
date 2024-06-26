package de.butzlabben.missilewars.util.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

public class MaterialHelper {
    
    // https://jd.papermc.io/paper/1.20.4/org/bukkit/Tag.html#ALL_SIGNS
    // https://minecraft.wiki/w/Tag#all_signs
    static Tag<Material> signMaterials = Bukkit.getTag(Tag.REGISTRY_BLOCKS, new NamespacedKey(NamespacedKey.MINECRAFT, "all_signs"), Material.class);
    
    public static boolean isSignMaterial(Material material) {
        if (signMaterials == null) return false;
        
        return signMaterials.isTagged(material);
    }
}
