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
import ee.ellytr.command.argument.provider.ProviderRegistry;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Getter
public class CommandRegistry {

  private final Plugin plugin;
  private final List<Class> classes;
  private final ProviderRegistry providerRegistry;
  private final CommandFactory factory;

  /**
   * This is a registry that manages the classes that contain the command methods
   *
   * @param plugin The plugin that is using EllyCommand
   */
  public CommandRegistry(Plugin plugin) {
    this.plugin = plugin;
    classes = Lists.newArrayList();
    providerRegistry = new ProviderRegistry();
    factory = new CommandFactory(this);
  }

  /**
   * Adds a class to the registry
   *
   * @param clazz The class to be added
   */
  public void addClass(Class clazz) {
    classes.add(clazz);
  }

  /**
   * Builds the commands from the classes in the registry using an instance of CommandFactory
   */
  public void register() {
    factory.build();
  }

  /**
   * Removes a class from the registry
   *
   * @param clazz The class to be removed
   */
  public void removeClass(Class clazz) {
    classes.remove(clazz);
  }

}
