package ee.ellytr.command;

import ee.ellytr.command.argument.ArgumentContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CommandMatch {

  private final List<ArgumentContext> matches;
  private final List<String> overflow;

  public boolean hasOverflow() {
    return !overflow.isEmpty();
  }

}
