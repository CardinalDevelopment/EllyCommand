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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PlayerProvider implements ArgumentProvider<Player> {

  @Override
  public Player getMatch(String in) {
    if (in.startsWith("@")) {
      return Bukkit.getPlayerExact(in.substring(1));
    } else if (in.startsWith("#")) {
      return Bukkit.getPlayer(UUID.fromString(in.substring(1)));
    }
    return Bukkit.getPlayer(in);
  }

  @Override
  public List<String> getSuggestions(String in) {
    List<String> suggestions = Lists.newArrayList();
    for (Player player : Bukkit.getOnlinePlayers()) {
      String name = player.getName();
      if (name.toLowerCase().startsWith(in.toLowerCase())) {
        suggestions.add(name);
      }
    }
    return suggestions;
  }

}
