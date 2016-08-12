package ee.ellytr.command.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchError {

  CONSOLE_NO_ACCESS(0),
  PLAYER_NO_ACCESS(0),

  NO_PERMISSION(1),

  TOO_FEW_ARGUMENTS(2),
  TOO_MANY_ARGUMENTS(2),

  INVALID_USAGE(3),

  INVALID_ARGUMENTS(4);

  private final int priority;

  public boolean isPrioritizedOver(@NonNull MatchError error) {
    return priority > error.getPriority();
  }

}
