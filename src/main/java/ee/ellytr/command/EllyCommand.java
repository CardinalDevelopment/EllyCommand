package ee.ellytr.command;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

@Getter
public class EllyCommand {

  private final List<String> aliases;
  private final String description;
  private final String usage;

  private final List<CommandInstance> instances = Lists.newArrayList();
  private final List<EllyCommand> nestedCommands = Lists.newArrayList();
  private final CommandTabCompleter tabCompleter = new CommandTabCompleter(this);

  protected EllyCommand(Command command) {
    aliases = Lists.newArrayList(command.aliases());
    description = command.description();
    usage = command.usage();
  }

  public String getName() {
    return aliases.get(0);
  }

  public void addAlias(String alias) {
    aliases.add(alias);
  }

  public boolean hasAlias(String alias) {
    return aliases.contains(alias);
  }

  public void addInstance(CommandInstance instance) {
    instances.add(instance);
  }

  public void addNestedCommand(EllyCommand command) {
    nestedCommands.add(command);
  }

}
