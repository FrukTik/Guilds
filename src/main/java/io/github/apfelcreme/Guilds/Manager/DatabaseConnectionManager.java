package io.github.apfelcreme.Guilds.Manager;

import io.github.apfelcreme.Guilds.Guilds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Guilds
 * Copyright (C) 2015 Lord36 aka Apfelcreme
 * <p/>
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * @author Lord36 aka Apfelcreme on 24.04.2015.
 */
public class DatabaseConnectionManager {

    private Guilds plugin;

    public DatabaseConnectionManager(Guilds plugin) {
        this.plugin = plugin;
        initConnection();
    }

    /**
     * initializes the database connection
     *
     * @return
     */
    private Connection initConnection() {
        Connection connection;
        try {
            if (plugin.getGuildsConfig().getMysqlDatabase() == null || plugin.getGuildsConfig().getMysqlDatabase().isEmpty()) {
                return null;
            } else {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + plugin.getGuildsConfig().getMysqlUrl()
                        + "/" + "", plugin.getGuildsConfig().getMysqlUser(), plugin.getGuildsConfig().getMysqlPassword());
                if (connection != null) {
                    try {
                        connection.createStatement().execute(
                                "CREATE DATABASE IF NOT EXISTS " + plugin.getGuildsConfig().getMysqlDatabase());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                connection = DriverManager.getConnection("jdbc:mysql://" + plugin.getGuildsConfig().getMysqlUrl()
                                + "/" +
                                plugin.getGuildsConfig().getMysqlDatabase(),
                        plugin.getGuildsConfig().getMysqlUser(),
                        plugin.getGuildsConfig().getMysqlPassword());
                initTables();
                return connection;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * creates the database and tables
     */
    private void initTables() throws SQLException {
        Connection connection = getConnection();
        if (connection != null) {
            PreparedStatement statement;
            statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getMysqlDatabase());
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getAllianceTable() + " (" +
                    "allianceId INTEGER AUTO_INCREMENT, " +
                    "alliance VARCHAR(90) NOT NULL UNIQUE, " +
                    "tag VARCHAR(12) NOT NULL, " +
                    "founded BIGINT, " +
                    "color VARCHAR(20) NOT NULL DEFAULT 'DARK_GREEN', " +
                    "PRIMARY KEY (allianceId));");
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getGuildsTable() + " (" +
                    "guildId INTEGER AUTO_INCREMENT, " +
                    "guild VARCHAR(90) NOT NULL UNIQUE, " +
                    "tag VARCHAR(12) NOT NULL, " +
                    "color VARCHAR(20) NOT NULL DEFAULT 'DARK_GREEN', " +
                    "balance DOUBLE DEFAULT 0, " +
                    "exp BIGINT DEFAULT 0, " +
                    "level TINYINT DEFAULT 1, " +
                    "founded BIGINT, " +
                    "allianceId INTEGER, " +
                    "guildHomeX DOUBLE, " +
                    "guildHomeY DOUBLE, " +
                    "guildHomeZ DOUBLE, " +
                    "guildHomeWorld VARCHAR(50), " +
                    "guildHomeServer VARCHAR(50), " +
                    "FOREIGN KEY (allianceId) references " + plugin.getGuildsConfig().getAllianceTable() + " (allianceId), " +
                    "PRIMARY KEY (guildId));");
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getRanksTable() + " (" +
                    "rankId INTEGER AUTO_INCREMENT, " +
                    "rankName VARCHAR(50) NOT NULL, " +
                    "canInvite TINYINT(1) default 0, " +
                    "canKick TINYINT(1) default 0, " +
                    "canPromote TINYINT(1) default 0, " +
                    "canDisband TINYINT(1) default 0, " +
                    "canUpgrade TINYINT(1) default 0, " +
                    "canWithdrawMoney TINYINT(1) default 0, " +
                    "canUseBlackboard TINYINT(1) default 0, " +
                    "canDoDiplomacy TINYINT(1) default 0, " +
                    "isBaseRank TINYINT(1) default 0, " +
                    "isLeader TINYINT(1) default 0, " +
                    "guildId INTEGER, " +
                    "FOREIGN KEY (guildId) REFERENCES " + plugin.getGuildsConfig().getGuildsTable() + " (guildId), " +
                    "PRIMARY KEY (rankId));");
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getPlayerTable() + " (" +
                    "uuid VARCHAR(36) NOT NULL UNIQUE, " +
                    "playerName VARCHAR(50) NOT NULL, " +
                    "prefix VARCHAR(75), " +
                    "guildId INTEGER, " +
                    "rankId INTEGER, " +
                    "joined BIGINT, " +
                    "lastSeen BIGINT, " +
                    "FOREIGN KEY (guildId) REFERENCES " + plugin.getGuildsConfig().getGuildsTable() + " (guildId), " +
                    "FOREIGN KEY (rankId) REFERENCES " + plugin.getGuildsConfig().getRanksTable() + " (rankId), " +
                    "PRIMARY KEY (uuid));");
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getBlackboardTable() + " (" +
                    "messageId BIGINT auto_increment not null, " +
                    "player VARCHAR(36) NOT NULL, " +
                    "guildId INTEGER, " +
                    "message VARCHAR(255), " +
                    "timestamp TIMESTAMP, " +
                    "cleared TINYINT DEFAULT 0, " +
                    "FOREIGN KEY (player) REFERENCES " + plugin.getGuildsConfig().getPlayerTable() + " (uuid), " +
                    "FOREIGN KEY (guildId) REFERENCES " + plugin.getGuildsConfig().getGuildsTable() + " (guildId), " +
                    "PRIMARY KEY (messageId));");
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getInvitesTable() + " (" +
                    "inviteId INTEGER AUTO_INCREMENT, " +
                    "player VARCHAR(36) NOT NULL, " +
                    "targetPlayer VARCHAR(36) NOT NULL, " +
                    "guildId INTEGER NOT NULL, " +
                    "status SMALLINT DEFAULT 0, " +
                    "FOREIGN KEY (guildId) REFERENCES " + plugin.getGuildsConfig().getGuildsTable() + " (guildId), " +
                    "FOREIGN KEY (player) REFERENCES " + plugin.getGuildsConfig().getPlayerTable() + " (uuid), " +
                    "FOREIGN KEY (targetPlayer) REFERENCES " + plugin.getGuildsConfig().getPlayerTable() + " (uuid), " +
                    "PRIMARY KEY (inviteId));");
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
                    + plugin.getGuildsConfig().getAllianceInviteTable() + " (" +
                    "allianceInviteId INTEGER AUTO_INCREMENT, " +
                    "allianceId INTEGER NOT NULL, " +
                    "guildId INTEGER NOT NULL, " +
                    "status SMALLINT DEFAULT 0, " +
                    "FOREIGN KEY (guildId) REFERENCES " + plugin.getGuildsConfig().getGuildsTable() + " (guildId), " +
                    "FOREIGN KEY (allianceId) REFERENCES " + plugin.getGuildsConfig().getAllianceTable() + " (allianceId), " +
                    "PRIMARY KEY (allianceInviteId));");
            statement.executeUpdate();
            statement.close();

            connection.close();
        }
    }

    /**
     * returns the database connection to work with
     *
     * @return a Connection
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" +
                            plugin.getGuildsConfig().getMysqlUrl()
                            + "/" +
                            plugin.getGuildsConfig().getMysqlDatabase(),
                    plugin.getGuildsConfig().getMysqlUser(),
                    plugin.getGuildsConfig().getMysqlPassword());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Database connection could not be built", e);
        }
        return null;
    }

}
