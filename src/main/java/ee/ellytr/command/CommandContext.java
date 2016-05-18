package ee.ellytr.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@Getter
@RequiredArgsConstructor
public class CommandContext {

  private final CommandSender sender;
  private final String[] arguments;

}
