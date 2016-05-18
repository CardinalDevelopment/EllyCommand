package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class IntegerProvider implements ArgumentProvider<Integer> {

  @Override
  public Integer getMatch(String in, Player player) {
    return Integer.parseInt(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
