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

package de.butzlabben.missilewars.wrapper.player;

import com.google.common.base.Preconditions;
import de.butzlabben.missilewars.util.version.VersionUtil;
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
import java.util.Map;
import java.util.UUID;

@ToString
@AllArgsConstructor
public class PlayerData implements ConfigurationSerializable {

    private final long time;
    private UUID uuid;
    private ItemStack[] contents;
    private float exp;
    private double health;
    private int expLevel, foodLevel;
    private GameMode gameMode;

    public PlayerData(Player player) {
        contents = player.getInventory().getContents();
        exp = player.getExp();
        expLevel = player.getLevel();
        foodLevel = player.getFoodLevel();
        health = player.getHealth();
        gameMode = player.getGameMode();
        uuid = player.getUniqueId();
        time = System.currentTimeMillis();
    }

    public static PlayerData loadFromFile(File file) {
        Preconditions.checkArgument(file.isFile(), file.getAbsolutePath() + " is not a file");

        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        PlayerData data;

        if (VersionUtil.getVersion() > 12) {
            data = yamlConfiguration.getSerializable("data", PlayerData.class);
        } else {
            data = (PlayerData) yamlConfiguration.get("data");
        }
        return data;
    }

    public void apply(Player player) {
        Preconditions.checkArgument(player.getUniqueId().equals(uuid),
                player + " is not the user of this data (data UUID: " + uuid + ")");

        player.getInventory().setContents(contents);
        player.setExp(exp);
        player.setLevel(expLevel);
        player.setHealth(Math.min(health, player.getMaxHealth()));
        player.setFoodLevel(foodLevel);
        player.setGameMode(gameMode);
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("uuid", uuid.toString());
        serialized.put("gamemode", gameMode.name());
        serialized.put("health", health);
        serialized.put("food-level", foodLevel);
        serialized.put("exp", exp);
        serialized.put("exp-level", expLevel);
        serialized.put("contents", contents);
        serialized.put("time", time);
        return serialized;
    }
}
