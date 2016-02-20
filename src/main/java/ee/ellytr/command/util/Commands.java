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
import ee.ellytr.command.Command;
import ee.ellytr.command.CommandInfo;

import java.lang.reflect.Method;
import java.util.Set;

public class Commands {

  public static String getName(Set<CommandInfo> info) {
    return Lists.newArrayList(info).get(0).getAliases()[0];
  }

  public static String getName(Method method) {
    Command command = method.getAnnotation(Command.class);
    if (command != null) {
      return command.aliases()[0];
    }
    return null;
  }

  public static String getDescription(Set<CommandInfo> info) {
    return Lists.newArrayList(info).get(0).getDescription();
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
        for (int i = 1; i < parameters.length; i ++) {
          if (i <= info.getMin()) {
            usage += " <" + parameters[i].getSimpleName() + ">";
          } else {
            usage += " [" + parameters[i].getSimpleName() + "]";
          }
        }
      }
    }
    return usage;
  }

  public static String[] getAliases(Set<CommandInfo> info) {
    return Lists.newArrayList(info).get(0).getAliases();
  }

}
