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
import ee.ellytr.command.argument.Argument;
import ee.ellytr.command.argument.ArgumentContext;
import ee.ellytr.command.util.Collections;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CommandTabCompleter implements TabCompleter {

  private final EllyCommand command;

  @SuppressWarnings("unchecked")
  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
    List<String> suggestions = new ArrayList<>();

    int argsLength = args.length;

    List<CommandInstance> instances = new ArrayList<>();
    for (CommandInstance instance : command.getInstances()) {
      boolean valid = true;
      boolean allArgumentsPresent = true;
      CommandMatch match = Argument.matchArguments(
          instance, new CommandContext(sender, Collections.removeLastArgument(args))
      );
      if (match.hasOverflow()) {
        valid = false;
      } else {
        for (ArgumentContext argumentContext : match.getMatches()) {
          boolean present = argumentContext.isPresent();
          if (argumentContext.getMatch() == null && present) {
            valid = false;
          }
          if (!present) {
            allArgumentsPresent = false;
          }
        }
      }
      if (allArgumentsPresent) {
        valid = false;
      }
      if (valid) {
        instances.add(instance);
      }
    }

    String argument = args[0];
    for (EllyCommand nestedCommand : command.getNestedCommands()) {
      if (argsLength == 1 && nestedCommand.getName().toLowerCase().startsWith(argument.toLowerCase())) {
        suggestions.add(nestedCommand.getName());
      } else if (argsLength > 1 && nestedCommand.getName().equalsIgnoreCase(argument)) {
        suggestions.addAll(nestedCommand.getTabCompleter().onTabComplete(sender, cmd, alias,
            Collections.removeFirstArgument(args)));
      }
    }

    for (CommandInstance instance : instances) {
      suggestions.addAll(
          instance.getArguments().get(argsLength - 1).getProvider().getSuggestions(args[argsLength - 1], sender)
      );
    }

    return suggestions;
  }

}
