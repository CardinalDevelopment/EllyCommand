package ee.ellytr.command.argument.provider.minecraft;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerProvider implements ArgumentProvider<Player> {

  @Override
  public Player getMatch(String in, Player player) {
    boolean exact = in.startsWith("@");
    boolean uuid = in.startsWith("#");
    if (exact || uuid) {
      in = in.substring(1);
    }
    if (exact) {
      return Bukkit.getPlayerExact(in);
    }
    if (uuid) {
      return Bukkit.getPlayer(UUID.fromString(in));
    }
    return Bukkit.getPlayer(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return Bukkit.getOnlinePlayers().stream().map(Player::getName)
        .filter(str -> str.toLowerCase().startsWith(in.toLowerCase())).collect(Collectors.toList());
  }

}
