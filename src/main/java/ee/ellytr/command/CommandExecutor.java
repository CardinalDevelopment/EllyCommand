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
import ee.ellytr.command.util.MatchError;
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
    int argsLength = args.length;

    if (argsLength > 0) {
      EllyCommand nestedCommand = Collections.getCommand(command.getNestedCommands(), args[0]);
      if (nestedCommand != null) {
        execute(nestedCommand, sender, Collections.removeFirstNode(args));
        return;
      }
    }

    List<CommandInstance> instances = new ArrayList<>();

    MatchError error = null;
    ArgumentContext invalidArgument = null;
    CommandContext cmd = new CommandContext(sender, args);
    for (CommandInstance instance : command.getInstances()) {
      boolean console = instance.isConsole(), player = instance.isPlayer();
      if (console || player) {
        if (!player && sender instanceof Player) {
          if (error == null || MatchError.PLAYER_NO_ACCESS.isPrioritizedOver(error)) {
            error = MatchError.PLAYER_NO_ACCESS;
          }
          continue;
        }
        if (!console && sender instanceof ConsoleCommandSender) {
          if (error == null || MatchError.CONSOLE_NO_ACCESS.isPrioritizedOver(error)) {
            error = MatchError.CONSOLE_NO_ACCESS;
          }
          continue;
        }
      }

      boolean skip = false;
      for (String permission : instance.getPermissions()) {
        if (!sender.hasPermission(permission)) {
          if (error == null || MatchError.NO_PERMISSION.isPrioritizedOver(error)) {
            error = MatchError.NO_PERMISSION;
          }
          skip = true;
          break;
        }
      }
      if (skip) {
        continue;
      }

      if (argsLength < instance.getMin()) {
        if (error == null || MatchError.TOO_FEW_ARGUMENTS.isPrioritizedOver(error)) {
          error = MatchError.TOO_FEW_ARGUMENTS;
        }
        continue;
      }
      if (argsLength > instance.getMax()) {
        if (error == null || MatchError.TOO_MANY_ARGUMENTS.isPrioritizedOver(error)) {
          error = MatchError.TOO_MANY_ARGUMENTS;
        }
        continue;
      }

      CommandMatch match = Argument.matchArguments(instance, cmd);
      if (match.hasOverflow()) {
        ArgumentContext invalid = null;
        for (ArgumentContext context : match.getMatches()) {
          if (context.getMatch() == null && context.getArgument().isRequired()) {
            invalid = context;
          }
        }
        if (invalid == null) {
          for (ArgumentContext context : match.getMatches()) {
            if (context.getMatch() == null) {
              invalid = context;
            }
          }
        }
        if (invalid == null) {
          if (error == null || MatchError.TOO_MANY_ARGUMENTS.isPrioritizedOver(error)) {
            error = MatchError.TOO_MANY_ARGUMENTS;
            continue;
          }
        } else {
          if (error == null || MatchError.INVALID_ARGUMENTS.isPrioritizedOver(error)) {
            error = MatchError.INVALID_ARGUMENTS;
            invalidArgument = invalid;
            continue;
          }
        }
      } else {
        for (ArgumentContext context : match.getMatches()) {
          if (context == null && context.getArgument().isRequired()) {
            skip = true;
            if (!context.isPresent()) {
              if (error == null || MatchError.TOO_FEW_ARGUMENTS.isPrioritizedOver(error)) {
                error = MatchError.TOO_FEW_ARGUMENTS;
              }
            }
            break;
          }
        }
        if (skip) {
          continue;
        }
      }

      instances.add(instance);
    }

    if (instances.isEmpty()) {
      switch (error) {
        case CONSOLE_NO_ACCESS:
          throw new CommandConsoleException();
        case PLAYER_NO_ACCESS:
          throw new CommandPlayerException();
        case NO_PERMISSION:
          throw new CommandPermissionException();
        case TOO_FEW_ARGUMENTS:
          throw new CommandUsageException(MatchError.TOO_FEW_ARGUMENTS, null);
        case TOO_MANY_ARGUMENTS:
          throw new CommandUsageException(MatchError.TOO_MANY_ARGUMENTS, null);
        case INVALID_USAGE:
          throw new CommandUsageException(MatchError.INVALID_USAGE, null);
        case INVALID_ARGUMENTS:
          throw new CommandUsageException(MatchError.INVALID_ARGUMENTS, invalidArgument);
        default:
          throw new CommandException();
      }
    }

    if (executeAllValidInstances) {
      for (CommandInstance instance : instances) {
        List<Object> parameters = toParameters(Argument.matchArguments(instance, cmd).getMatches());
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

      List<Object> parameters = toParameters(Argument.matchArguments(instance, cmd).getMatches());
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
