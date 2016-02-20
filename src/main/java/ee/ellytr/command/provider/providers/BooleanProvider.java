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
package ee.ellytr.command.provider.providers;

import com.google.common.collect.Lists;
import ee.ellytr.command.provider.ArgumentProvider;

import java.util.List;
import java.util.stream.Collectors;

public class BooleanProvider implements ArgumentProvider<Boolean> {

  @Override
  public List<String> getSuggestions(String in) {
    List<String> values = Lists.newArrayList("on", "off");
    return values.stream().filter(value -> value.toLowerCase().startsWith(in.toLowerCase())).collect(Collectors.toList());
  }

  @Override
  public Boolean getMatch(String in) {
    return in.equalsIgnoreCase("on") || in.equalsIgnoreCase("true");
  }

}

