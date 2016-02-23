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

import java.lang.reflect.Field;

public class ReflectionUtil {

  /*
   * Author: zml2008
   */
  @SuppressWarnings("unchecked")
  public static <T> T getField(Object from, String name) {
    Class<?> checkClass = from.getClass();
    do {
      try {
        Field field = checkClass.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(from);
      } catch (NoSuchFieldException | IllegalAccessException ignored) { }
    } while (checkClass.getSuperclass() != Object.class && ((checkClass = checkClass.getSuperclass()) != null));
    return null;
  }

}
