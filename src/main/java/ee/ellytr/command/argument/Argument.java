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
import ee.ellytr.command.CommandContext;
import ee.ellytr.command.CommandInstance;
import ee.ellytr.command.CommandMatch;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

  @SuppressWarnings("unchecked")
  public static CommandMatch matchArguments(
      @NonNull CommandInstance instance, @NonNull CommandContext context) {
    List<ArgumentContext> matches = new ArrayList<>();

    List<String> input = Lists.newArrayList(context.getArguments());
    List<Argument> arguments = instance.getArguments();

    CommandSender sender = context.getSender();

    for (int i = 0; i < arguments.size(); i++) {
      Argument argument = arguments.get(i);
      ArgumentProvider provider = argument.getProvider();

      if (input.isEmpty()) {
        handleInvalidArgument(matches, argument, sender, false);
      } else {
        try {
          int inputSize = input.size();
          if (inputSize < argument.getMin()) {
            handleInvalidArgument(matches, argument, sender, false);
          }

          int argsCount = 0;
          StringBuilder in = new StringBuilder();
          for (int j = 0; j < argument.getMax(); j++) {
            if (j < inputSize) {
              in.append(input.get(j)).append(" ");
              argsCount++;
            } else {
              break;
            }
          }

          Bukkit.broadcastMessage(in.toString());

          Object match = provider.getMatch(in.toString().trim(), sender);
          if (match == null) {
            handleInvalidArgument(matches, argument, sender, true);
          } else {
            matches.add(new ArgumentContext(match, argument, true));
            for (int j = 0; j < argsCount; j++) {
              input.remove(0);
            }
          }
        } catch (Exception e) {
          handleInvalidArgument(matches, argument, sender, true);
        }
      }
    }

    return new CommandMatch(matches, input);
  }

  @SuppressWarnings("unchecked")
  private static void handleInvalidArgument(
      @NonNull List<ArgumentContext> matches, @NonNull Argument argument, @NonNull CommandSender sender, boolean present) {
    if (argument.isRequired()) {
      matches.add(new ArgumentContext(null, argument, present));
    } else {
      String defaultValue = argument.getDefaultValue();
      if (!defaultValue.equals("")) {
        try {
          matches.add(new ArgumentContext(
              present ? null : argument.getProvider().getMatch(defaultValue, sender), argument, present
          ));
        } catch (Exception e) {
          matches.add(new ArgumentContext(null, argument, present));
        }
      } else {
        matches.add(new ArgumentContext(null, argument, present));
      }
    }
  }

}
