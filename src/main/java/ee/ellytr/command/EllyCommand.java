/*
 * This file is part of EllyCommand.
 *
 * EllyCommand is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EllyCommand is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with EllyCommand.  If not, see <http://www.gnu.org/licenses/>.
 */
package ee.ellytr.command;

import com.google.common.collect.Multimap;
import ee.ellytr.command.util.Commands;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.Arrays;

@Getter
public class EllyCommand extends org.bukkit.command.Command {


  private Multimap<CommandInfo, Method> methods;
  private final CommandExecutor executor;

  public EllyCommand(Multimap<CommandInfo, Method> methods, CommandExecutor executor) {
    super(Commands.getName(methods.keySet()), Commands.getDescription(methods.keySet()), Commands.getUsageString(methods), Arrays.asList(Commands.getAliases(methods.keySet())));

    this.methods = methods;
    this.executor = executor;
  }

  @Override
  public boolean execute(CommandSender sender, String label, String[] args) {
    return executor.onCommand(sender, this, label, args);
  }

}
