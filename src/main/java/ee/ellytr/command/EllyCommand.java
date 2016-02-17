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

import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

@Getter
public class EllyCommand extends org.bukkit.command.Command {

  private final List<String> aliases;
  private final String description;
  private final String[] permissions;
  private final int min;
  private final int max;
  private final Class[] args;
  private final CommandExecutor executor;

  public EllyCommand(String[] aliases, String description, String[] permissions, int min, int max, Class[] args, CommandExecutor executor) {
    super(aliases[0], description, null, Arrays.asList(aliases));

    this.aliases = Arrays.asList(aliases);
    this.description = description;
    this.permissions = permissions;
    this.min = min;
    this.max = max;
    this.args = args;
    this.executor = executor;

    String usage = "/" + aliases[0];
    for (Class clazz : args) {
      usage += " <" + clazz.getSimpleName().toLowerCase() + ">";
    }
    this.setUsage(usage);
  }

  @Override
  public boolean execute(CommandSender sender, String label, String[] args) {
    return executor.onCommand(sender, this, label, args);
  }
}
