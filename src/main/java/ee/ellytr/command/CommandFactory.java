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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import ee.ellytr.command.command.AlternateCommand;
import ee.ellytr.command.command.Command;
import ee.ellytr.command.command.CommandPermissions;
import ee.ellytr.command.command.NestedCommands;
import ee.ellytr.command.util.Commands;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
public class CommandFactory {

  private final CommandRegistry registry;
  private final CommandExecutor executor;
  private final List<EllyCommand> commands;

  protected CommandFactory(CommandRegistry registry) {
    this.registry = registry;
    executor = new CommandExecutor(this);
    commands = Lists.newArrayList();
  }

  /**
   * Builds the commands by finding the command methods in each class and registering them in the Bukkit command map.
   */
  public void build() {
    registry.getClasses().forEach(clazz -> {
      findCommands(clazz).forEach(methods -> {
        EllyCommand ellyCommand = getCommand(methods);
        commands.add(ellyCommand);
        try {
          Constructor constructor = Class.forName(PluginCommand.class.getName()).getDeclaredConstructor(String.class, Plugin.class);
          constructor.setAccessible(true);
          Plugin plugin = registry.getPlugin();
          PluginCommand command = (PluginCommand) constructor.newInstance(ellyCommand.getName(), plugin);
          command.setAliases(ellyCommand.getAliases());
          command.setDescription(ellyCommand.getDescription());
          command.setExecutor(plugin);
          command.setUsage(ellyCommand.getUsage());
          command.setTabCompleter(ellyCommand.getTabCompleter());

          Commands.getCommandMap().register(plugin.getName(), command);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
          Logger.getLogger("EllyCommand").warning("Could not register PluginCommand for command \"" + ellyCommand.getName() + "\"");
          e.printStackTrace();
        }
      });
    });
    for (EllyCommand command : commands) {
      for (Method method : command.getMethods().values()) {
        NestedCommands nestedCommandsAnn = method.getAnnotation(NestedCommands.class);
        if (nestedCommandsAnn != null) {
          for (Class clazz : nestedCommandsAnn.value()) {
            findCommands(clazz).forEach(methods -> command.addNestedCommand(getCommand(methods)));
          }
        }
      }
    }
  }

  /**
   * Gets a list of methods that represent commands within a specified class. The methods are grouped by command name,
   * so if two methods refer to the same command name they will be within the same sub-list.
   *
   * @param clazz The class that is to be looked through for command methods
   * @return The grouped list of commands found in the class
   */
  public List<List<Method>> findCommands(Class clazz) {
    List<List<Method>> commands = Lists.newArrayList();
    List<Method> classMethods = Lists.newArrayList(clazz.getDeclaredMethods());
    Collections.reverse(classMethods);
    for (Method method : classMethods) {
      Command info = method.getAnnotation(Command.class);
      Class[] parameters = method.getParameterTypes();
      if (info != null
              && Modifier.isStatic(method.getModifiers())
              && method.getReturnType().equals(void.class)
              && parameters.length > 0 && parameters[0].equals(CommandContext.class)) {
        boolean newList = true;
        for (List<Method> methods : commands) {
          String name = Commands.getName(methods.get(0));
          if (name != null && name.equalsIgnoreCase(info.aliases()[0])) {
            methods.add(method);
            newList = false;
          }
        }
        if (newList) {
          commands.add(Lists.newArrayList(method));
        }
      }
    }
    for (Method method : clazz.getDeclaredMethods()) {
      commands.forEach(methods -> {
        Method command = methods.get(0);
        Command methodCommand = method.getAnnotation(Command.class);
        AlternateCommand info = method.getAnnotation(AlternateCommand.class);
        Class[] parameters = method.getParameterTypes();
        String name = Commands.getName(method);
        if (info != null
                && (command.getName().equals(method.getName()) || (name != null && info.aliases()[0].equalsIgnoreCase(name)))
                && Modifier.isStatic(method.getModifiers())
                && method.getReturnType().equals(void.class)
                && parameters.length > 0 && parameters[0].equals(CommandContext.class)) {
          methods.add(method);
        }
      });
    }
    return commands;
  }

  /**
   * Gets a command based on a list of methods with the same name given by the command annotation.
   *
   * @param methods The list of methods that share a command.
   * @return The command that is built from the method list.
   */
  public EllyCommand getCommand(List<Method> methods) {
    ImmutableMultimap.Builder<CommandInfo, Method> builder = ImmutableMultimap.builder();
    Command commandAnn = methods.get(0).getAnnotation(Command.class);
    for (Method method : methods) {
      Command methodCommand = method.getAnnotation(Command.class);
      CommandPermissions commandPermissions = method.getAnnotation(CommandPermissions.class);
      AlternateCommand alternateCommand = method.getAnnotation(AlternateCommand.class);
      boolean alternate = alternateCommand != null;

      String[] aliases = (alternate ? commandAnn : methodCommand).aliases();
      String description = (alternate ? commandAnn : methodCommand).description();
      List<String> permissions = Lists.newArrayList();
      if (alternate) {
        permissions.addAll(Arrays.asList(alternateCommand.permissions()));
      } else {
        permissions.addAll(Arrays.asList(methodCommand.permissions()));
      }
      if (commandPermissions != null) {
        permissions.addAll(Arrays.asList(commandPermissions.value()));
      }

      int min = alternate ? alternateCommand.min() : methodCommand.min();
      int max = alternate ? alternateCommand.max() : methodCommand.max();

      builder.put(new CommandInfo(aliases, description, permissions.toArray(new String[permissions.size()]), min, max), method);
    }

    ImmutableMultimap<CommandInfo, Method> commandMethods = builder.build();
    EllyCommand command = new EllyCommand(commandMethods, registry.getPlugin());
    command.setTabCompleter(new CommandTabCompleter(this, command));
    return command;
  }

  /**
   * Gets a command based on a given name.
   *
   * @param name The name to get the command by
   * @return The command found with the specified name
   */
  public EllyCommand getCommand(String name) {
    for (EllyCommand command : commands) {
      if (command.getName().equalsIgnoreCase(name)) {
        return command;
      }
    }
    return null;
  }

  public EllyCommand getCommand(Method method) {
    for (EllyCommand command : commands) {
      if (command.getMethods().values().contains(method)) {
        return command;
      }
    }
    return null;
  }

  /**
   * Gets a parent command by searching through the commands for the nested command.
   *
   * @param nestedCommand The command to get the parent of.
   * @return The parent of the nested command.
   */
  public List<EllyCommand> getParentCommands(EllyCommand nestedCommand) {
    return commands.stream().filter(command -> command.getNestedCommands().contains(nestedCommand))
            .collect(Collectors.toList());
  }

  /**
   * Gets the list of permissions for a command method.
   *
   * @param method The command method to get the permissions of.
   * @return The list of permissions for this command method.
   */
  public List<String> getPermissions(Method method) {
    List<String> permissions = Lists.newArrayList();
    EllyCommand command = getCommand(method);
    command.getMethods().keySet().stream().filter(info -> command.getMethods().get(info).contains(method)).forEach(info -> permissions.addAll(Arrays.asList(info.getPermissions())));
    List<EllyCommand> parents = getParentCommands(command);
    if (!parents.isEmpty()) {
      for (EllyCommand parent : parents) {
        permissions.addAll(getPermissions(parent));
      }
    }
    return permissions;
  }

  /**
   * Gets the list of permissions for a command.
   *
   * @param command The command to get the permissions of.
   * @return The list of permissions for this command.
   */
  public List<String> getPermissions(EllyCommand command) {
    List<String> permissions = Lists.newArrayList();
    for (Method method : command.getMethods().values()) {
      permissions.addAll(getPermissions(method));
    }
    return permissions;
  }

}
