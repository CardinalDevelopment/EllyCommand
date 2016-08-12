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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OfflinePlayerProvider implements ArgumentProvider<OfflinePlayer> {

  @Override
  public OfflinePlayer getMatch(String in, CommandSender sender) {
    boolean exact = in.startsWith("@");
    boolean uuid = in.startsWith("#");
    if (exact || uuid) {
      in = in.substring(1);
    }
    if (exact) {
      return Bukkit.getOfflinePlayer(in);
    }
    if (uuid) {
      return Bukkit.getOfflinePlayer(UUID.fromString(in));
    }

    return Bukkit.getPlayer(in);
  }

  @Override
  public List<String> getSuggestions(String in, CommandSender sender) {
    return Bukkit.getOnlinePlayers().stream().map(Player::getName)
        .filter(name -> name.toLowerCase().startsWith(in.toLowerCase())).collect(Collectors.toList());
  }

}
