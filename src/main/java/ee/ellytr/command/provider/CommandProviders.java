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
package ee.ellytr.command.provider;

import com.google.common.collect.Maps;

import java.util.HashMap;

public class CommandProviders {

  private HashMap<Class, ArgumentProvider> providers = Maps.newHashMap();

  public <T> void addProvider(ArgumentProvider<T> provider, Class<T> clazz) {
    providers.put(clazz, provider);
  }

  @SuppressWarnings("unchecked")
  public <T> ArgumentProvider<T> getProvider(Class<T> clazz) {
    return providers.get(clazz);
  }

}
