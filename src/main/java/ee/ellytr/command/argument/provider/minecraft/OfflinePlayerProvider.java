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
