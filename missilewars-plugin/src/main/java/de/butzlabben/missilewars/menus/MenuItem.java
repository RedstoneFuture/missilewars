package de.butzlabben.missilewars.menus;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.configuration.ActionSet;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.player.MWPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MenuItem {
    
    @Getter private final int slot;
    @Getter private final int priority;

    @Getter private ItemRequirement itemRequirement;

    @Getter @Setter private String materialName;
    
    @Setter private String displayName;
    @Getter private String finalDisplayName;
    
    @Setter private List<String> loreList;
    @Getter private final List<String> finalLoreList = new ArrayList<>();
    
    @Getter @Setter private ActionSet leftClickActions;
    @Getter @Setter private ActionSet rightClickActions;
    
    private ItemStack itemStack;
    
    
    public void updateItem(MWPlayer mwPlayer) {
        
        ItemStack tempItem;
        
        // basehead-<base64 (Value field in the head's give command)>
        if (materialName.startsWith("basehead-")) {
            tempItem = getCustomHead(materialName.split("-")[1]);
            
        } else if (materialName.equalsIgnoreCase("{player-team-item}")) {
            tempItem = mwPlayer.getTeam().getMenuItem();
            
        } else {
            tempItem = new ItemStack(Material.valueOf(materialName.toUpperCase()));
            
        }
        
        updatePapiValues(mwPlayer.getPlayer());
        
        // initial ItemMeta values:
        
        ItemMeta itemMeta = tempItem.getItemMeta();
        MenuItem.hideMetaValues(tempItem);
        MenuItem.setDisplayName(tempItem, finalDisplayName);
        itemMeta.setLore(finalLoreList);
        tempItem.setItemMeta(itemMeta);
        
        itemStack = tempItem;
    }
    
    public void sendToPlayer(MWPlayer mwPlayer) {
        updateItem(mwPlayer);
        mwPlayer.getPlayer().getInventory().setItem(slot, itemStack);
    }
    
    public static ItemStack getCustomHead(String base64Texture) {
        ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
        setSkinViaBase64(skullMeta, base64Texture);
        headItem.setItemMeta(skullMeta);
        return headItem;
    }

    /**
     * A method used to set the skin of a player skull via a base64 encoded string.
     *
     * Source: <a href="https://www.spigotmc.org/threads/generated-texture-to-heads.512604/#post-4198463">Post by BoBoBalloon</a>
     *
     * @param meta the skull meta to modify
     * @param base64 the base64 encoded string
     */
    private static void setSkinViaBase64(SkullMeta meta, String base64) {
        try {
            Method setProfile = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            setProfile.setAccessible(true);

            GameProfile profile = new GameProfile(UUID.randomUUID(), "skull-texture");
            profile.getProperties().put("textures", new Property("textures", base64));

            setProfile.invoke(meta, profile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Logger.ERROR.log("There was a severe internal reflection error when attempting to set the skin of a player skull via base64!");
            e.printStackTrace();
        }
    }
    
    private void updatePapiValues(Player player) {
        finalDisplayName = Messages.getPapiMessage(displayName, player);

        finalLoreList.clear();
        for (String lore : loreList) {
            finalLoreList.add(Messages.getPapiMessage(lore, player));
        }
    }
    
    public static void setEnchantment(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) itemMeta.addEnchant(Enchantment.LUCK, 10, true);
        itemStack.setItemMeta(itemMeta);
    }
    
    public static void setDisplayName(ItemStack itemStack, String displayName) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
    }
    
    public static void hideMetaValues(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, 
                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS);
        itemStack.setItemMeta(itemMeta);
    }
    
    public void setItemRequirement(ConfigurationSection input) {
        ConfigurationSection cfg = input.getConfigurationSection("view_requirement");
        if (cfg != null) {
            this.itemRequirement = new ItemRequirement(cfg);
            return;
        }
        this.itemRequirement = new ItemRequirement();
    }
}
