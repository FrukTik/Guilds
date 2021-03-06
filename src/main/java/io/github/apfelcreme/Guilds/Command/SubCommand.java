package io.github.apfelcreme.Guilds.Command;

import io.github.apfelcreme.Guilds.Guilds;
import org.bukkit.command.CommandSender;

/**
 * Guilds
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
 * @author Lord36 aka Apfelcreme on 26.04.2015.
 */
public abstract class SubCommand {

    protected Guilds plugin;

    public SubCommand(Guilds plugin) {
        this.plugin = plugin;
    }

    /**
     * executes the command
     * @param commandSender the sender
     * @param strings the command args
     */
    public abstract void execute(CommandSender commandSender, String[] strings);
}
