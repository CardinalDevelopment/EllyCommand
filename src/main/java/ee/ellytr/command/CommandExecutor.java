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
import ee.ellytr.command.exception.CommandConsoleNoUseException;
import ee.ellytr.command.exception.CommandException;
import ee.ellytr.command.exception.CommandPermissionsException;
import ee.ellytr.command.exception.CommandPlayerNoUseException;
import ee.ellytr.command.exception.CommandUsageException;
import ee.ellytr.command.argument.provider.ArgumentProvider;
import ee.ellytr.command.util.Commands;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public class CommandExecutor {

  private final CommandFactory factory;

  /**
   * Executes a command.
   *
   * @param name   The name of the command.
   * @param sender The sender of the command.
   * @param args   The arguments specified with this command.
   * @throws CommandException Thrown in the nested execute command.
   */
  public void execute(String name, CommandSender sender, String[] args) throws CommandException {
    execute(factory.getCommand(name), sender, args);
  }

  /**
   * Executes a command.
   *
   * @param command The command to be executed.
   * @param sender  The sender of this command.
   * @param args    The arguments specified with this command.
   * @throws CommandException Thrown if there is any user-specified issues with the command and its execution.
   */
  public void execute(EllyCommand command, CommandSender sender, String[] args) throws CommandException {
    int argsLength = args.length;

    List<EllyCommand> nestedCommands = command.getNestedCommands();
    if (!nestedCommands.isEmpty() && args.length > 0) {
      EllyCommand nestedCommand = Commands.getCommand(nestedCommands, args[0]);
      if (nestedCommand != null) {
        List<String> newArgsList = Lists.newArrayList(args);
        newArgsList.remove(0);
        int size = newArgsList.size();
        String[] newArgs = new String[size];
        for (int i = 0; i < size; i++) {
          newArgs[i] = newArgsList.get(i);
        }
        execute(nestedCommand, sender, newArgs);
        return;
      }
    }

    Multimap<CommandInfo, Method> methods = command.getMethods();

    List<CommandInfo> applicableCommands = methods.keySet().stream().filter(info -> argsLength >= info.getMin() && argsLength <= info.getMax()).collect(Collectors.toList());

    if (applicableCommands.isEmpty()) {
      throw new CommandUsageException();
    }

    CommandContext cmd = new CommandContext(sender, args);

    boolean consoleNoUse = false, playerNoUse = false, noPermission = false;
    Method method = null;
    int nullParameters = Integer.MAX_VALUE;
    Object[] parameters = null;
    for (CommandInfo currentInfo : applicableCommands) {
      for (Method currentMethod : methods.get(currentInfo)) {
        boolean valid = true;

        boolean console = Commands.isConsoleCommand(currentMethod), player = Commands.isPlayerCommand(currentMethod);
        if ((console || player) && !((console && sender instanceof ConsoleCommandSender) || (player && sender instanceof Player))) {
          if (console && !(sender instanceof ConsoleCommandSender)) {
            consoleNoUse = true;
          }
          if (player && !(sender instanceof Player)) {
            playerNoUse = true;
          }
          continue;
        }

        boolean nextMethod = false;
        for (String permission : factory.getPermissions(currentMethod)) {
          if (!sender.hasPermission(permission)) {
            noPermission = true;
            nextMethod = true;
            break;
          }
        }
        if (nextMethod) {
          continue;
        }

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
      if (noPermission) {
        throw new CommandPermissionsException();
      }
      if (consoleNoUse) {
        throw new CommandConsoleNoUseException();
      }
      if (playerNoUse) {
        throw new CommandPlayerNoUseException();
      }
      throw new CommandUsageException();
    }

    try {
      method.invoke(null, parameters);
    } catch (IllegalAccessException | InvocationTargetException e) {
      Logger.getLogger("EllyCommand").severe("Could not invoke command \"" + command.getName() + "\"");
      e.printStackTrace();
    }
  }

}
