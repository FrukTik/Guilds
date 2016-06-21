package io.github.apfelcreme.Guilds.Manager;

import io.github.apfelcreme.Guilds.Alliance.Alliance;
import io.github.apfelcreme.Guilds.Alliance.AllianceInvite;
import io.github.apfelcreme.Guilds.Guild.Guild;
import io.github.apfelcreme.Guilds.Guilds;
import io.github.apfelcreme.Guilds.GuildsUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
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
 */
public class AllianceManager {

    private Guilds plugin;

    /**
     * the alliance set
     */
    private Map<Integer, Alliance> alliances;

    public AllianceManager(Guilds plugin) {
        this.plugin = plugin;
        alliances = new Hashtable<Integer, Alliance>();
    }

    /**
     * loads the list of all guilds
     */
    public void loadAlliances() {
        plugin.runAsync(new Runnable() {
            public void run() {
                Connection connection = plugin.getDatabaseConnection();
                if (connection != null) {
                    try {
//                        guilds.clear();
                        alliances.clear();
                        PreparedStatement statement = connection.prepareStatement("Select allianceId from " + plugin.getGuildsConfig().getAllianceTable());
                        ResultSet resultSet = statement.executeQuery();
                        int z = 0;
                        while (resultSet.next()) {
                            reload(resultSet.getInt("allianceId"));
                            z++;
                        }
                        plugin.getLogger().info(z + " Allianzen synchronisiert");
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * reloads an alliance and all guilds that are in it
     *
     * @param allianceId the id of the alliance
     */
    public void reload(final Integer allianceId) {
        plugin.runAsync(new Runnable() {
            public void run() {

                Connection connection = plugin.getDatabaseConnection();
                if (connection != null) {
                    try {
                        Alliance alliance;

                        PreparedStatement statement = connection.prepareStatement(
                                "Select g.guildId, a.allianceId, a.alliance, a.tag as allianceTag, a.color as allianceColor, a.founded from " + plugin.getGuildsConfig().getGuildsTable() + " g " +
                                        "left join " + plugin.getGuildsConfig().getAllianceTable() + " a on g.allianceId = a.allianceId " +
                                        "where a.allianceId = ?");
                        statement.setInt(1, allianceId);
                        ResultSet resultSet = statement.executeQuery();
                        List<Guild> allianceMembers = new ArrayList<Guild>();
                        if (resultSet.next()) {
                            Integer allianceId = resultSet.getInt("allianceId");
                            String tag = resultSet.getString("allianceTag");
                            resultSet.beforeFirst();
                            while (resultSet.next()) {
                                allianceMembers.add(plugin.getGuildManager().getGuild(resultSet.getInt("guildId")));
                            }
                            resultSet.first();
                            alliance = new Alliance(allianceId,
                                    GuildsUtil.replaceChatColors(resultSet.getString("a.alliance")),
                                    GuildsUtil.replaceChatColors(resultSet.getString("allianceTag")),
                                    ChatColor.valueOf(resultSet.getString("allianceColor")),
                                    resultSet.getLong("founded"),
                                    allianceMembers);

                            /**
                             * load the pending AllianceInvites which are yet to be accepted or declined
                             */
                            statement = connection.prepareStatement(
                                    "SELECT guildId " +
                                            "from " + plugin.getGuildsConfig().getAllianceInviteTable() +
                                            " where status = 0 and allianceId = ?");
                            statement.setInt(1, allianceId);
                            resultSet = statement.executeQuery();
                            resultSet.beforeFirst();
                            while (resultSet.next()) {
                                Guild guild = plugin.getGuildManager().getGuild(resultSet.getInt("guildId"));
                                alliance.putPendingAllianceInvite(
                                        new AllianceInvite(
                                                alliance,
                                                guild
                                        )
                                );
                            }

                            alliances.remove(allianceId);
                            alliances.put(allianceId, alliance);
                            plugin.getLogger().info("Allianz " + alliance.getName() + " [" + tag + "] geladen");
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ConcurrentModificationException e) {
                        e.printStackTrace();
                        plugin.getLogger().info("Beim Laden ist ein Fehler aufgetreten!");
                    }
                }
            }
        });
    }

    public void checkForReload(int allianceId) {
        if (!alliances.containsKey(allianceId)) {
            reload(allianceId);
        }
    }

    /**
     * returns a list of all alliances
     *
     * @return all alliances in a list
     */
    public Collection<Alliance> getAlliances() {
        return alliances.values();
    }

    /**
     * returns a guilds alliance
     *
     * @param guild the guild
     * @return the alliance the given guild is part of
     */
    public Alliance getAlliance(Guild guild) {
        String guildName = guild.getName();
        for (Alliance alliance : alliances.values()) {
            for (Guild member : alliance.getGuilds()) {
                if (member.getName().equals(guildName)) {
                    return alliance;
                }
            }
        }
        return null;
    }

    /**
     * returns a players alliance
     *
     * @param player the player
     * @return the alliance the given players guild is in
     */
    public Alliance getAlliance(OfflinePlayer player) {
        for (Alliance alliance : alliances.values()) {
            for (Guild guild : alliance.getGuilds()) {
                if (guild.getMember(player.getUniqueId()) != null) {
                    return alliance;
                }
            }
        }
        return null;
    }

    /**
     * returns the alliance with the given name
     *
     * @param name the name
     * @return the alliance with the given name
     */
    public Alliance getAlliance(String name) {
        for (Alliance alliance : alliances.values()) {
            if (alliance.getName().equalsIgnoreCase(name)) {
                return alliance;
            }
        }
        return null;
    }

    /**
     * returns an alliance invite for a guild if there is one
     *
     * @param guild the guild
     * @return the invite sent to this guild
     */
    public AllianceInvite getInvite(Guild guild) {
        for (Alliance alliance : alliances.values())
            for (AllianceInvite invite : alliance.getPendingAllianceInvites()) {
                if (invite.getGuild().getName().equals(guild.getName())) {
                    return invite;
                }
            }

        return null;
    }


    /**
     * sets the invite status to 1 (=Accepted)
     */
    public void acceptInvite(final AllianceInvite invite) {
        plugin.runAsync(new Runnable() {
            public void run() {
                try {
                    Connection connection = plugin.getDatabaseConnection();
                    if (connection != null) {
                        PreparedStatement statement;
                        statement= connection.prepareStatement(
                                "UPDATE "+ plugin.getGuildsConfig().getAllianceInviteTable()+" SET status = 1 " +
                                        "WHERE guildId = ? " +
                                        "AND allianceId = ? ");
                        statement.setInt(1, invite.getGuild().getId());
                        statement.setInt(2, invite.getAlliance().getId());
                        statement.executeUpdate();
                        statement.close();

                        statement = connection.prepareStatement(
                                "UPDATE "+ plugin.getGuildsConfig().getGuildsTable()+
                                        " SET allianceId = ? where guildId = ?;");
                        statement.setInt(1, invite.getGuild().getId());
                        statement.setInt(2, invite.getAlliance().getId());
                        statement.executeUpdate();
                        statement.close();

                        connection.close();
                        plugin.getBungeeConnection().forceAllianceSync(invite.getAlliance().getId());
                        plugin.getBungeeConnection().forceGuildSync(invite.getGuild().getId());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * sets the invite status to 2 (=Denied)
     */
    public void denyInvite(final AllianceInvite invite) {
        plugin.runAsync(new Runnable() {
            public void run() {
                try {
                    Connection connection = plugin.getDatabaseConnection();
                    if (connection != null) {
                        PreparedStatement statement = connection.prepareStatement(
                                "UPDATE "+ plugin.getGuildsConfig().getAllianceInviteTable()+" SET status = 2 " +
                                        "WHERE guildId = ? " +
                                        "AND allianceId = ? ");
                        statement.setInt(1, invite.getGuild().getId());
                        statement.setInt(2, invite.getAlliance().getId());
                        statement.executeUpdate();
                        statement.close();

                        connection.close();
                        plugin.getBungeeConnection().forceAllianceSync(invite.getAlliance().getId());
                        plugin.getBungeeConnection().forceGuildSync(invite.getGuild().getId());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addInvite(final AllianceInvite invite) {
        plugin.runAsync(new Runnable() {
            public void run() {
                try {
                    Connection connection = plugin.getDatabaseConnection();
                    if (connection != null) {

                        PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO " + plugin.getGuildsConfig().getAllianceInviteTable() + " (allianceId, guildId) " +
                                        "VALUES (?, ?); ");
                        statement.setInt(1, invite.getAlliance().getId());
                        statement.setInt(2, invite.getGuild().getId());
                        statement.executeUpdate();
                        statement.close();

                        connection.close();
                        plugin.getBungeeConnection().forceAllianceSync(invite.getAlliance().getId());
                        plugin.getBungeeConnection().forceGuildSync(invite.getGuild().getId());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Add a new alliance
     * @param alliance The Alliance to add
     */
    public void create(final Alliance alliance) {
        plugin.runAsync(new Runnable() {
            public void run() {
                Connection connection = plugin.getDatabaseConnection();
                if (connection != null) {
                    try {
                        PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO " + plugin.getGuildsConfig().getAllianceTable() + "(alliance, founded, tag, color) VALUES (?, ?, ?, ?)");
                        statement.setString(1, alliance.getName());
                        statement.setLong(2, new Date().getTime());
                        statement.setString(3, alliance.getTag());
                        statement.setString(4, alliance.getColor().name());
                        statement.executeUpdate();


                        statement = connection.prepareStatement(
                                "Select allianceId from " + plugin.getGuildsConfig().getAllianceTable() + " where alliance = ?");
                        statement.setString(1, alliance.getName());
                        ResultSet resultSet = statement.executeQuery();
                        resultSet.first();
                        alliance.setId(resultSet.getInt("allianceId"));

                        for (Guild guild : alliance.getGuilds()) {
                            statement = connection.prepareStatement(
                                    "UPDATE " + plugin.getGuildsConfig().getGuildsTable() + " SET allianceId = ? " +
                                            "where guildId = ?");
                            statement.setInt(1, alliance.getId());
                            statement.setInt(2, guild.getId());
                            statement.executeUpdate();
                        }
                        connection.close();

                        plugin.getBungeeConnection().forceGuildSync(alliance.getGuilds().get(0).getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Remove an alliance
     * @param alliance The Alliance to remove
     */
    public void delete(final Alliance alliance) {
        plugin.runAsync(new Runnable() {
            public void run() {
                Connection connection = plugin.getDatabaseConnection();
                if (connection != null) {
                    try {
                        PreparedStatement statement = connection.prepareStatement(
                                "UPDATE " + plugin.getGuildsConfig().getGuildsTable() + " SET allianceId = null " +
                                        "WHERE allianceId = ? ");
                        statement.setInt(1, alliance.getId());
                        statement.executeUpdate();

                        statement = connection.prepareStatement("DELETE FROM " + plugin.getGuildsConfig().getAllianceInviteTable() +
                                " WHERE allianceId = ? ");
                        statement.setInt(1, alliance.getId());
                        statement.executeUpdate();

                        statement = connection.prepareStatement("DELETE FROM " + plugin.getGuildsConfig().getAllianceTable() +
                                " WHERE allianceId = ? ");
                        statement.setInt(1, alliance.getId());
                        statement.executeUpdate();

                        plugin.getBungeeConnection().forceGuildsSync();
                        plugin.getBungeeConnection().forceAlliancesSync();

                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setColor(final Alliance alliance, final ChatColor color) {
        alliance.setColor(color);
        plugin.runAsync(new Runnable() {
            public void run() {
                Connection connection = plugin.getDatabaseConnection();
                if (connection != null) {
                    try {
                        PreparedStatement statement = connection.prepareStatement(
                                "UPDATE " + plugin.getGuildsConfig().getAllianceTable() +
                                        " SET color = ? where allianceId = ? ");
                        statement.setString(1, color.name());
                        statement.setInt(2, alliance.getId());
                        statement.executeUpdate();
                        statement.close();
                        connection.close();

                        plugin.getBungeeConnection().forceGuildSync(alliance.getId());

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void removeMember(final Alliance alliance, final Guild guild) {
        plugin.runAsync(new Runnable() {
            public void run() {
                Connection connection = plugin.getDatabaseConnection();
                if (connection != null) {
                    try {
                        PreparedStatement statement = connection.prepareStatement(
                                "UPDATE " + plugin.getGuildsConfig().getGuildsTable() +
                                        " SET allianceId = null " +
                                        "where guildId = ? ");
                        statement.setInt(1, guild.getId());
                        statement.executeUpdate();
                        statement.close();
                        connection.close();
                        plugin.getBungeeConnection().forceGuildSync(guild.getId());
                        plugin.getBungeeConnection().forceAllianceSync(alliance.getId());

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
