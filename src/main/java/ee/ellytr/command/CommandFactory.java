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
import ee.ellytr.command.argument.Optional;
import ee.ellytr.command.util.Collections;
import ee.ellytr.command.util.Commands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CommandFactory {

  private final CommandRegistry registry;
  private final List<EllyCommand> commands = Lists.newArrayList();
  private final CommandExecutor executor = new CommandExecutor(this);

  protected void build() {
    for (Class clazz : registry.getClasses()) {
      // Builds all commands from the registry class's command methods
      commands.addAll(findCommandMethods(clazz).stream().map(method
          -> analyzeCommand(method.getDeclaredAnnotation(Command.class), method)).collect(Collectors.toList()));
    }
    for (EllyCommand command : commands) {
      for (CommandInstance instance : command.getInstances()) {
        NestedCommands nestedCommands = instance.getMethod().getDeclaredAnnotation(NestedCommands.class);
        if (nestedCommands != null) {
          for (Class clazz : nestedCommands.value()) {
            // Builds all commands from the nested class's command methods
            for (Method method : findCommandMethods(clazz)) {
              command.addNestedCommand(analyzeCommand(method.getDeclaredAnnotation(Command.class), method));
            }
          }
        }
      }
    }
    register();
  }

  private void register() {
    for (EllyCommand command : commands) {
      try {
        Constructor constructor = Class.forName(PluginCommand.class.getName()).getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);

        Plugin plugin = registry.getPlugin();

        PluginCommand pluginCommand = (PluginCommand) constructor.newInstance(command.getName(), plugin);
        pluginCommand.setAliases(command.getAliases());
        pluginCommand.setDescription(command.getDescription());
        pluginCommand.setExecutor(plugin);
        pluginCommand.setTabCompleter(command.getTabCompleter());
        pluginCommand.setUsage(command.getUsage());

        Commands.getCommandMap().register(plugin.getName(), pluginCommand);
      } catch (InstantiationException | InvocationTargetException | IllegalAccessException
          | NoSuchMethodException | ClassNotFoundException e) {
        Logger.getLogger("EllyCommand").severe("Could not register command \"" + command.getName() + "\"");
      }
    }
  }

  private EllyCommand analyzeCommand(Command command, Method method) {
    List<String> aliases = Lists.newArrayList(command.aliases());
    CommandInstance instance = new CommandInstance(command.min(), command.max(), command.permissions(), command.usage(),
        method.getDeclaredAnnotation(ConsoleCommand.class) != null,
        method.getDeclaredAnnotation(PlayerCommand.class) != null,
        getArguments(method), method);
    for (EllyCommand ellyCommand : commands) {
      if (Collections.getIntersection(aliases, ellyCommand.getAliases()).size() >= 1) {
        // Add any additional aliases to the command
        aliases.stream().filter(alias -> !ellyCommand.hasAlias(alias)).forEach(ellyCommand::addAlias);

        ellyCommand.addInstance(instance);

        return ellyCommand;
      }
    }

    // If no commands with matching aliases have been found, then make a new command
    EllyCommand ellyCommand = new EllyCommand(command);
    ellyCommand.addInstance(instance);
    return ellyCommand;
  }

  @SuppressWarnings("unchecked")
  private List<Argument> getArguments(Method method) {
    Class[] parameters = method.getParameterTypes();
    List<Argument> arguments = Lists.newArrayList();
    for (int i = 1; i < parameters.length; i++) {
      Argument argument;
      List<Integer> multiArgs = Argument.getMultiArgs(method, i);
      Optional optional = method.getParameters()[i].getAnnotation(Optional.class);
      String defaultValue = null;
      if (optional != null) {
        defaultValue = optional.defaultValue();
      }
      if (multiArgs != null) {
        if (i + 1 == parameters.length) {
          argument = new Argument(Argument.isRequired(method.getParameterAnnotations()[i]), defaultValue,
              multiArgs.get(0), multiArgs.get(1), registry.getProviderRegistry().getProvider(parameters[i]));
        } else {
          Logger.getLogger("EllyCommand").warning("Invalid position of MultiArgs for command");
          argument = new Argument(Argument.isRequired(method.getParameterAnnotations()[i]), defaultValue,
              1, 1, registry.getProviderRegistry().getProvider(parameters[i]));
        }
      } else {
        argument = new Argument(Argument.isRequired(method.getParameterAnnotations()[i]), defaultValue,
            1, 1, registry.getProviderRegistry().getProvider(parameters[i]));
      }
      arguments.add(argument);
    }
    return arguments;
  }

  private List<Method> findCommandMethods(Class clazz) {
    List<Method> methods = Lists.newArrayList();
    for (Method method : clazz.getDeclaredMethods()) {
      Command command = method.getDeclaredAnnotation(Command.class);
      Class[] parameters = method.getParameterTypes();
      if (command != null
          && Modifier.isStatic(method.getModifiers())
          && method.getReturnType().equals(void.class)
          && parameters.length >= 1 && parameters[0].equals(CommandContext.class)) {
        methods.add(method);
      }
    }
    return methods;
  }

  protected EllyCommand getCommand(String name) {
    return Collections.getCommand(commands, name);
  }

}
