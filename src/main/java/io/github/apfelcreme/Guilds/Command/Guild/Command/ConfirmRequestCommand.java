package io.github.apfelcreme.Guilds.Command.Guild.Command;

import io.github.apfelcreme.Guilds.Command.Request;
import io.github.apfelcreme.Guilds.Command.SubCommand;
import io.github.apfelcreme.Guilds.Guilds;
import io.github.apfelcreme.Guilds.GuildsConfig;
import io.github.apfelcreme.Guilds.Manager.RequestController;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
 * @author Lord36 aka Apfelcreme on 10.05.2015.
 */
public class ConfirmRequestCommand extends SubCommand {

    public ConfirmRequestCommand(Guilds plugin) {
        super(plugin);
    }

    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    public void execute(CommandSender commandSender, String[] strings) {
        Player sender = (Player) commandSender;
        if (sender.hasPermission("Guilds.confirm")) {
            Request request = plugin.getRequestController().getRequest(sender);
            if (request != null) {
                request.execute();
                plugin.getRequestController().removeRequest(sender);
            } else {
                plugin.getChat().sendMessage(sender, plugin.getGuildsConfig().getText("error.noRequest"));
            }
        } else {
            plugin.getChat().sendMessage(sender, plugin.getGuildsConfig().getText("error.noPermission"));
        }
    }
}
