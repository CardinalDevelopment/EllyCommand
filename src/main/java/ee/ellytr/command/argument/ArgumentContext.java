package ee.ellytr.command.argument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArgumentContext<T> {

  private final T match;

  private final Argument<T> argument;
  private final boolean present;

}
