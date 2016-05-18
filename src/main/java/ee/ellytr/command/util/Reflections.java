package ee.ellytr.command.util;

import java.lang.reflect.Field;

public class Reflections {

  /*
   * Author: zml2008
   */
  @SuppressWarnings("unchecked")
  public static <T> T getField(Object from, String name) {
    Class<?> clazz = from.getClass();
    do {
      try {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(from);
      } catch (NoSuchFieldException | IllegalAccessException ignored) {
      }
    } while (clazz.getSuperclass() != Object.class && ((clazz = clazz.getSuperclass()) != null));
    return null;
  }

}
