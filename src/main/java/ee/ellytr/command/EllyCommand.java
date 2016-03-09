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

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ee.ellytr.command.util.Commands;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class EllyCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {

  private final Multimap<CommandInfo, Method> methods;
  private final Plugin plugin;
  private final List<EllyCommand> nestedCommands;
  private CommandTabCompleter tabCompleter;

  /**
   * A class to keep track of the command. While registered, this is not actually registered in the Bukkit command map
   *
   * @param methods The map of command information to a specific method, as commands can have more than one method with
   *                varying information, such as argument length and permissions
   * @param plugin  The plugin that is used in the {@link CommandRegistry} and {@link CommandFactory}
   */
  public EllyCommand(Multimap<CommandInfo, Method> methods, Plugin plugin) {
    super(Commands.getName(methods.keySet()), Commands.getDescription(methods.keySet()),
            Commands.getUsageString(methods), Arrays.asList(Commands.getAliases(methods.keySet())));

    this.methods = methods;
    this.plugin = plugin;
    nestedCommands = Lists.newArrayList();
  }

  /**
   * Adds a nested command within this command, which would be already specified by the factory
   *
   * @param nestedCommand The nested command to add
   */
  public void addNestedCommand(EllyCommand nestedCommand) {
    nestedCommands.add(nestedCommand);
  }

  /**
   * An inherited method that is ran when the command is executed
   */
  @Override
  public boolean execute(CommandSender sender, String label, String[] args) {
    return plugin.onCommand(sender, this, label, args);
  }

  /**
   * @return The plugin that this command is using
   */
  @Override
  public Plugin getPlugin() {
    return plugin;
  }
}
