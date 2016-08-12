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
import ee.ellytr.command.EllyCommand;
import lombok.NonNull;

import java.util.List;

public class Collections {

  public static <T> List<T> getIntersection(List<T> list1, List<T> list2) {
    List<T> intersection = Lists.newArrayList(list1);
    intersection.retainAll(list2);
    return intersection;
  }

  public static EllyCommand getCommand(List<EllyCommand> commands, String name) {
    for (EllyCommand command : commands) {
      for (String alias : command.getAliases()) {
        if (alias.equalsIgnoreCase(name)) {
          return command;
        }
      }
    }
    return null;
  }

  public static String[] removeFirstNode(@NonNull String[] array) {
    int length = array.length;

    if (length == 0) {
      return new String[0];
    }

    String[] to = new String[length - 1];
    for (int i = 1; i < length; i++) {
      to[i - 1] = array[i];
    }
    return to;
  }

  public static String[] removeLastNode(@NonNull String[] array) {
    int length = array.length;

    if (length == 0) {
      return new String[0];
    }

    length--;
    String[] to = new String[length];
    for (int i = 0; i < length; i++) {
      to[i] = array[i];
    }
    return to;
  }

}
