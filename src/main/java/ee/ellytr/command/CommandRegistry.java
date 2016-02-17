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

import ee.ellytr.command.provider.ArgumentProvider;
import ee.ellytr.command.provider.providers.BooleanProvider;
import ee.ellytr.command.provider.providers.ByteProvider;
import ee.ellytr.command.provider.providers.CharacterProvider;
import ee.ellytr.command.provider.providers.DoubleProvider;
import ee.ellytr.command.provider.providers.FloatProvider;
import ee.ellytr.command.provider.providers.IntegerProvider;
import ee.ellytr.command.provider.providers.LongProvider;
import ee.ellytr.command.provider.providers.ShortProvider;
import ee.ellytr.command.util.ReflectionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class CommandRegistry {

  @Getter
  private Plugin plugin;
  @Getter
  private CommandExecutor executor;
  private List<Class> registered;
  private HashMap<ArgumentProvider, Class> providers;
  private CommandMap fallbackCommands;

  public CommandRegistry(Plugin plugin) {
    this(plugin, plugin);
  }

  public CommandRegistry(Plugin plugin, CommandExecutor executor) {
    this.plugin = plugin;
    this.executor = executor;
    registered = new ArrayList<>();
    providers = new HashMap<>();

    addDefaultProviders();
  }

  public void addClass(Class clazz) {
    Logger.getLogger("EllyCommand").info("hi" + clazz.getSimpleName());
    registered.add(clazz);
  }

  public <T> void addProvider(ArgumentProvider<T> provider, Class<T> clazz) {
    providers.put(provider, clazz);
  }

  public CommandManager register() {
    Logger.getLogger("EllyCommand").info("hi register");
    CommandFactory factory = new CommandFactory(this, registered);
    providers.forEach(factory::addProvider);
    return new CommandManager(factory);
  }

  private void addDefaultProviders() {
    addProvider(new BooleanProvider(), Boolean.class);
    addProvider(new ByteProvider(), Byte.class);
    addProvider(new CharacterProvider(), Character.class);
    addProvider(new DoubleProvider(), Double.class);
    addProvider(new FloatProvider(), Float.class);
    addProvider(new IntegerProvider(), Integer.class);
    addProvider(new LongProvider(), Long.class);
    addProvider(new ShortProvider(), Short.class);
  }

  public CommandMap getCommandMap() {
    CommandMap commandMap = ReflectionUtil.getField(plugin.getServer().getPluginManager(), "commandMap");
    if (commandMap == null) {
      if (fallbackCommands != null) {
        commandMap = fallbackCommands;
      } else {
        Logger.getLogger("EllyCommand").severe("Could not find plugin CommandMap, using fallback instead");
        fallbackCommands = commandMap = new SimpleCommandMap(Bukkit.getServer());
        Bukkit.getServer().getPluginManager().registerEvents(new FallbackRegistrationListener(fallbackCommands), plugin);
      }
    }
    return commandMap;
  }

}
