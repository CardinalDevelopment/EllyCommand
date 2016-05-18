package ee.ellytr.command.argument.provider.minecraft;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class WorldProvider implements ArgumentProvider<World> {

  @Override
  public World getMatch(String in, Player player) {
    return Bukkit.getWorld(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return Bukkit.getWorlds().stream().map(World::getName)
        .filter(str -> str.toLowerCase().startsWith(in.toLowerCase())).collect(Collectors.toList());
  }

}
