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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

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
        ImmutableMultimap.Builder<CommandInfo, Method> builder = ImmutableMultimap.builder();
        Command commandAnn = methods.get(0).getAnnotation(Command.class);
        for (Method method : methods) {
          AlternateCommand altCommandAnn = method.getAnnotation(AlternateCommand.class);
          CommandInfo info;
          if (altCommandAnn != null) {
            info = new CommandInfo(commandAnn.aliases(), commandAnn.description(), altCommandAnn.permissions(), altCommandAnn.min(), altCommandAnn.max());
          } else {
            info = new CommandInfo(commandAnn.aliases(), commandAnn.description(), commandAnn.permissions(), commandAnn.min(), commandAnn.max());
          }
          builder.put(info, method);
        }
        Plugin plugin = registry.getPlugin();
        EllyCommand command = new EllyCommand(builder.build(), plugin);
        Bukkit.getCommandMap().register(plugin.getName(), command);
        commands.add(command);
      });
    });
  }

  public List<List<Method>> findCommands(Class clazz) {
    List<List<Method>> commands = Lists.newArrayList();
    for (Method method : clazz.getDeclaredMethods()) {
      Class[] parameters = method.getParameterTypes();
      Command info = method.getAnnotation(Command.class);
      if (info != null
              && Modifier.isStatic(method.getModifiers())
              && method.getReturnType().equals(boolean.class)
              && parameters.length > 0 && parameters[0].equals(CommandContext.class)) {
        commands.add(Lists.newArrayList(method));
      }
    }
    for (Method method : clazz.getDeclaredMethods()) {
      commands.forEach(methods -> {
        if (methods.get(0).getName().equals(method.getName())) {
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

}
