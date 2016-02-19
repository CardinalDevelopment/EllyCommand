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

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import ee.ellytr.command.exception.CommandException;
import ee.ellytr.command.exception.CommandPermissionsException;
import ee.ellytr.command.provider.ArgumentProvider;
import ee.ellytr.command.provider.CommandProviders;
import org.apache.commons.lang.Validate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CommandFactory {

  private final CommandRegistry registry;
  private final CommandProviders providers;
  private final ImmutableBiMap<Method, EllyCommand> commands;

  protected CommandFactory(CommandRegistry registry, List<Class> classes) {
    this.registry = registry;
    this.providers = new CommandProviders();

    ImmutableBiMap.Builder<Method, EllyCommand> builder = new ImmutableBiMap.Builder<>();
    classes.forEach(clazz -> findCommands(clazz).forEach(method -> builder.put(method, registerCommand(method))));
    commands = builder.build();
  }

  private List<Method> findCommands(Class clazz) {
    List<Method> methods = Lists.newArrayList();
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.getAnnotation(Command.class) != null && Modifier.isStatic(method.getModifiers()) && method.getReturnType().equals(boolean.class)) {
        methods.add(method);
      }
    }
    return methods;
  }

  private EllyCommand registerCommand(Method method) {
    Command command = method.getAnnotation(Command.class);
    EllyCommand cmd = new EllyCommand(command.aliases(), command.description(), command.permissions(), command.min(), command.max(), command.args(), registry.getExecutor());
    registry.getCommandMap().register(registry.getPlugin().getName(), cmd);
    return cmd;
  }

  public boolean execute(EllyCommand command, CommandContext cmd) throws CommandException {
    Validate.notNull(command);
    if (command.getPermissions() != null) {
      for (String permission : command.getPermissions()) {
        if (!cmd.getSender().hasPermission(permission)) {
          throw new CommandPermissionsException();
        }
      }
    }

    Method method = commands.inverse().get(command);

    Class[] classArgs = command.getArgs();
    Object[] parameters = new Object[classArgs.length + 1];
    parameters[0] = cmd;
    String[] args = cmd.getArgs();

    for (int i = 0; i < classArgs.length; i++) {
      if (i < args.length) {
        parameters[i + 1] = providers.getProvider(classArgs[i]).getMatch(args[i]);
      } else {
        parameters[i + 1] = null;
      }
    }


    try {
      return (boolean) method.invoke(null, parameters);
    } catch (IllegalAccessException | InvocationTargetException e) {
      Logger.getLogger("EllyCommand").severe("Could not invoke command \"" + command.getName() + "\"");
      e.printStackTrace();
    }
    return false;
  }

  protected <T> void addProvider(ArgumentProvider<T> provider, Class<T> clazz) {
    providers.addProvider(provider, clazz);
  }

  protected EllyCommand getCommand(String name) {
    for (EllyCommand command : commands.values()) {
      if (command.getName().equalsIgnoreCase(name)) {
        return command;
      }
    }
    return null;
  }

}
