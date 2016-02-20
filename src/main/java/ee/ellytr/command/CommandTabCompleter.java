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

  @SuppressWarnings("unchecked")
  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    List<Method> applicableMethods = Lists.newArrayList();
    int argsLength = args.length;
    for (Method method : command.getMethods().values()) {
      Class[] parameterTypes = method.getParameterTypes();
      boolean applicable = true;
      for (int i = 0; i < argsLength; i++) {
        try {
          if (factory.getRegistry().getProviderRegistry().getProvider(parameterTypes[i + 1]).getMatch(args[i]) == null) {
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
    List<String> suggestions = Lists.newArrayList();
    for (Method method : applicableMethods) {
      Class[] parameterTypes = method.getParameterTypes();
      if (argsLength < parameterTypes.length) {
        suggestions.addAll(factory.getRegistry().getProviderRegistry().getProvider(parameterTypes[argsLength]).getSuggestions(args[argsLength - 1]));
      }
    }
    return suggestions;
  }

}
