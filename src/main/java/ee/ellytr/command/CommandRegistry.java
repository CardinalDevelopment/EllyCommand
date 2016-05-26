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
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CommandRegistry {

  private final Plugin plugin;

  private final List<Class> classes = Lists.newArrayList();
  private final CommandFactory factory = new CommandFactory(this);
  private final ProviderRegistry providerRegistry = new ProviderRegistry();

  public void addClass(Class clazz) {
    classes.add(clazz);
  }

  public void removeClass(Class clazz) {
    classes.remove(clazz);
  }

  public void register() {
    factory.build();
  }

}
