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

package ee.ellytr.command.argument.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ee.ellytr.command.argument.provider.providers.BooleanProvider;
import ee.ellytr.command.argument.provider.providers.ByteProvider;
import ee.ellytr.command.argument.provider.providers.CharacterProvider;
import ee.ellytr.command.argument.provider.providers.DoubleProvider;
import ee.ellytr.command.argument.provider.providers.FloatProvider;
import ee.ellytr.command.argument.provider.providers.IntegerProvider;
import ee.ellytr.command.argument.provider.providers.LongProvider;
import ee.ellytr.command.argument.provider.providers.PlayerProvider;
import ee.ellytr.command.argument.provider.providers.ShortProvider;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProviderRegistry {

  private final HashMap<Class, ArgumentProvider> providers;

  /**
   * Gets a new instance of a provider registry, which allows for the registering of argument providers.
   */
  public ProviderRegistry() {
    providers = Maps.newHashMap();
    addDefaultProviders();
  }

  /**
   * Add the default providers for the primitive types and the {@link Player} class.
   */
  private void addDefaultProviders() {
    registerProvider(new BooleanProvider(), Boolean.class);
    registerProvider(new ByteProvider(), Byte.class);
    registerProvider(new CharacterProvider(), Character.class);
    registerProvider(new DoubleProvider(), Double.class);
    registerProvider(new FloatProvider(), Float.class);
    registerProvider(new IntegerProvider(), Integer.class);
    registerProvider(new LongProvider(), Long.class);
    registerProvider(new PlayerProvider(), Player.class);
    registerProvider(new ShortProvider(), Short.class);
  }

  /**
   * Registers a provider under a certain class.
   *
   * @param provider The argument provider to be registered.
   * @param clazz    The class that the provider handles.
   * @param <T>      The class type that is handled by the provider.
   */
  public <T> void registerProvider(ArgumentProvider<T> provider, Class<T> clazz) {
    providers.put(clazz, provider);
  }

  /**
   * Gets a certain argument provider based on a class.
   *
   * @param clazz The class that the returned argument provider handles.
   * @param <T>   The class type that is specified.
   * @return The argument provider that handles the specified class.
   */
  @SuppressWarnings("unchecked")
  public <T> ArgumentProvider<T> getProvider(Class<T> clazz) {
    return providers.get(clazz);
  }

  /**
   * Unregister an argument provider.
   *
   * @param provider The provider to unregister.
   */
  public void unregisterProvider(ArgumentProvider provider) {
    List<Class> remove = Lists.newArrayList();
    remove.addAll(providers.keySet().stream().filter(clazz -> providers.get(clazz).equals(provider))
            .collect(Collectors.toList()));
    remove.forEach(providers::remove);
  }

}
