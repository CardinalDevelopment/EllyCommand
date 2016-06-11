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
package ee.ellytr.command.argument;

import com.google.common.collect.Lists;
import ee.ellytr.command.CommandInstance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class Argument<T> {

  private final boolean required;
  private final String defaultValue;
  private final int min;
  private final int max;

  private final ArgumentProvider<T> provider;

  public static boolean isRequired(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      Class clazz = annotation.annotationType();
      if (clazz.equals(Optional.class)) {
        return false;
      } else if (clazz.equals(Required.class)) {
        return true;
      }
    }
    return true;
  }

  public static List<Integer> getMultiArgs(Method method, int parameter) {
    for (Annotation annotation : method.getParameterAnnotations()[parameter]) {
      if (annotation.annotationType().equals(MultiArgs.class)) {
        MultiArgs multiArgs = method.getParameters()[parameter].getAnnotation(MultiArgs.class);
        return Lists.newArrayList(multiArgs.min(), multiArgs.max());
      }
    }
    return null;
  }

  public static List<Object> matchArguments(CommandInstance instance, String[] argsArray,
                                            CommandSender sender, boolean includeOptionals, boolean onlyPresentArgs) {
    List<Object> matches = Lists.newArrayList();

    List<String> args = Lists.newArrayList(argsArray);
    List<Argument> arguments = instance.getArguments();

    for (int i = 0; i < arguments.size(); i ++) {
      Argument argument = arguments.get(i);
      boolean required = argument.isRequired();

      ArgumentProvider provider = argument.getProvider();
      if (args.isEmpty()) {
        if (onlyPresentArgs) {
          break;
        } else {
          if (required) {
            matches.add(null);
          } else {
            String defaultValue = argument.getDefaultValue();
            if (!defaultValue.equals("")) {
              try {
                matches.add(provider.getMatch(defaultValue, sender));
              } catch (Exception e) {
                matches.add(includeOptionals ? java.util.Optional.empty() : null);
              }
            } else {
              matches.add(includeOptionals ? java.util.Optional.empty() : null);
            }
          }
        }
        continue;
      }
      String in = args.get(0);
      int min = argument.getMin(), max = argument.getMax();
      if (i + 1 == arguments.size() && (min != 1 || max != 1)) {
        int remaining = args.size();
        if (min <= remaining && remaining <= max) {
          StringBuilder inBuilder = new StringBuilder();
          args.forEach(arg -> inBuilder.append(arg).append(" "));
          in = inBuilder.toString().trim();
        } else {
          matches.add(null);
          break;
        }
      }

      try {
        Object match = provider.getMatch(in, sender);
        if (match == null) {
          if (required) {
            matches.add(null);
          } else {
            String defaultValue = argument.getDefaultValue();
            if (!defaultValue.equals("")) {
              try {
                matches.add(provider.getMatch(defaultValue, sender));
              } catch (Exception e) {
                matches.add(includeOptionals ? java.util.Optional.empty() : null);
              }
            } else {
              matches.add(includeOptionals ? java.util.Optional.empty() : null);
            }
          }
        } else {
          matches.add(match);
          args.remove(0);
        }
      } catch (Exception e) {
        matches.add(includeOptionals ? java.util.Optional.empty() : null);
      }
    }

    return matches;
  }

}
