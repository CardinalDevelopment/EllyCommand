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
package ee.ellytr.command.argument.provider.minecraft;

import ee.ellytr.command.argument.ArgumentProvider;
import ee.ellytr.command.util.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayersProvider implements ArgumentProvider<Players> {

  @Override
  public Players getMatch(String in, CommandSender sender) {
    Players players = new Players();
    if (in.contains(" ")) {
      for (String player : in.split(" ")) {
        players.addAll(getPlayers(player));
      }
    } else {
      players.addAll(getPlayers(in));
    }
    return players;
  }

  private List<Player> getPlayers(String in) {
    List<Player> players = new ArrayList<>();

    boolean exact = in.startsWith("@");
    boolean uuid = in.startsWith("#");
    if (exact) {
      in = in.substring(1);
    }
    if (uuid) {
      in = in.substring(1);
    }

    boolean containsWildcard = in.contains("\\*");
    in = in.replace("\\*", ".+");

    if (uuid) {
      if (containsWildcard) {
        for (Player player : Bukkit.getOnlinePlayers()) {
          if (player.getUniqueId().toString().matches(in)) {
            players.add(player);
          }
        }
      } else {
        players.add(Bukkit.getPlayer(UUID.fromString(in)));
      }
    } else if (exact) {
      players.add(Bukkit.getPlayerExact(in));
    } else {
      if (containsWildcard) {
        for (Player player : Bukkit.getOnlinePlayers()) {
          if (player.getName().matches(in)) {
            players.add(player);
          }
        }
      } else {
        players.add(Bukkit.getPlayer(in));
      }
    }

    return players;
  }

  @Override
  public List<String> getSuggestions(String in, CommandSender sender) {
    return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(
        name -> name.toLowerCase().startsWith(in.toLowerCase())
    ).collect(Collectors.toList());
  }

}
