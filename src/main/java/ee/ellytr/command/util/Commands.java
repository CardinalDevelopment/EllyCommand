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

package ee.ellytr.command.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ee.ellytr.command.command.AlternateCommand;
import ee.ellytr.command.command.Command;
import ee.ellytr.command.CommandInfo;
import ee.ellytr.command.command.ConsoleCommand;
import ee.ellytr.command.EllyCommand;
import ee.ellytr.command.argument.Optional;
import ee.ellytr.command.command.PlayerCommand;
import ee.ellytr.command.argument.Required;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

public class Commands {

  public static String[] getAliases(Set<CommandInfo> info) {
    return Lists.newArrayList(info).get(0).getAliases();
  }

  public static EllyCommand getCommand(Collection<EllyCommand> commands, String name) {
    for (EllyCommand command : commands) {
      if (command.getName().equalsIgnoreCase(name)) {
        return command;
      }
    }
    return null;
  }

  public static CommandMap getCommandMap() {
    return ReflectionUtil.getField(Bukkit.getPluginManager(), "commandMap");
  }

  public static String getDescription(Set<CommandInfo> info) {
    return Lists.newArrayList(info).get(0).getDescription();
  }

  public static String getName(Method method) {
    Command command = method.getAnnotation(Command.class);
    if (command != null) {
      return command.aliases()[0];
    }
    AlternateCommand altCommand = method.getAnnotation(AlternateCommand.class);
    if (altCommand != null) {
      return altCommand.aliases()[0];
    }
    return null;
  }

  public static String getUsageString(Multimap<CommandInfo, Method> methods) {
    String usage = "/" + getName(methods.keySet());
    boolean firstUsage = true;
    for (CommandInfo info : methods.keySet()) {
      for (Method method : methods.get(info)) {
        if (firstUsage) {
          firstUsage = false;
        } else {
          usage += ",";
        }
        Class[] parameters = method.getParameterTypes();
        for (int i = 1; i < parameters.length; i++) {
          if (i <= info.getMin() || Commands.isRequiredParameter(method.getParameterAnnotations()[i])) {
            usage += " <" + parameters[i].getSimpleName() + ">";
          } else {
            usage += " [" + parameters[i].getSimpleName() + "]";
          }
        }
      }
    }
    return usage;
  }

  public static String getName(Set<CommandInfo> info) {
    return Lists.newArrayList(info).get(0).getAliases()[0];
  }

  public static boolean isRequiredParameter(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(Optional.class)) {
        return false;
      }
      if (annotation.annotationType().equals(Required.class)) {
        return true;
      }
    }
    return true;
  }

  public static boolean isConsoleCommand(Method method) {
    for (Annotation annotation : method.getDeclaredAnnotations()) {
      if (annotation.annotationType().equals(ConsoleCommand.class)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPlayerCommand(Method method) {
    for (Annotation annotation : method.getDeclaredAnnotations()) {
      if (annotation.annotationType().equals(PlayerCommand.class)) {
        return true;
      }
    }
    return false;
  }

}
