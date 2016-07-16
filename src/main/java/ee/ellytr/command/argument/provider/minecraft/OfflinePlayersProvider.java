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
