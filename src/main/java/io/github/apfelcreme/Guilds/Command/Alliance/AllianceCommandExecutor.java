package io.github.apfelcreme.Guilds.Command.Alliance;

import io.github.apfelcreme.Guilds.Command.Alliance.Command.*;
import io.github.apfelcreme.Guilds.Command.Guild.Command.ConfirmRequestCommand;
import io.github.apfelcreme.Guilds.Command.SubCommand;
import io.github.apfelcreme.Guilds.Guilds;
import io.github.apfelcreme.Guilds.GuildsConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Alliances
 * Copyright (C) 2015 Lord36 aka Apfelcreme
 * <p>
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * @author Lord36 aka Apfelcreme on 30.05.2015.
 */
public class AllianceCommandExecutor implements CommandExecutor {

    private Guilds plugin;

    public AllianceCommandExecutor(Guilds plugin) {
        this.plugin = plugin;
    }

    /**
     * @param commandSender the sender
     * @param command       the command
     * @param s             ???
     * @param strings       the command args
     * @return ??
     */
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player sender = (Player) commandSender;
            if (sender.hasPermission("Guilds.user")) {
                SubCommand subCommand = null;
                if (strings.length > 0) {
                    AllianceOperation operation = AllianceOperation.getOperation(strings[0]);
                    if (operation != null) {
                        switch (operation) {
                            case ACCEPT:
                                subCommand = new InviteAcceptCommand();
                                break;
                            case CONFIRM:
                                subCommand = new ConfirmRequestCommand();
                                break;
                            case CREATE:
                                subCommand = new CreateCommand();
                                break;
                            case DENY:
                                subCommand = new InviteDenyCommand();
                                break;
                            case INVITE:
                                subCommand = new InviteCommand();
                                break;
                            case LEAVE:
                                subCommand = new LeaveCommand();
                                break;
                            case LIST:
                                subCommand = new ListCommand();
                                break;
                            case INFO:
                                subCommand = new InfoCommand();
                                break;
                        }
                    } else {
                        subCommand = new MenuCommand();
                    }
                } else {
                    subCommand = new MenuCommand();
                }
                subCommand.execute(sender, strings);
            } else {
                Guilds.getInstance().getChat().sendMessage(sender, GuildsConfig.getText("error.noPermission"));
            }
        }
        return false;
    }

    /**
     * all possible sub-commands for /alliance
     */
    public enum AllianceOperation {
        ACCEPT, CONFIRM, CREATE, DENY, INVITE, LEAVE, LIST, INFO;

        /**
         * returns the matching operation
         *
         * @param operationString the string
         * @return the matching enum constant or null
         */
        public static AllianceOperation getOperation(String operationString) {
            for (AllianceOperation operation : AllianceOperation.values()) {
                if (operation.name().equalsIgnoreCase(operationString)) {
                    return operation;
                }
            }
            return null;
        }

    }
}