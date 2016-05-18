package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class LongProvider implements ArgumentProvider<Long> {

  @Override
  public Long getMatch(String in, Player player) {
    return Long.parseLong(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
