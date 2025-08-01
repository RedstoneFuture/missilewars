package de.butzlabben.missilewars.menus;

import de.butzlabben.missilewars.configuration.ActionSet;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.player.MWPlayer;
import de.redstoneworld.redutilities.items.HeadHelper;
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

import java.util.ArrayList;
import java.util.List;

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
            tempItem = HeadHelper.getCustomHead(materialName.split("-")[1]);
            
        } else if (materialName.equalsIgnoreCase("{player-team-item}")) {
            tempItem = mwPlayer.getTeam().getMenuItem();
            
        } else {
            tempItem = new ItemStack(Material.valueOf(materialName.toUpperCase()));
            
        }
        
        updatePapiValues(mwPlayer.getPlayer());
        
        // initial ItemMeta values:
        
        ItemMeta itemMeta = tempItem.getItemMeta();
        itemMeta.setLore(finalLoreList);
        tempItem.setItemMeta(itemMeta);
        MenuItem.hideMetaValues(tempItem);
        MenuItem.setDisplayName(tempItem, finalDisplayName);
        
        itemStack = tempItem;
    }
    
    public void sendToPlayer(MWPlayer mwPlayer) {
        updateItem(mwPlayer);
        mwPlayer.getPlayer().getInventory().setItem(slot, itemStack);
    }
    
    private void updatePapiValues(Player player) {
        finalDisplayName = PluginMessages.getPapiMessage(displayName, player);

        finalLoreList.clear();
        for (String lore : loreList) {
            finalLoreList.add(PluginMessages.getPapiMessage(lore, player));
        }
    }
    
    public static void setEnchantment(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) itemMeta.addEnchant(Enchantment.FORTUNE, 10, true);
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
                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
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
