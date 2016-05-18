package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class StringProvider implements ArgumentProvider<String> {

  @Override
  public String getMatch(String in, Player player) {
    return in;
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
