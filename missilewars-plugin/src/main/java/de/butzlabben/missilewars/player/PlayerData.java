/*
 * This file is part of MissileWars (https://github.com/Butzlabben/missilewars).
 * Copyright (c) 2018-2021 Daniel Nägele.
 *
 * MissileWars is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MissileWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MissileWars.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.butzlabben.missilewars.player;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ToString
@AllArgsConstructor
public class PlayerData implements ConfigurationSerializable {

    private UUID uuid;
    private ItemStack[] contents;
    private GameMode gameMode;
    private double health;
    private float exp;
    private int expLevel, foodLevel;
    private final long time;

    public PlayerData(Player player) {
        uuid = player.getUniqueId();
        contents = player.getInventory().getContents();
        gameMode = player.getGameMode();
        health = player.getHealth();
        exp = player.getExp();
        expLevel = player.getLevel();
        foodLevel = player.getFoodLevel();
        time = System.currentTimeMillis();
    }

    /**
     * This method is used to load the original player data from the temporary player-data file.
     */
    public PlayerData(Map<String, Object> elements) {
        uuid = UUID.fromString(elements.get("uuid").toString());
        contents = ((List<ItemStack>) elements.get("contents")).toArray(new ItemStack[] {});
        gameMode = GameMode.valueOf(elements.get("gamemode").toString());
        health = Double.parseDouble(elements.get("health").toString());
        exp = Float.parseFloat(elements.get("exp").toString());
        expLevel = Integer.parseInt(elements.get("exp-level").toString());
        foodLevel = Integer.parseInt(elements.get("food-level").toString());
        time = Long.parseLong(elements.get("time").toString());
    }

    public static PlayerData loadFromFile(File file) {
        Preconditions.checkArgument(file.isFile(), file.getAbsolutePath() + " is not a file");

        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        PlayerData data;

        data = yamlConfiguration.getSerializable("data", PlayerData.class);

        return data;
    }

    public void apply(Player player) {
        Preconditions.checkArgument(player.getUniqueId().equals(uuid),
                player + " is not the user of this data (data UUID: " + uuid + ")");

        player.getInventory().setContents(contents);
        player.setGameMode(gameMode);
        player.setHealth(Math.min(health, player.getMaxHealth()));
        player.setExp(exp);
        player.setLevel(expLevel);
        player.setFoodLevel(foodLevel);
    }

    public void saveToFile(String file) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("data", this);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to save the original player data in the temporary player-data file.
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("uuid", uuid.toString());
        serialized.put("contents", contents);
        serialized.put("gamemode", gameMode.name());
        serialized.put("health", health);
        serialized.put("exp", exp);
        serialized.put("exp-level", expLevel);
        serialized.put("food-level", foodLevel);
        serialized.put("time", time);
        return serialized;
    }
}
