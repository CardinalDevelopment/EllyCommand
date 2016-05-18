package ee.ellytr.command;

import ee.ellytr.command.argument.Argument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CommandInstance {

  private final int min;
  private final int max;
  private final String[] permissions;
  private final List<Argument> arguments;

  private final Method method;

}
