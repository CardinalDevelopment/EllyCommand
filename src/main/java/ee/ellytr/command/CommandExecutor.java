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

import ee.ellytr.command.argument.Argument;
import ee.ellytr.command.argument.ArgumentContext;
import ee.ellytr.command.exception.CommandConsoleException;
import ee.ellytr.command.exception.CommandException;
import ee.ellytr.command.exception.CommandNestedException;
import ee.ellytr.command.exception.CommandPermissionException;
import ee.ellytr.command.exception.CommandPlayerException;
import ee.ellytr.command.exception.CommandUsageException;
import ee.ellytr.command.util.Collections;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class CommandExecutor {

  private final CommandFactory factory;
  private boolean executeAllValidInstances = false;

  public void execute(String name, CommandSender sender, String[] args) throws CommandException {
    execute(factory.getCommand(name), sender, args);
  }

  private void execute(EllyCommand command, CommandSender sender, String[] args) throws CommandException {
    // Execute any valid nested commands
    if (args.length > 0) {
      EllyCommand nestedCommand = Collections.getCommand(command.getNestedCommands(), args[0]);
      if (nestedCommand != null) {
        execute(nestedCommand, sender, Collections.removeFirstArgument(args));
        return;
      }
    }

    CommandContext context = new CommandContext(sender, args);

    List<CommandInstance> instances = new ArrayList<>();
    boolean noPermission = false, consoleNoUse = false,
        playerNoUse = false, tooFewArguments = false, tooManyArguments = false;
    ArgumentContext invalidArgument = null;
    for (CommandInstance instance : command.getInstances()) {
      int argsLength = args.length;
      if (argsLength < instance.getMin()) {
        tooFewArguments = true;
        continue;
      }
      if (argsLength > instance.getMax()) {
        tooManyArguments = true;
        continue;
      }
      boolean console = instance.isConsole(), player = instance.isPlayer();
      if (console || player) {
        if (!player && sender instanceof Player) {
          playerNoUse = true;
          continue;
        }
        if (!console && sender instanceof ConsoleCommandSender) {
          consoleNoUse = true;
          continue;
        }
      }
      boolean skip = false;
      for (String permission : instance.getPermissions()) {
        if (!sender.hasPermission(permission)) {
          noPermission = true;
          skip = true;
        }
      }
      if (skip) {
        continue;
      }

      CommandMatch match = Argument.matchArguments(instance, context);
      if (match.hasOverflow()) {
        ArgumentContext currentInvalid = null;
        for (ArgumentContext argument : match.getMatches()) {
          if (argument.getMatch() == null && (argument.getArgument().isRequired() && argument.isPresent())) {
            currentInvalid = argument;
            break;
          }
        }
        if (currentInvalid != null) {
          invalidArgument = currentInvalid;
        } else {
          tooManyArguments = true;
        }
        continue;
      } else {
        for (ArgumentContext argument : match.getMatches()) {
          if (argument.getMatch() == null && argument.getArgument().isRequired()) {
            skip = true;

            if (!argument.isPresent()) {
              tooFewArguments = true;
            }
          }
        }
        if (skip) {
          continue;
        }
      }

      instances.add(instance);
    }
    if (instances.isEmpty()) {
      if (playerNoUse) {
        throw new CommandPlayerException();
      } else if (consoleNoUse) {
        throw new CommandConsoleException();
      } else if (noPermission) {
        throw new CommandPermissionException();
      } else if (invalidArgument != null) {
        throw new CommandUsageException(CommandUsageException.Error.INVALID_ARGUMENTS, invalidArgument);
      } else {
        CommandUsageException.Error error = CommandUsageException.Error.INVALID_USAGE;
        if (tooFewArguments && !tooManyArguments) {
          error = CommandUsageException.Error.TOO_FEW_ARGUMENTS;
        } else if (tooManyArguments && !tooFewArguments) {
          error = CommandUsageException.Error.TOO_MANY_ARGUMENTS;
        }
        throw new CommandUsageException(error, null);
      }
    }
    CommandContext cmd = new CommandContext(sender, args);
    if (executeAllValidInstances) {
      for (CommandInstance instance : instances) {
        List<Object> parameters = toParameters(Argument.matchArguments(instance, context).getMatches());
        parameters.add(0, cmd);
        try {
          instances.get(0).getMethod().invoke(null, parameters);
        } catch (InvocationTargetException e) {
          Throwable cause = e.getCause();
          if (cause instanceof CommandException) {
            throw (CommandException) cause;
          } else {
            Logger.getLogger("EllyCommand").warning("Could not execute command \"" + command.getName() + "\"");
            e.printStackTrace();
          }
        } catch (IllegalAccessException e) {
          Logger.getLogger("EllyCommand").warning("Could not execute command \"" + command.getName() + "\"");
          e.printStackTrace();
        }
      }
    } else {
      CommandInstance instance = instances.get(0);

      List<Object> parameters = toParameters(Argument.matchArguments(instance, context).getMatches());
      parameters.add(0, cmd);

      try {
        instances.get(0).getMethod().invoke(null, parameters.toArray(new Object[parameters.size()]));
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof CommandException) {
          throw (CommandException) cause;
        } else {
          throw new CommandNestedException(cause);
        }
      } catch (IllegalAccessException e) {
        Logger.getLogger("EllyCommand").warning("Could not execute command \"" + command.getName() + "\"");
        e.printStackTrace();
      }
    }
  }

  private List<Object> toParameters(@NonNull List<ArgumentContext> match) {
    return match.stream().map(ArgumentContext::getMatch).collect(Collectors.toList());
  }

}
