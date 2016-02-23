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
import ee.ellytr.command.util.Commands;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

          Commands.getCommandMap().register(command.getName(), command);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
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
        AlternateCommand info = method.getAnnotation(AlternateCommand.class);
        Class[] parameters = method.getParameterTypes();
        if (command.getName().equals(method.getName())
                && info != null
                && Modifier.isStatic(method.getModifiers())
                && method.getReturnType().equals(void.class)
                && parameters.length > 0 && parameters[0].equals(CommandContext.class)) {
          methods.add(method);
        }
      });
    }
    return commands;
  }

  public EllyCommand getCommand(String name) {
    for (EllyCommand command : commands) {
      if (command.getName().equalsIgnoreCase(name)) {
        return command;
      }
    }
    return null;
  }

  public EllyCommand getCommand(List<Method> methods) {
    ImmutableMultimap.Builder<CommandInfo, Method> builder = ImmutableMultimap.builder();
    Command commandAnn = methods.get(0).getAnnotation(Command.class);
    for (Method method : methods) {
      Command methodCommandAnn = method.getAnnotation(Command.class);
      AlternateCommand altCommandAnn = method.getAnnotation(AlternateCommand.class);
      CommandInfo info;
      if (altCommandAnn != null) {
        info = new CommandInfo(commandAnn.aliases(), commandAnn.description(), altCommandAnn.permissions(), altCommandAnn.min(), altCommandAnn.max());
      } else {
        if (methodCommandAnn != null) {
          info = new CommandInfo(commandAnn.aliases(), commandAnn.description(), methodCommandAnn.permissions(), methodCommandAnn.min(), methodCommandAnn.max());
        } else {
          info = new CommandInfo(commandAnn.aliases(), commandAnn.description(), commandAnn.permissions(), commandAnn.min(), commandAnn.max());
        }
      }
      builder.put(info, method);
    }

    ImmutableMultimap<CommandInfo, Method> commandMethods = builder.build();
    Plugin plugin = registry.getPlugin();
    EllyCommand command = new EllyCommand(commandMethods, plugin);
    command.setTabCompleter(new CommandTabCompleter(this, command));
    return command;
  }

  public List<EllyCommand> getParentCommands(EllyCommand nestedCommand) {
    return commands.stream().filter(command -> command.getNestedCommands().contains(nestedCommand)).collect(Collectors.toList());
  }

  public List<String> getPermissions(EllyCommand command) {
    List<String> permissions = Lists.newArrayList();
    for (CommandInfo info : command.getMethods().keySet()) {
      for (String permission : info.getPermissions()) {
        permissions.add(permission);
      }
    }
    List<EllyCommand> parents = getParentCommands(command);
    if (!parents.isEmpty()) {
      for (EllyCommand parent : parents) {
        permissions.addAll(getPermissions(parent));
      }
    }
    return permissions;
  }

}
