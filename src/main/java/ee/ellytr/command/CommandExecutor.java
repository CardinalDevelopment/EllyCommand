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
import ee.ellytr.command.exception.CommandException;
import ee.ellytr.command.exception.CommandUsageException;
import ee.ellytr.command.provider.ArgumentProvider;
import ee.ellytr.command.util.Commands;
import lombok.Data;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public class CommandExecutor {

  private final CommandFactory factory;

  public void execute(String name, CommandSender sender, String[] args) throws CommandException {
    EllyCommand command = factory.getCommand(name);
    Multimap<CommandInfo, Method> methods = command.getMethods();

    int argsLength = args.length;
    List<CommandInfo> applicableCommands = methods.keySet().stream().filter(info -> argsLength >= info.getMin() && argsLength <= info.getMax()).collect(Collectors.toList());

    if (applicableCommands.isEmpty()) {
      throw new CommandUsageException();
    }

    CommandContext cmd = new CommandContext(sender, args);

    Method method = null;
    int nullParameters = Integer.MAX_VALUE;
    Object[] parameters = null;
    for (CommandInfo info : applicableCommands) {
      for (Method currentMethod : methods.get(info)) {
        boolean valid = true;
        Class[] parameterTypes = currentMethod.getParameterTypes();
        int currentNullParameters = 0;
        Object[] currentParameters = new Object[parameterTypes.length];
        currentParameters[0] = cmd;
        for (int i = 1; i < parameterTypes.length; i++) {
          boolean required = Commands.isRequiredParameter(currentMethod.getParameterAnnotations()[i]);
          if (i > argsLength) {
            if (required) {
              valid = false;
              break;
            }
            currentNullParameters++;
            currentParameters[i] = null;
            continue;
          }
          Class parameterClass = parameterTypes[i];
          ArgumentProvider provider = factory.getRegistry().getProviderRegistry().getProvider(parameterClass);
          try {
            Object parameter = provider.getMatch(args[i - 1]);
            currentParameters[i] = parameter;
            if (parameter == null) {
              if (required) {
                valid = false;
                break;
              }
              currentNullParameters++;
            }
          } catch (Exception e) {
            if (required) {
              valid = false;
              break;
            }
            currentNullParameters++;
            currentParameters[i] = null;
          }
        }
        if (currentNullParameters < nullParameters && valid) {
          method = currentMethod;
          nullParameters = currentNullParameters;
          parameters = currentParameters;
        }
      }
    }

    if (method == null) {
      Logger.getLogger("EllyCommand").severe("Could not retrieve method for command \"" + command.getName() + "\"");
      return;
    }
    try {
      method.invoke(null, parameters);
    } catch (IllegalAccessException | InvocationTargetException e) {
      Logger.getLogger("EllyCommand").severe("Could not invoke command \"" + command.getName() + "\"");
      e.printStackTrace();
    }
  }

}
