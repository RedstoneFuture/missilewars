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

package de.butzlabben.missilewars.util;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import org.bukkit.Bukkit;

import java.sql.*;

/**
 * @author Butzlabben
 * @since 13.08.2018
 */
public class ConnectionHolder {

    private static final Object lock = new Object();
    private static Connection connection;

    private ConnectionHolder() {
    }

    public static void connect(String host, String database, String port, String user, String password) {
        synchronized (lock) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Logger.ERROR.log("[MySQL] §cDrivers are not working properly");
                Bukkit.getPluginManager().disablePlugin(MissileWars.getInstance());
                return;
            }
            try {
                if (connection != null && !connection.isClosed())
                    connection.close();
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?user="
                        + user + "&password=" + password);
            } catch (SQLException e) {
                Logger.ERROR.log("[MySQL] Failed to connect with given server:");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(MissileWars.getInstance());
            }
        }
    }

    public static void close() {
        synchronized (lock) {
            try {
                if (connection == null || connection.isClosed()) {
                    Logger.ERROR.log("[MySQL] Connection does not exist or was already closed");
                    return;
                }
                connection.close();
            } catch (SQLException e) {
                Logger.ERROR.log("[MySQL] Connection could not be closed");
                e.printStackTrace();
            }
        }
    }

    public static PreparedStatement prepareStatement(String sql) throws SQLException {
        synchronized (lock) {
            if (!isConnectionValid())
                connect();
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }
    }

    public static ResultSet executeQuery(PreparedStatement ps) throws SQLException {
        synchronized (lock) {
            if (!isConnectionValid())
                connect();
            return ps.executeQuery();
        }
    }

    public static int executeUpdate(PreparedStatement ps) throws SQLException {
        synchronized (lock) {
            if (!isConnectionValid())
                connect();
            return ps.executeUpdate();
        }
    }

    public static Connection getConnection() throws SQLException {
        synchronized (lock) {
            if (!isConnectionValid())
                connect();
            return connection;
        }
    }

    public static void connect() {
        connect(Config.getHost(), Config.getDatabase(), Config.getPort(), Config.getUser(), Config.getPassword());
    }

    public static boolean isConnectionValid() throws SQLException {
        return connection != null && !connection.isClosed() && connection.isValid(5);
    }
}
