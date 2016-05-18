package ee.ellytr.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.*;
import org.bukkit.command.Command;

import java.util.List;

@RequiredArgsConstructor
public class CommandTabCompleter implements TabCompleter {

  private final EllyCommand command;

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return null;
  }

}
