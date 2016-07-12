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

import com.google.common.collect.Maps;
import ee.ellytr.command.argument.ArgumentProvider;
import ee.ellytr.command.argument.provider.BooleanProvider;
import ee.ellytr.command.argument.provider.ByteProvider;
import ee.ellytr.command.argument.provider.CharacterProvider;
import ee.ellytr.command.argument.provider.DoubleProvider;
import ee.ellytr.command.argument.provider.FloatProvider;
import ee.ellytr.command.argument.provider.IntegerProvider;
import ee.ellytr.command.argument.provider.LongProvider;
import ee.ellytr.command.argument.provider.ShortProvider;
import ee.ellytr.command.argument.provider.StringProvider;
import ee.ellytr.command.argument.provider.minecraft.BiomeProvider;
import ee.ellytr.command.argument.provider.minecraft.PlayerProvider;
import ee.ellytr.command.argument.provider.minecraft.PlayersProvider;
import ee.ellytr.command.argument.provider.minecraft.WorldProvider;
import ee.ellytr.command.util.Players;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ProviderRegistry {

  private Map<Class, ArgumentProvider> providers = Maps.newHashMap();

  protected ProviderRegistry() {
    registerDefaultProviders();
  }

  public <T> void registerProvider(Class<T> clazz, ArgumentProvider<T> provider) {
    providers.put(clazz, provider);
  }

  // ArgumentProviders will always match up with their respective class,
  // so we do not need to have an unchecked cast warning.
  @SuppressWarnings("unchecked")
  public <T> ArgumentProvider<T> getProvider(Class<T> clazz) {
    return providers.get(clazz);
  }

  private void registerDefaultProviders() {
    registerProvider(Boolean.class, new BooleanProvider());
    registerProvider(Byte.class, new ByteProvider());
    registerProvider(Character.class, new CharacterProvider());
    registerProvider(Double.class, new DoubleProvider());
    registerProvider(Float.class, new FloatProvider());
    registerProvider(Integer.class, new IntegerProvider());
    registerProvider(Long.class, new LongProvider());
    registerProvider(Short.class, new ShortProvider());
    registerProvider(String.class, new StringProvider());

    registerProvider(Biome.class, new BiomeProvider());
    registerProvider(Player.class, new PlayerProvider());
    registerProvider(Players.class, new PlayersProvider());
    registerProvider(World.class, new WorldProvider());
  }

}
