package ee.ellytr.command.argument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public class Argument<T> {

  private final boolean required;
  private final int min;
  private final int max;

  private final ArgumentProvider<T> provider;

  public static boolean isRequired(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      Class clazz = annotation.getClass();
      if (clazz.equals(Optional.class)) {
        return false;
      } else if (clazz.equals(Required.class)) {
        return true;
      }
    }
    return true;
  }

  public static int[] getMultiArgs(Method method, int parameter) {
    for (Annotation annotation : method.getParameterAnnotations()[parameter]) {
      if (annotation.getClass().equals(MutliArgs.class)) {
        MutliArgs mutliArgs = method.getParameters()[parameter].getAnnotation(MutliArgs.class);
        return new int[]{mutliArgs.min(), mutliArgs.max()};
      }
    }
    return new int[]{1, 1};
  }

}
