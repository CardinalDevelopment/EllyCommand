package ee.ellytr.command.exception;

import ee.ellytr.command.argument.ArgumentProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommandProviderException extends CommandException {

  private ArgumentProvider provider;

}
