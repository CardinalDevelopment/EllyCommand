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
import ee.ellytr.command.util.Commands;
import lombok.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Method;
import java.util.List;

@Data
public class CommandTabCompleter implements TabCompleter {

  private final CommandFactory factory;
  private final EllyCommand command;

  /**
   * Gets any applicable methods that go alongside this command with the given arguments
   *
   * @param args The command arguments
   * @return The list of applicable methods for this command with the arguments
   */
  public List<Method> getApplicableMethods(String[] args) {
    List<Method> applicableMethods = Lists.newArrayList();
    for (Method method : command.getMethods().values()) {
      Class[] parameterTypes = method.getParameterTypes();
      boolean applicable = true;
      for (int i = 0; i < args.length - 1; i++) {
        try {
          if (factory.getRegistry().getProviderRegistry().getProvider(parameterTypes[i + 1]).getMatch(args[i])
                  == null) {
            applicable = false;
          }
        } catch (Exception e) {
          applicable = false;
        }
      }
      if (applicable) {
        applicableMethods.add(method);
      }
    }
    return applicableMethods;
  }

  /**
   * Returns a list of suggestions for the applicable nested commands of this command.
   *
   * @param sender The sender of this command
   * @param cmd    The command to be checked for nested commands
   * @param label  The command label
   * @param args   The arguments of the original command
   * @return The tab complete suggestions for any applicable nested commands
   */
  public List<String> getNestedCommandSuggestions(CommandSender sender, Command cmd, String label, String[] args) {
    List<String> suggestions = Lists.newArrayList();
    int argsLength = args.length;
    if (argsLength == 1) {
      command.getNestedCommands().forEach(nestedCommand -> {
        String name = nestedCommand.getName();
        if (name.toLowerCase().startsWith(args[0].toLowerCase())) {
          suggestions.add(name);
        }
      });
    } else if (argsLength > 1) {
      EllyCommand nestedCommand = Commands.getCommand(command.getNestedCommands(), args[0]);
      if (nestedCommand != null) {
        List<String> newArgsList = Lists.newArrayList(args);
        newArgsList.remove(0);
        suggestions.addAll(nestedCommand.getTabCompleter().onTabComplete(sender, cmd, label,
                (String[]) newArgsList.toArray()));
      }
    }
    return suggestions;
  }

  /**
   * Returns a list of strings that are the suggestions when tab completing this command.
   *
   * @param sender The sender of this command
   * @param cmd    The command to be tab completed
   * @param label  The command label
   * @param args   The arguments that are sent with this command
   * @return The list of suggestions for this command
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    List<String> suggestions = Lists.newArrayList();
    int argsLength = args.length;

    suggestions.addAll(getNestedCommandSuggestions(sender, cmd, label, args));
    List<Method> applicableMethods = getApplicableMethods(args);

    for (Method method : applicableMethods) {
      Class[] parameterTypes = method.getParameterTypes();
      if (argsLength < parameterTypes.length) {
        suggestions.addAll(factory.getRegistry().getProviderRegistry().getProvider(parameterTypes[argsLength])
                .getSuggestions(args[argsLength - 1]));
      }
    }
    return suggestions;
  }

}
