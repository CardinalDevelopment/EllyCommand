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
import ee.ellytr.command.util.OfflinePlayers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OfflinePlayersProvider implements ArgumentProvider<OfflinePlayers> {

  @Override
  public OfflinePlayers getMatch(String in, CommandSender sender) {
    OfflinePlayers offlinePlayers = new OfflinePlayers();
    if (in.contains(" ")) {
      for (String player : in.split(" ")) {
        offlinePlayers.addAll(getOfflinePlayers(player));
      }
    } else {
      offlinePlayers.addAll(getOfflinePlayers(in));
    }
    return offlinePlayers.isEmpty() ? null : offlinePlayers;
  }

  private List<OfflinePlayer> getOfflinePlayers(String in) {
    List<OfflinePlayer> players = new ArrayList<>();

    boolean exact = in.startsWith("@");
    boolean uuid = in.startsWith("#");
    if (exact) {
      in = in.substring(1);
    }
    if (uuid) {
      in = in.substring(1);
    }

    boolean containsWildcard = in.contains("*");
    in = in.replace("*", ".+");

    if (uuid) {
      if (containsWildcard) {
        for (Player player : Bukkit.getOnlinePlayers()) {
          if (player.getUniqueId().toString().matches(in)) {
            players.add(player);
          }
        }
      } else {
        players.add(Bukkit.getOfflinePlayer(UUID.fromString(in)));
      }
    } else if (exact) {
      players.add(Bukkit.getOfflinePlayer(in));
    } else {
      if (containsWildcard) {
        for (Player player : Bukkit.getOnlinePlayers()) {
          if (player.getName().matches(in)) {
            players.add(player);
          }
        }
      } else {
        Player player = Bukkit.getPlayer(in);
        if (player != null) {
          players.add(player);
        } else {
          players.add(Bukkit.getOfflinePlayer(in));
        }
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
