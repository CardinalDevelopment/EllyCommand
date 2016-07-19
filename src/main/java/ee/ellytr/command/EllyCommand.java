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
package ee.ellytr.command;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

@Getter
public class EllyCommand {

  private final List<String> aliases;
  private final String description;
  private final List<String> usages;

  private final List<CommandInstance> instances = Lists.newArrayList();
  private final List<EllyCommand> nestedCommands = Lists.newArrayList();
  private final CommandTabCompleter tabCompleter = new CommandTabCompleter(this);

  protected EllyCommand(Command command) {
    aliases = Lists.newArrayList(command.aliases());
    description = command.description();
    usages = Lists.newArrayList();
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
    String usage = instance.getUsage();
    if (!usage.equals("")) {
      usages.add(usage);
    }
  }

  public void addNestedCommand(EllyCommand command) {
    nestedCommands.add(command);
    usages.add(command.getUsage(true));
  }

  public String getUsage(boolean nested) {
    StringBuilder usage = new StringBuilder(nested ? "<" + aliases.get(0) + ">" : "/" + aliases.get(0));
    for (int i = 0; i < usages.size(); i ++) {
      if (i == 0) {
        usage.append(" ").append(usages.get(i));
      } else {
        usage.append(", ").append(usages.get(i));
      }
    }
    return usage.toString();
  }

}
